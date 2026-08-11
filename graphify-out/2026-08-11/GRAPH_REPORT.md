# Graph Report - futurecash  (2026-08-11)

## Corpus Check
- 21 files · ~13,943 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 237 nodes · 450 edges · 24 communities (14 shown, 10 thin omitted)
- Extraction: 87% EXTRACTED · 13% INFERRED · 0% AMBIGUOUS · INFERRED: 57 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `21b4105a`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- MainActivity
- Util
- .dp
- CreateFutureActivity
- CollectActivity
- Cb
- SubActivity
- FuturePayment
- BaseView
- LinearLayout
- LinearLayout
- gradlew
- FcDesign
- Minima FutureCash (native Android)
- LinearLayout
- BroadcastReceiver
- Bundle
- Handler
- JSONObject
- View
- ViewPager

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 43 edges
2. `CreateFutureActivity` - 19 edges
3. `CollectActivity` - 17 edges
4. `Cb` - 17 edges
5. `SubActivity` - 17 edges
6. `BaseView` - 13 edges
7. `FuturePayment` - 13 edges
8. `NodeApi` - 13 edges
9. `MainPager` - 10 edges
10. `Util` - 10 edges

## Surprising Connections (you probably didn't know these)
- `BaseView` --references--> `MainActivity`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecash/BaseView.java → app/src/main/java/com/eurobuddha/futurecash/MainActivity.java
- `CollectActivity` --references--> `FuturePayment`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecash/CollectActivity.java → app/src/main/java/com/eurobuddha/futurecash/FuturePayment.java
- `CollectActivity` --inherits--> `SubActivity`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecash/CollectActivity.java → app/src/main/java/com/eurobuddha/futurecash/SubActivity.java
- `CreateFutureActivity` --inherits--> `SubActivity`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecash/CreateFutureActivity.java → app/src/main/java/com/eurobuddha/futurecash/SubActivity.java
- `SubActivity` --references--> `NodeApi`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/futurecash/SubActivity.java → app/src/main/java/com/eurobuddha/futurecash/NodeApi.java

## Import Cycles
- None detected.

## Communities (24 total, 10 thin omitted)

### Community 0 - "MainActivity"
Cohesion: 0.10
Nodes (14): android.content.BroadcastReceiver, android.os.Bundle, android.os.Handler, androidx.appcompat.app.AppCompatActivity, androidx.viewpager.widget.ViewPager, BaseView, LinearLayout, Override (+6 more)

### Community 1 - "Util"
Cohesion: 0.11
Nodes (3): JSONObject, Util, FuturePayment

### Community 2 - ".dp"
Cohesion: 0.11
Nodes (17): android.view.View, android.widget.Button, android.widget.LinearLayout, android.widget.TextView, AboutView, OnClickListener, Override, FutureView (+9 more)

### Community 3 - "CreateFutureActivity"
Cohesion: 0.18
Nodes (8): CreateFutureActivity, EditText, Override, TextView, MsCb, ArrayAdapter, SimpleDateFormat, Spinner

### Community 4 - "CollectActivity"
Cohesion: 0.15
Nodes (7): CollectActivity, BroadcastReceiver, EditText, Override, TextView, FcCardUi, View

### Community 5 - "Cb"
Cohesion: 0.19
Nodes (7): Cb, Handler, JSONObject, NodeApi, PairingListener, Context, MinimaAPI

### Community 6 - "SubActivity"
Cohesion: 0.22
Nodes (7): Bundle, EditText, LinearLayout, Override, TextView, SubActivity, AppCompatActivity

### Community 7 - "FuturePayment"
Cohesion: 0.20
Nodes (3): FutureCashContract, FuturePayment, JSONObject

### Community 8 - "BaseView"
Cohesion: 0.14
Nodes (8): BaseView, View, Override, View, MainPager, NonNull, PagerAdapter, ViewGroup

### Community 11 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 16 - "Minima FutureCash (native Android)"
Cohesion: 0.40
Nodes (4): Build, How it works, Minima FutureCash (native Android), Releases

## Knowledge Gaps
- **3 isolated node(s):** `How it works`, `Build`, `Releases`
  These have ≤1 connection - possible missing edges or undocumented components.
- **10 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `Util`, `.dp`, `CollectActivity`, `Cb`, `BaseView`?**
  _High betweenness centrality (0.413) - this node is a cross-community bridge._
- **Why does `BaseView` connect `BaseView` to `MainActivity`, `.dp`?**
  _High betweenness centrality (0.151) - this node is a cross-community bridge._
- **Why does `Cb` connect `Cb` to `Util`, `CreateFutureActivity`, `CollectActivity`?**
  _High betweenness centrality (0.141) - this node is a cross-community bridge._
- **Are the 13 inferred relationships involving `Cb` (e.g. with `.fetchBlock()` and `.runSequence()`) actually correct?**
  _`Cb` has 13 INFERRED edges - model-reasoned connections that need verification._
- **What connects `How it works`, `Build`, `Releases` to the rest of the system?**
  _3 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `MainActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.1032258064516129 - nodes in this community are weakly interconnected._
- **Should `Util` be split into smaller, more focused modules?**
  _Cohesion score 0.11067193675889328 - nodes in this community are weakly interconnected._