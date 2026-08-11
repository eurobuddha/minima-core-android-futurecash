# Minima FutureCash (native Android)

A native Android app for **time-locked payments on [Minima](https://minima.global)** — "send money to the future".
Lock funds now so they become payable to a chosen recipient **only after a future block** (a date/time you pick), then
anyone can collect the matured payment to that recipient. Trustless and non-custodial, all on-chain. Package
`com.eurobuddha.futurecash`.

## How it works

- **Send to the future** — pick a token, recipient, amount, and an unlock date/time. The funds are sent to a
  **FutureCash covenant** address with the unlock block + recipient encoded in coin state. On pairing the app
  registers the covenant script (`newscript`), and because the script is a verbatim port, it derives the **same
  address as the FutureCash web dapp** — so the two interoperate.
- **Maturity** — the covenant releases only once the chain reaches the future block (or an equivalent coin-age
  threshold), and only to the stored recipient. Until then the funds sit locked; no one — not even the sender — can
  redirect them.
- **Collect** — after maturity, the matured coin is listed; collecting it spends the whole coin to the recipient in a
  single-shot transaction. The covenant has **no `SIGNEDBY`**, so collecting needs **no signature** — hence no WOTS
  key-use / key-reuse risk.

It talks to the **local Minima Core node** over the broadcast-Intent IPC (`minimaapi`, the same transport as Vestr) —
no server, no internet permission for the payment flow.

## Build

Requires a **JDK 17/21** (the Android Studio JBR works):

```sh
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleRelease
```

Install, then enable **FutureCash** in Minima Core → Apps to authorize the IPC and register the covenant script.

## Releases

Versioned APKs are published to the [PandaApps catalog](https://github.com/eurobuddha/minima-core-apks)
(`apks.json`). Current: **v0.2.5**.
