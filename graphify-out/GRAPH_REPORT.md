# Graph Report - /Users/eurobuddha/Projects/minima/apks/futurecash  (2026-08-23)

## Corpus Check
- 15 files · ~14,624 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 296 nodes · 501 edges · 32 communities (17 shown, 15 thin omitted)
- Extraction: 86% EXTRACTED · 13% INFERRED · 1% AMBIGUOUS · INFERRED: 66 edges (avg confidence: 0.82)
- Token cost: 173,271 input · 0 output

## Community Hubs (Navigation)
- Main Activity Shell
- Payment Card Rendering
- About & Future Tabs
- Launcher Icon Identity
- Tab Page Scaffolding
- Collect Transaction Flow
- Create Future Payment Flow
- Sub-Screen Form Helpers
- FutureCash Covenant Domain
- Node IPC Transport
- Gradle Wrapper
- README Documentation
- Project Instructions
- Design Tokens
- RULE 0 Working Agreement
- Git Hook Installer
- Pre-commit Version Gate
- LinearLayout Ref
- LinearLayout Ref
- BroadcastReceiver Ref
- Bundle Ref
- Handler Ref
- JSONObject Ref
- LinearLayout Ref
- View Ref
- LinearLayout Ref
- OnClickListener Ref
- Override Ref
- ViewPager Ref

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 45 edges
2. `CreateFutureActivity` - 19 edges
3. `CollectActivity` - 17 edges
4. `SubActivity` - 17 edges
5. `BaseView` - 13 edges
6. `FuturePayment` - 13 edges
7. `NodeApi` - 13 edges
8. `Cb` - 13 edges
9. `MainPager` - 10 edges
10. `Util` - 10 edges

## Surprising Connections (you probably didn't know these)
- `Versioning Guardrail — Every Code Change Ships With a Version Bump` --semantically_similar_to--> `No SIGNEDBY in the Covenant — Signature-free Collection`  [INFERRED] [semantically similar]
  CLAUDE.md → README.md
- `Versioning Guardrail — Every Code Change Ships With a Version Bump` --conceptually_related_to--> `Minima FutureCash (native Android app)`  [INFERRED]
  CLAUDE.md → README.md
- `Versioning Guardrail — Every Code Change Ships With a Version Bump` --conceptually_related_to--> `PandaApps Catalog (apks.json release channel)`  [INFERRED]
  CLAUDE.md → README.md
- `Orange Rounded-Square Badge` --conceptually_related_to--> `Minima Companion APK Iconography`  [AMBIGUOUS]
  app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.png → app/src/main/res/mipmap-hdpi/ic_launcher_foreground.png
- `Clock Dial Glyph (black analogue clock face on orange rounded square)` --semantically_similar_to--> `FutureCash App Identity`  [AMBIGUOUS] [semantically similar]
  app/src/main/res/mipmap-xhdpi/ic_launcher_foreground.png → app/src/main/res/mipmap-mdpi/ic_launcher_foreground.png

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **FutureCash time-locked payment lifecycle** — readme_send_to_the_future, readme_futurecash_covenant, readme_maturity, readme_collect [EXTRACTED 1.00]
- **Covenant address interoperability via verbatim script port** — readme_newscript, readme_futurecash_covenant, readme_futurecash_web_dapp, readme_minima_core_node [EXTRACTED 1.00]

## Communities (32 total, 15 thin omitted)

### Community 0 - "Main Activity Shell"
Cohesion: 0.08
Nodes (15): android.content.BroadcastReceiver, android.os.Bundle, android.os.Handler, androidx.appcompat.app.AppCompatActivity, androidx.viewpager.widget.ViewPager, BaseView, LinearLayout, Override (+7 more)

### Community 1 - "Payment Card Rendering"
Cohesion: 0.08
Nodes (7): FcCardUi, View, FutureCashContract, FuturePayment, JSONObject, JSONObject, Util

### Community 2 - "About & Future Tabs"
Cohesion: 0.11
Nodes (17): android.view.View, android.widget.Button, android.widget.LinearLayout, android.widget.TextView, AboutView, OnClickListener, Override, FutureView (+9 more)

### Community 3 - "Launcher Icon Identity"
Cohesion: 0.11
Nodes (27): Android Adaptive Icon Foreground Layer, FutureCash Launcher Icon Foreground (hdpi), Android Adaptive Icon Foreground Layer (hdpi density bucket), FutureCash App Visual Identity, Analog Clock Face Motif, Minima Companion APK Iconography, Time-Locked / Future-Dated Funds, FutureCash Launcher Icon Foreground (xhdpi) (+19 more)

### Community 4 - "Tab Page Scaffolding"
Cohesion: 0.12
Nodes (9): BaseView, View, Override, View, MainPager, NonNull, PagerAdapter, SuppressWarnings (+1 more)

### Community 5 - "Collect Transaction Flow"
Cohesion: 0.17
Nodes (5): CollectActivity, BroadcastReceiver, EditText, Override, TextView

### Community 6 - "Create Future Payment Flow"
Cohesion: 0.17
Nodes (9): CreateFutureActivity, EditText, Override, TextView, MsCb, Cb, ArrayAdapter, SimpleDateFormat (+1 more)

### Community 7 - "Sub-Screen Form Helpers"
Cohesion: 0.22
Nodes (7): Bundle, EditText, LinearLayout, Override, TextView, SubActivity, AppCompatActivity

### Community 8 - "FutureCash Covenant Domain"
Cohesion: 0.17
Nodes (16): Pre-commit Version-Bump Hook (.githooks/pre-commit), Versioning Guardrail — Every Code Change Ships With a Version Bump, Collect (single-shot spend of matured coin), FutureCash Covenant Address, FutureCash Web Dapp (address-compatible sibling), Maturity (unlock block / coin-age threshold), Local Minima Core Node, Minima FutureCash (native Android app) (+8 more)

### Community 9 - "Node IPC Transport"
Cohesion: 0.23
Nodes (6): Handler, JSONObject, NodeApi, PairingListener, Context, MinimaAPI

### Community 10 - "Gradle Wrapper"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 11 - "README Documentation"
Cohesion: 0.40
Nodes (4): Build, How it works, Minima FutureCash (native Android), Releases

### Community 12 - "Project Instructions"
Cohesion: 0.50
Nodes (3): RULE 0 (highest priority) — Follow the user's explicit instructions. They are BLOCKING, not suggestions., User instructions — AUTHORITATIVE. These override default behavior and must be followed exactly., Versioning guardrail — every code change ships with a version bump

### Community 14 - "RULE 0 Working Agreement"
Cohesion: 0.67
Nodes (3): Disagree Openly, Never Disobey Quietly, Reuse Before You Reinvent, RULE 0 — Explicit User Instructions Are Blocking

## Ambiguous Edges - Review These
- `FutureCash Launcher Icon Foreground (hdpi)` → `Minima Companion APK Iconography`  [AMBIGUOUS]
  app/src/main/res/mipmap-hdpi/ic_launcher_foreground.png · relation: semantically_similar_to
- `Minima Companion APK Iconography` → `Orange Rounded-Square Badge`  [AMBIGUOUS]
  app/src/main/res/mipmap-hdpi/ic_launcher_foreground.png · relation: conceptually_related_to
- `FutureCash App Identity` → `Clock Dial Glyph (black analogue clock face on orange rounded square)`  [AMBIGUOUS]
  app/src/main/res/mipmap-xhdpi/ic_launcher_foreground.png · relation: semantically_similar_to
- `Minima Companion APK Branding` → `Orange Rounded-Square Badge`  [AMBIGUOUS]
  app/src/main/res/mipmap-mdpi/ic_launcher_foreground.png · relation: conceptually_related_to
- `Orange Rounded-Square Badge` → `Minima Companion-APK Visual Branding`  [AMBIGUOUS]
  app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.png · relation: conceptually_related_to
- `Orange-on-Black Brand Palette` → `Minima Companion APK Family Branding`  [AMBIGUOUS]
  app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png · relation: conceptually_related_to

## Knowledge Gaps
- **15 isolated node(s):** `RULE 0 (highest priority) — Follow the user's explicit instructions. They are BLOCKING, not suggestions.`, `Versioning guardrail — every code change ships with a version bump`, `How it works`, `Build`, `Releases` (+10 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **15 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `FutureCash Launcher Icon Foreground (hdpi)` and `Minima Companion APK Iconography`?**
  _Edge tagged AMBIGUOUS (relation: semantically_similar_to) - confidence is low._
- **What is the exact relationship between `Minima Companion APK Iconography` and `Orange Rounded-Square Badge`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `FutureCash App Identity` and `Clock Dial Glyph (black analogue clock face on orange rounded square)`?**
  _Edge tagged AMBIGUOUS (relation: semantically_similar_to) - confidence is low._
- **What is the exact relationship between `Minima Companion APK Branding` and `Orange Rounded-Square Badge`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Orange Rounded-Square Badge` and `Minima Companion-APK Visual Branding`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Orange-on-Black Brand Palette` and `Minima Companion APK Family Branding`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **Why does `MainActivity` connect `Main Activity Shell` to `Payment Card Rendering`, `About & Future Tabs`, `Tab Page Scaffolding`?**
  _High betweenness centrality (0.298) - this node is a cross-community bridge._