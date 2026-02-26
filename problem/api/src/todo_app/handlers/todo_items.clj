(ns todo-app.handlers.todo-items
  (:require
   [todo-app.db.todo-items :as item-db]
   [ring.util.http-response :refer [not-found ok]]))

(defn list-items
  [{{{list-id :list-id} :path} :parameters}]
  (ok (item-db/get-items-by-list-id list-id)))

(defn create-item
  [{{{list-id :list-id} :path
     {:keys [content]} :body} :parameters}]
  (let [max-order (item-db/get-max-display-order-for-list list-id)
        item (item-db/insert-item!
              {:todo_list_id list-id
               :content (or content "")
               :display_order (inc max-order)})]
    (ok item)))

(defn update-item
  "
   アイテムの更新を行う関数

   args:
    - item-id: 更新対象のアイテムのID
    - content: タスクの内容(画面で登録されている文字列)
    - done: アイテムの完了状態(画面で登録されているcheckboxの状態)
   
   tips:
    - この関数はTodoリストの更新時に呼び出されcontent と done は同時には送られてこない
    - 送られてこなかった方の値は、現在のデータベースの値を使用して更新する
    (既存の値を上書きしないようにするため)
   "
  [
   {{{item-id :item-id} :path
     {content :content
      done :done} :body
    } :parameters}]
  ;; TODO: アイテムの更新ロジックを実装してください
  ;; - リクエストから item-id と body (content, done) を取得
  ;; - item-db/update-item! を呼び出して更新
  ;; - 更新成功時は ok、見つからない場合は not-found を返す
  ;; ヒント: create-item や delete-item の実装パターンを参考にしてください
  (  
    ;; 更新するアイテムの現在の値(content, done)を取得 
    let [item_record (item-db/get-item-by-id item-id)]
    (
     cond
     ;; done(checkbox)を更新した場合
     (nil? content)
      (if (item-db/update-item! {:id item-id :content (get item_record :todo_items/content) :done done})
        (ok {:message "Update successful"})
        (not-found {:message "Item not found"}))
     ;; content(タスクの内容)を更新した場合
     (nil? done)
     (if (item-db/update-item! {:id item-id :content content :done (get item_record :todo_items/done)})
        (ok {:message "Update successful"})
        (not-found {:message "Item not found"}))
     )))

(defn delete-item
  [{{{item-id :item-id} :path} :parameters}]
  (if (item-db/delete-item! {:id item-id})
    (ok {})
    (not-found {:message "Item not found"})))

(defn reorder-items
  [{{{:keys [ordered-ids]} :body} :parameters}]
  (item-db/reorder-items! ordered-ids)
  (ok {}))
