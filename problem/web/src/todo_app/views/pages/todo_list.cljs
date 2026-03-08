(ns todo-app.views.pages.todo-list
  (:require
   ["react" :as react]
   [todo-app.state.todo-items :as todo-items]
   [todo-app.state.todo-lists :as todo-lists]
   [re-frame.core :as rf]
   [shadow.css :refer [css]]))

(def $container
  (css :p-6 :h-full :flex :flex-col))

(def $title-area
  (css :flex :items-center :mb-6))

(def $title
  (css :text-xl :font-bold :text-black-600 :cursor-pointer
       {:border "2px solid transparent"
        :padding "4px 8px"
        :border-radius "4px"
        :min-width "200px"}
       ["&:hover" {:border-color "#dadee5"}]))

(def $title-input
  (css :text-xl :font-bold :text-black-600
       {:border "2px solid #1a8bf0"
        :padding "4px 8px"
        :border-radius "4px"
        :outline "none"
        :min-width "200px"}))

(def $items-area
  (css :flex-1 :overflow-auto))

(def $item
  (css :flex :items-center :py-2 :px-3 :mb-1 :rounded
       {:border "1px solid #e6e9ed"
        :background "#fff"
        :transition "box-shadow 0.15s"}
       ["&:hover" {:box-shadow "0 2px 4px rgba(0,0,0,0.08)"}]))

(def $drag-handle
  (css :cursor-grab :mr-2 :text-gray-500 :select-none
       {:font-size "16px"}))

(def $checkbox
  (css :mr-3 :cursor-pointer
       {:width "18px" :height "18px" :accent-color "#1a8bf0"}))

(def $content
  (css :flex-1 :text-sm :text-black-500 :cursor-pointer
       {:padding "2px 4px"
        :border "1px solid transparent"
        :border-radius "2px"}
       ["&:hover" {:border-color "#dadee5"}]))

(def $content-done
  (css {:text-decoration "line-through"
        :color "#8b98ac"}))

(def $content-input
  (css :flex-1 :text-sm :text-black-500
       {:padding "2px 4px"
        :border "1px solid #1a8bf0"
        :border-radius "2px"
        :outline "none"}))

(def $due-date
  (css :ml-2 :text-xs
       {:padding "2px 6px"
        :border "1px solid #e6e9ed"
        :border-radius "4px"
        :color "#5a6577"
        :background "transparent"
        :cursor "pointer"
        :min-width "110px"}
       ["&::-webkit-calendar-picker-indicator" {:cursor "pointer"}]))

(def $due-date-overdue
  (css {:color "#d52727"
        :border-color "#f77474"}))

(def $menu-btn
  (css :ml-2 :cursor-pointer :text-gray-500 :relative
       {:font-size "16px"
        :padding "4px 8px"
        :border "none"
        :background "transparent"
        :border-radius "4px"}
       ["&:hover" {:background "#f1f3f5"}]))

(def $menu-dropdown
  (css :absolute :right-0 :top-full :mt-1 :rounded :shadow-md
       {:background "#fff"
        :border "1px solid #e6e9ed"
        :z-index "10"
        :min-width "100px"}))

(def $menu-item
  (css :px-3 :py-2 :text-sm :cursor-pointer :text-red-500
       ["&:hover" {:background "#ffecec"}]))

(def $add-btn
  (css :mt-3 :py-2 :px-4 :text-sm :cursor-pointer :rounded :text-blue-600
       {:background "transparent"
        :border "1px dashed #c5cdda"
        :width "100%"
        :transition "background-color 0.15s"}
       ["&:hover" {:background "#e3f4fe"}]))

(def $delete-list-btn
  (css :ml-auto :py-1 :px-3 :text-sm :cursor-pointer :rounded :text-red-500
       {:background "transparent"
        :border "1px solid #f77474"
        :transition "background-color 0.15s"}
       ["&:hover" {:background "#ffecec"}]))

(def $filter-area
  (css :flex :items-center :gap-2 :mb-4))

(def $filter-btn
  (css :py-1 :px-3 :text-sm :cursor-pointer :rounded
       {:background "transparent"
        :border "1px solid #c5cdda"
        :color "#5a6577"
        :transition "all 0.15s"}
       ["&:hover" {:background "#f1f3f5"}]))

(def $filter-btn-active
  (css {:background "#1a8bf0"
        :border-color "#1a8bf0"
        :color "#fff"}
       ["&:hover" {:background "#1578d3"}]))

(defn- title-view
  "
  タイトルの表示と編集を行う関数
  args:
    - list-name: 現在のリストの名前
    - list-id: リストのID
  "
  [list-name list-id]
  (let [[editing? set-editing!] (react/useState false)]
    (if editing?
      [:input {:class [$title-input]
               :auto-focus true
               :default-value list-name
               :on-blur (fn [e]
                          (set-editing! false)
                          (let [v (.. e -target -value)]
                            (when (and (not= v list-name) (not= v ""))
                              (rf/dispatch [::todo-lists/update-list list-id v]))))
               :on-key-down (fn [e]
                              (when (= (.-key e) "Enter")
                                (.blur (.-target e))))}]
      [:div {:class [$title]
             :on-click #(set-editing! true)}
       list-name])))

(defn- item-menu
  [item-id]
  (let [[open? set-open!] (react/useState false)]
    [:div {:style {:position "relative" :display "inline-block"}}
     [:button {:class [$menu-btn]
               :on-click #(set-open! not)}
      "\u22EE"]
     (when open?
       [:div {:class [$menu-dropdown]
              :on-mouse-leave #(set-open! false)}
        [:div {:class [$menu-item]
               :on-click (fn [_]
                           (set-open! false)
                           (rf/dispatch [::todo-items/delete-item item-id]))}
         "削除"]])]))

(defn- item-content
  [{:keys [id content done]}]
  (let [
        [editing? set-editing!] (react/useState false)
        [content set-content!] (react/useState content)
        ]
    (if editing?
      [:input {:class [$content-input]
               :auto-focus true
               :default-value content
               :on-blur (fn [e]
                          (set-editing! false)
                          (let [v (.. e -target -value)]
                            (when (not= v content)
                              (set-content! v)
                              (rf/dispatch [::todo-items/update-item id {:content v}]))))
               :on-key-down (fn [e]
                              (when (= (.-key e) "Enter")
                                (.blur (.-target e))))}]
      [:span {:class [$content (when done $content-done)]
              :on-click #(set-editing! true)}
       content])))

(defn- item-due-date
  [_item]
  [:input {:type "date"
           :class [$due-date]
           :value ""
           :on-change (fn [e]
                        (js/console.log "due-date changed:" (.. e -target -value)))}])

(defn- item-view
  "
   itemの表示設定を行う関数
    args:
      - item: アイテムのデータ (id, content, done など)
      - on-drag-start: ドラッグ開始時のコールバック関数
      - on-drop: ドロップ時のコールバック関数
  "
  [item on-drag-start on-drop]
  ; useState(ある変数とそれをレンダリングする関数を返してくれる関数)
  ; [入れたい変数 書き換える関数] (react/useState 初期値)
  (let [[done? set-done!] (react/useState (:done item))]
    [:div {:class [$item]
           :draggable true
           :on-drag-start #(on-drag-start (:id item) %)
           :on-drag-over #(.preventDefault %)
           :on-drop #(on-drop (:id item) %)}
     [:span {:class [$drag-handle]} "\u2261"]
     [:input {:type "checkbox"
              :class [$checkbox]
              :checked done?
              :on-change (fn [_]
                           (let [new-done (not done?)]
                             (set-done! new-done)
                             (rf/dispatch [::todo-items/update-item
                                           (:id item)
                                           {:done new-done}])))}]
     ^{:key (str "content-" (:id item))}
     [item-content item]
     ^{:key (str "due-date-" (:id item))}
     [item-due-date item]
     ^{:key (str "menu-" (:id item))}
     [item-menu (:id item)]]))


(defn- filter-buttons
  []
  ;; TODO: フィルターボタンを re-frame のイベント/サブスクリプションと接続してください
  ;; 現状: ボタンをクリックすると console.log が出力されるだけ
  ;; 期待: フィルター状態に応じてアイテム表示が絞り込まれること
  ;; ヒント:
  ;; - todo-app.state.todo-items に ::set-filter, ::current-filter, ::filtered-items が定義済み
  ;; - on-click で rf/dispatch を使ってフィルター状態を更新する
  ;; - 選択中のフィルターに応じてボタンのスタイルを切り替える ($filter-btn-active)
  (let [current-filter @(rf/subscribe [::todo-items/current-filter])]
    [:div {:class [$filter-area]}
     (for [[filter-key label] [[:all "すべて"] [:active "未完了"] [:done "完了"]]]
       ^{:key filter-key}
       [:button {:class [$filter-btn (when (= current-filter filter-key) $filter-btn-active)]
                 :on-click (fn [_] (rf/dispatch [::todo-items/set-filter filter-key]))}
        label])]))

(defn view
  [match]
  (let [list-id (-> match :parameters :path :list-id)
        lists @(rf/subscribe [::todo-lists/lists])
        items @(rf/subscribe [::todo-items/filtered-items])
        current-list (some #(when (= (:id %) list-id) %) lists)
        drag-source (react/useRef nil)]
    [:div {:class [$container]}
     (when current-list
       [:<>
        [:div {:class [$title-area]}
         ^{:key (str "title-" list-id)}
         [title-view (:name current-list) list-id]
         [:button {:class [$delete-list-btn]
                   :on-click (fn [_]
                               (when (js/confirm "このリストを削除しますか？")
                                 (rf/dispatch [::todo-lists/delete-list list-id])
                                 (set! (.-href js/window.location) "/")))}
          "リスト削除"]]
        [filter-buttons]
        [:div {:class [$items-area]}
         (for [item items]
           ^{:key (:id item)}
           [item-view item
            (fn [id _e] (set! (.-current drag-source) id))
            (fn [target-id _e]
              (when-let [source-id (.-current drag-source)]
                (when (not= source-id target-id)
                  (let [ids (mapv :id items)
                        source-idx (.indexOf ids source-id)
                        target-idx (.indexOf ids target-id)
                        without-source (into (subvec ids 0 source-idx)
                                             (subvec ids (inc source-idx)))
                        reordered (into (subvec without-source 0 target-idx)
                                        (cons source-id (subvec without-source target-idx)))]
                    (rf/dispatch [::todo-items/reorder-items reordered])))))])]
        [:button {:class [$add-btn]
                  :on-click #(rf/dispatch [::todo-items/create-item ""])}
         "+ TODOを追加"]])]))
