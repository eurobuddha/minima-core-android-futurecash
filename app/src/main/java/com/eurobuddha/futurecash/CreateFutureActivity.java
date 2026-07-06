package com.eurobuddha.futurecash;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** Send to the future — token, recipient, amount, unlock date → send to the script with the FutureCash state. */
public class CreateFutureActivity extends SubActivity {

    private final SimpleDateFormat fmt = new SimpleDateFormat("dd MMM yyyy  HH:mm", Locale.ENGLISH);

    private String script = "";
    private int tip = 0;
    private final List<String> tokenIds = new ArrayList<>();
    private final List<String> tokenNames = new ArrayList<>();

    private Spinner tokenSpinner;
    private EditText addressInput, amountInput, burnInput, passwordInput;
    private TextView unlockBtn, sendBtn, status;
    private long unlockMs = 0;

    @Override
    protected void init() {
        title("Send to the future");
        script = getIntent().getStringExtra("script");
        if (script == null) script = "";
        buildForm();
        fetchBlock();
        fetchTokens();
        fetchDefaultAddress();
        if (script.isEmpty()) resolveScript();   // self-resolve if MainActivity hadn't yet — avoids a blank address
    }

    /** Resolve the FutureCash script address ourselves (register, then fall back to a scripts lookup). */
    private void resolveScript() {
        node.cmd("newscript script:\"" + FutureCashContract.SCRIPT + "\" trackall:false", new NodeApi.Cb() {
            @Override public void onResult(JSONObject j) {
                JSONObject r = j.optJSONObject("response");
                String a = r == null ? "" : r.optString("address", "");
                if (!a.isEmpty()) script = a; else resolveViaScripts();
            }
            @Override public void onError(String m) { resolveViaScripts(); }
        });
    }

    private void resolveViaScripts() {
        node.cmd("scripts", new NodeApi.Cb() {
            @Override public void onResult(JSONObject j) {
                JSONArray arr = j.optJSONArray("response");
                if (arr != null) for (int i = 0; i < arr.length(); i++) {
                    JSONObject s = arr.optJSONObject(i);
                    if (s == null) continue;
                    if (FutureCashContract.SCRIPT.equals(s.optString("script", "").replaceAll("\\s+", " ").trim())) {
                        script = s.optString("address", script);
                        break;
                    }
                }
            }
            @Override public void onError(String m) {}
        });
    }

    private void buildForm() {
        label("Token");
        tokenSpinner = new Spinner(this);
        tokenNames.add("Minima"); tokenIds.add("0x00");
        tokenSpinner.setAdapter(tokenAdapter());
        form.addView(tokenSpinner);

        label("Recipient address");
        addressInput = input("Mx… or 0x… (defaults to you)", T_TEXT);

        label("Amount");
        amountInput = input("0.0", T_DEC);

        label("Unlock date & time");
        unlockBtn = pickerButton("Select when it unlocks");
        unlockBtn.setOnClickListener(v -> pickDateTime(unlockMs, ms -> { unlockMs = ms; unlockBtn.setText(fmt.format(ms)); }));

        label("Burn (optional, MINIMA)");
        burnInput = input("0", T_DEC);

        label("Vault password (only if your node is locked)");
        passwordInput = input("Vault password", T_PASS);

        sendBtn = primaryButton("Lock & send");
        sendBtn.setOnClickListener(v -> submit());
        status = status();
    }

    private ArrayAdapter<String> tokenAdapter() {
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tokenNames);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return a;
    }

    // ---- node fetches ----

    private void fetchBlock() {
        node.cmd("block", new NodeApi.Cb() {
            @Override public void onResult(JSONObject j) {
                JSONObject r = j.optJSONObject("response");
                if (r != null) try { tip = Integer.parseInt(r.optString("block", "0")); } catch (Exception ignored) {}
            }
            @Override public void onError(String m) {}
        });
    }

    private void fetchTokens() {
        node.cmd("balance", new NodeApi.Cb() {
            @Override public void onResult(JSONObject j) {
                JSONArray arr = j.optJSONArray("response");
                if (arr == null) return;
                tokenIds.clear(); tokenNames.clear();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject b = arr.optJSONObject(i);
                    if (b == null) continue;
                    String tid = b.optString("tokenid", "0x00");
                    String name = Util.isMinima(tid) ? "Minima" : Util.tokenName(b.opt("token"), tid);
                    // Show the confirmed balance (what the user recognises). `sendable` is only what can be
                    // spent right now; note it in parens when it's lower so a send can't fail confusingly.
                    String confirmed = Util.tidyAmount(b.optString("confirmed", "0"));
                    String sendable = Util.tidyAmount(b.optString("sendable", "0"));
                    String label = confirmed.equals(sendable)
                            ? name + "  (" + confirmed + ")"
                            : name + "  (" + confirmed + " · " + sendable + " sendable)";
                    tokenIds.add(tid); tokenNames.add(label);
                }
                if (tokenIds.isEmpty()) { tokenIds.add("0x00"); tokenNames.add("Minima"); }
                tokenSpinner.setAdapter(tokenAdapter());
            }
            @Override public void onError(String m) {}
        });
    }

    private void fetchDefaultAddress() {
        node.cmd("getaddress", new NodeApi.Cb() {
            @Override public void onResult(JSONObject j) {
                JSONObject r = j.optJSONObject("response");
                String a = r == null ? "" : r.optString("miniaddress", r.optString("address", ""));
                if (!a.isEmpty() && addressInput.getText().length() == 0) addressInput.setText(a);
            }
            @Override public void onError(String m) {}
        });
    }

    // ---- submit ----

    private void submit() {
        final String tokenid = tokenIds.get(Math.max(0, tokenSpinner.getSelectedItemPosition()));
        final String addr = addressInput.getText().toString().trim();
        final String amountStr = amountInput.getText().toString().trim();
        final String burn = burnInput.getText().toString().trim();
        final String pw = passwordInput.getText().toString().trim();

        if (script == null || script.isEmpty()) { setStatus(status, "Still preparing the FutureCash contract — try again in a moment.", false); return; }
        if (addr.isEmpty() || !Util.isValidAddress(addr)) { setStatus(status, "Enter a valid recipient address.", false); return; }
        final BigDecimal amount;
        try { amount = new BigDecimal(amountStr); } catch (Exception e) { setStatus(status, "Enter a valid amount.", false); return; }
        if (amount.signum() <= 0) { setStatus(status, "Amount must be greater than zero.", false); return; }
        if (unlockMs == 0) { setStatus(status, "Pick an unlock date.", false); return; }
        if (unlockMs <= System.currentTimeMillis()) { setStatus(status, "Unlock must be in the future.", false); return; }
        // The node command grammar is space-delimited with no quoting, so whitespace in these free-text
        // fields would corrupt the command. Amount/address are already numeric/hex-validated.
        if (!burn.isEmpty() && !isPositive(burn)) { setStatus(status, "Burn must be a positive number.", false); return; }
        if (!pw.isEmpty() && pw.matches(".*\\s.*")) { setStatus(status, "Vault passwords with spaces can't be sent over this connection.", false); return; }
        if (tip == 0) { setStatus(status, "Still syncing the chain tip — try again in a moment.", false); return; }

        // Normalise to 0x — the contract's VERIFYOUT compares against state[2] (always 0x hex).
        sendBtn.setEnabled(false);
        setStatus(status, "Validating address…", true);
        node.cmd("checkaddress address:" + addr, new NodeApi.Cb() {
            @Override public void onResult(JSONObject j) {
                if (!j.optBoolean("status", false)) { sendBtn.setEnabled(true); setStatus(status, "That isn't a valid Minima address.", false); return; }
                JSONObject r = j.optJSONObject("response");
                String hex = r == null ? "" : r.optString("0x", "");
                if (hex.isEmpty()) hex = addr;
                doSend(hex, amount, tokenid, burn, pw);
            }
            @Override public void onError(String m) {
                sendBtn.setEnabled(true);
                setStatus(status, NodeApi.ERR_NOT_ENABLED.equals(m) ? "Enable FutureCash in Minima Core → Apps first." : "Could not validate the address.", false);
            }
        });
    }

    /** Build the FutureCash state (exactly as the dapp) and `send` the amount to the script address. */
    private void doSend(String recipientHex, BigDecimal amount, String tokenid, String burn, String pw) {
        long futureBlock = FutureCashContract.blockForDate(tip, unlockMs);
        long delta = Math.max(0, futureBlock - tip);
        long now = System.currentTimeMillis();

        // State ports: 0 reserved, 1 future block, 2 recipient (0x), 3 created ms, 4 coinage threshold.
        StringBuilder state = new StringBuilder("{");
        state.append("\"0\":\"0xFF\",");
        state.append("\"1\":\"").append(futureBlock).append("\",");
        state.append("\"2\":\"").append(recipientHex).append("\",");
        state.append("\"3\":\"").append(now).append("\",");
        state.append("\"4\":\"").append(delta).append("\"}");

        StringBuilder cmd = new StringBuilder("send amount:").append(amount.toPlainString())
                .append(" address:").append(script)
                .append(" tokenid:").append(tokenid)
                .append(" state:").append(state);
        if (!pw.isEmpty()) cmd.append(" password:").append(pw);
        if (!burn.isEmpty() && isPositive(burn)) cmd.append(" burn:").append(burn);

        sendBtn.setEnabled(false);
        setStatus(status, "Locking funds…", true);
        node.cmd(cmd.toString(), new NodeApi.Cb() {
            @Override public void onResult(JSONObject j) {
                boolean ok = j.optBoolean("status", false);
                boolean pending = j.optBoolean("pending", false);
                if (ok || pending) {
                    setStatus(status, pending ? "Pending — approve it in Minima Core, then it appears in Future."
                            : "✓ Sent. It will appear under Future shortly.", true);
                    unlockBtn.postDelayed(CreateFutureActivity.this::finish, 1400);
                } else {
                    sendBtn.setEnabled(true);
                    setStatus(status, j.optString("error", "Could not send."), false);
                }
            }
            @Override public void onError(String m) {
                sendBtn.setEnabled(true);
                setStatus(status, NodeApi.ERR_NOT_ENABLED.equals(m) ? "Enable FutureCash in Minima Core → Apps first." : m, false);
            }
        });
    }

    // ---- date-time picker ----

    private interface MsCb { void on(long ms); }

    private void pickDateTime(long initial, final MsCb cb) {
        final Calendar c = Calendar.getInstance();
        if (initial > 0) c.setTimeInMillis(initial);
        new DatePickerDialog(this, (dp, y, mo, d) -> {
            c.set(Calendar.YEAR, y); c.set(Calendar.MONTH, mo); c.set(Calendar.DAY_OF_MONTH, d);
            new TimePickerDialog(this, (tp, h, mi) -> {
                c.set(Calendar.HOUR_OF_DAY, h); c.set(Calendar.MINUTE, mi); c.set(Calendar.SECOND, 0);
                cb.on(c.getTimeInMillis());
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private boolean isPositive(String s) { try { return new BigDecimal(s).signum() > 0; } catch (Exception e) { return false; } }
}
