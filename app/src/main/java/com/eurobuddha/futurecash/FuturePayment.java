package com.eurobuddha.futurecash;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;

/**
 * A future-cash payment = a coin at the script address, locked until a future block and payable to a
 * recipient. Parsed from the node's {@code coins} response (state is a {port,type,data} array).
 */
public class FuturePayment {

    public String coinid, tokenid, address, tokenName, tokenUrl;
    public BigDecimal amount;        // the locked amount (full payout on collect)
    public String recipient;         // state[2] — who can collect (0x hex)
    public long futureBlock;         // state[1] — unlock block
    public long createdMs;           // state[3] — created timestamp (ms)
    public long coinageThreshold;    // state[4] — alt unlock (blocks since creation)
    public long createdBlock;        // coin.created — for coin age
    public JSONObject raw;           // source coin

    public static FuturePayment from(JSONObject coin) {
        FuturePayment p = new FuturePayment();
        p.raw = coin;
        p.coinid = coin.optString("coinid", "");
        p.tokenid = coin.optString("tokenid", "0x00");
        p.address = coin.optString("address", "");
        boolean minima = Util.isMinima(p.tokenid);
        p.amount = bd(minima ? coin.optString("amount", "0")
                : coin.optString("tokenamount", coin.optString("amount", "0")));
        Object tok = coin.opt("token");
        p.tokenName = minima ? "Minima" : Util.tokenName(tok, p.tokenid);
        if (tok instanceof JSONObject) p.tokenUrl = ((JSONObject) tok).optString("url", "");
        p.createdBlock = parseL(coin.optString("created", "0"));

        p.futureBlock      = parseL(state(coin, 1));
        p.recipient        = state(coin, 2);
        p.createdMs        = parseL(state(coin, 3));
        p.coinageThreshold = parseL(state(coin, 4));
        return p;
    }

    public boolean isMinima() { return Util.isMinima(tokenid); }
    public long coinAge(long tip) { return Math.max(0, tip - createdBlock); }
    public boolean matured(long tip) { return FutureCashContract.matured(tip, coinAge(tip), futureBlock, coinageThreshold); }
    public long blocksRemaining(long tip) { long left = futureBlock - tip; return left > 0 ? left : 0; }

    /** A real future-cash coin carries a recipient and a future block. */
    public boolean valid() { return recipient != null && !recipient.isEmpty() && futureBlock > 0; }

    static String state(JSONObject coin, int port) {
        JSONArray st = coin.optJSONArray("state");
        if (st == null) return "";
        for (int i = 0; i < st.length(); i++) {
            JSONObject s = st.optJSONObject(i);
            if (s != null && s.optInt("port", -1) == port) return s.optString("data", "");
        }
        return "";
    }

    static BigDecimal bd(String s) { try { return new BigDecimal(s); } catch (Exception e) { return BigDecimal.ZERO; } }
    static long parseL(String s) { try { return Long.parseLong(s.trim()); } catch (Exception e) { return 0; } }
}
