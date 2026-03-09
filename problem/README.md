# TODO App - 実装課題

Clojure/ClojureScript フルスタック TODO アプリケーションの実装課題です。
以下の2つの事前課題に取り組んでください。

## 技術スタック

### バックエンド
- Clojure + Aleph (HTTP サーバー)
- mount (状態管理)
- cprop (設定)
- HoneySQL + next.jdbc (DB クエリ)
- HikariCP (コネクションプール)
- ragtime (マイグレーション)
- reitit + malli (ルーティング + バリデーション)

### フロントエンド
- ClojureScript + shadow-cljs
- re-frame (状態管理)
- HSX (React コンポーネント)
- shadow-css (スタイリング)
- reitit-frontend (ルーティング)

### インフラ
- Docker Compose
- PostgreSQL 17
- nginx (リバースプロキシ)

## セットアップ

### 1. コンテナ起動

```bash
cd problem/
docker compose up -d
```

初回起動時は依存関係の解決やビルドに数分かかります。
`docker compose logs -f` でログを確認し、frontend コンテナに `[:build-hook] All builds completed!` と表示されたらブラウザでアクセスできます。

### 2. nREPL に接続

server コンテナが起動すると nREPL が port 52004 でリスンします。
VS Code + Calva から以下の手順で接続してください。

1. コマンドパレットを開く (Windows/Linux: `Ctrl+Shift+P`, macOS: `Cmd+Shift+P`)
2. `Calva: Connect to a Running REPL Server in the Project` を選択
3. `myskill-recruit/problem` を選択
4. プロジェクトタイプで `Generic` を選択
5. ホスト:ポートに `localhost:52004` を入力

接続できたら、Calva の REPL ウィンドウでコマンドを実行できます。

### 3. マイグレーション実行

```clojure
(migrate)
```

### 4. Seed データ投入

```clojure
(seed! "i'm sure")
```

### 5. アクセス

- ブラウザ: http://localhost:18288
- nREPL: `localhost:52004`

## ディレクトリ構成

```
├── api/                         バックエンド (Clojure)
│   ├── env/dev/user.clj         REPL ヘルパー
│   ├── resources/
│   │   ├── migrations/          DB マイグレーション
│   │   └── seed/                シードデータ
│   ├── src/todo_app/
│   │   ├── config.clj           設定
│   │   ├── core.clj             サーバー起動
│   │   ├── db.clj               DB 接続
│   │   ├── router.clj           API ルーティング
│   │   ├── db/                  DB アクセス層
│   │   ├── handlers/            HTTP ハンドラ
│   │   └── wrappers/            ミドルウェア
│   └── test/                    バックエンドテスト
├── shared/                      共通定義 (.cljc)
│   └── src/todo_app/
│       ├── routes/              ルート定義 (API + Web)
│       └── spec/                スキーマ定義 (malli)
├── web/                         フロントエンド (ClojureScript)
│   ├── env/dev/                 開発用設定 (CSS生成等)
│   ├── resources/public/        静的ファイル
│   ├── src/todo_app/
│   │   ├── core.cljs            エントリーポイント
│   │   ├── router.cljs          フロントエンドルーター
│   │   ├── views.cljs           メインビュー
│   │   ├── router/              API ルーターヘルパー
│   │   ├── state/               re-frame イベント/サブスクリプション
│   │   └── views/               UI コンポーネント
│   └── test/                    フロントエンドテスト
├── compose.yml                  Docker Compose 設定
├── deps.edn                     Clojure 依存関係
├── shadow-cljs.edn              ClojureScript ビルド設定
└── package.json                 npm 依存関係
```

## REPL コマンド

server の nREPL (port 52004) に接続して使用します。

```clojure
(migrate)            ;; マイグレーション実行
(rollback)           ;; 最後のマイグレーションをロールバック
(seed! "i'm sure")   ;; Seed データ投入
(reload)             ;; コード変更後の再読み込み
(sql-of query-fn)    ;; defquery で生成される SQL を確認
(create-migration "name")  ;; 新しいマイグレーションファイルを作成
```

## コード変更の反映

- **フロントエンド** (ClojureScript): ファイル保存時に shadow-cljs が自動でリビルドし、ブラウザに反映されます
→shadow-cljs.ednでctrl+sで反映
- **バックエンド** (Clojure): ファイル保存後、REPL で `(reload)` を実行してください

## テスト実行

既存のテストは課題に取り組む前の状態でもすべてパスします。
課題の実装中にリグレッションが発生していないか確認するために活用してください。

```bash
# バックエンドテスト
docker compose exec server clojure -M:api:test

# フロントエンドテスト (shadow-cljs のビルド完了後に実行)
docker compose exec frontend npx vitest run
```

余裕があれば、実装した機能に対するテストも追加してみてください。

- バックエンドテスト: `api/test/` 配下に kaocha で実行するテストがあります
  - `api/test/db/` は DB 接続を伴うテスト (トランザクションロールバックで分離)
- フロントエンドテスト: `web/test/` 配下に vitest で実行するテストがあります
  - malli spec のバリデーションテストなど

---

## 事前課題

以下の2つの課題に取り組んでください。難易度の目安を星で示しています。

### 課題1: API - アイテム更新ハンドラの実装 (★★☆)

**症状**: TODOアイテムの内容を編集したり、チェック状態を変更しても保存されない

**ヒント**:
- `api/src/todo_app/handlers/todo_items.clj` の `update-item` 関数を実装してください
- 同ファイル内の `create-item` や `delete-item` の実装パターンを参考にしてください
- `api/src/todo_app/db/todo_items.clj` に DB アクセス関数 `update-item!` が用意されています
- リクエストの分解パターン: `{{{item-id :item-id} :path body :body} :parameters}`

**期待動作**: アイテムの内容編集とチェック状態の変更が DB に保存される

### 課題2: フロントエンド - フィルター機能の接続 (★★☆)

**症状**: フィルターボタン(「すべて」「未完了」「完了」)はあるが、クリックしても console.log が出るだけでフィルタリングされない

**ヒント**:
- `web/src/todo_app/views/pages/todo_list.cljs` の `filter-buttons` 関数と `view` 関数を修正してください
- `web/src/todo_app/state/todo_items.cljs` に以下が定義済みです:
  - `::set-filter` イベント (フィルター状態を変更)
  - `::current-filter` サブスクリプション (現在のフィルター状態を取得)
  - `::filtered-items` サブスクリプション (フィルター済みアイテムを取得)
- ボタンの `on-click` で `rf/dispatch` を使ってフィルター状態を更新する
- `view` 関数で `::items` の代わりに `::filtered-items` を subscribe する
- 選択中のフィルターに応じてボタンのスタイルを切り替える (`$filter-btn-active`)

**期待動作**: フィルターボタンで「すべて」「未完了」「完了」の切り替えができる

---

## スキルマッチ選考当日

選考当日は、事前課題の内容に加えて、バックエンドとフロントエンドの両方にまたがる機能追加をその場で1つ取り組んでいただきます。

事前課題を素材に、以下のような点もお伺いします。

- 課題についての実装の解説、デモ
- 難しかった点、どういう調査をしたか
- こういう点が面白い、興味深い
