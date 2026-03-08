(ns todo-app.db.todo-items-test
  (:require
   [clojure.test :refer [deftest is use-fixtures]]
   [todo-app.db.todo-items :as todo-items]
   [todo-app.handlers.todo-items :as todo-items-api]
   [todo-app.db.todo-lists :as todo-lists]
   [todo-app.test-helpers :refer [testing-with-rollback with-rollback with-test-database]]))

(use-fixtures :once with-test-database)
(use-fixtures :each with-rollback)

(defn- create-test-list!
  []
  (todo-lists/insert-todo-list! {:name "テストリスト" :display_order 0}))

(deftest get-items-by-list-id-test
  (testing-with-rollback "リストにアイテムがない場合、空ベクタを返す"
                         (let [list (create-test-list!)]
                           (is (= [] (todo-items/get-items-by-list-id (:id list)))))))

(deftest insert-item-test
  (testing-with-rollback "アイテムを作成できる"
                         (let [list (create-test-list!)
                               item (todo-items/insert-item! {:todo_list_id (:id list)
                                                              :content "テストアイテム"
                                                              :display_order 0})]
                           (is (some? (:id item)))
                           (is (= "テストアイテム" (:content item)))
                           (is (= false (:done item)))))

  (testing-with-rollback "空のcontentでアイテムを作成できる"
                         (let [list (create-test-list!)
                               item (todo-items/insert-item! {:todo_list_id (:id list)
                                                              :display_order 0})]
                           (is (= "" (:content item))))))

(deftest update-item-test
  (testing-with-rollback "アイテムの内容を更新できる"
                         (let [list (create-test-list!)
                               item (todo-items/insert-item! {:todo_list_id (:id list)
                                                              :content "元の内容"
                                                              :display_order 0})
                               updated (todo-items/update-item! {:id (:id item)
                                                                 :content "新しい内容"})]
                           (is (= "新しい内容" (:content updated)))))

  (testing-with-rollback "アイテムの完了状態を更新できる"
                         (let [list (create-test-list!)
                               item (todo-items/insert-item! {:todo_list_id (:id list)
                                                              :content "タスク"
                                                              :display_order 0})
                               updated (todo-items/update-item! {:id (:id item)
                                                                 :done true})]
                           (is (= true (:done updated))))))

(deftest delete-item-test
  (testing-with-rollback "アイテムを削除できる"
                         (let [list (create-test-list!)
                               item (todo-items/insert-item! {:todo_list_id (:id list)
                                                              :content "削除対象"
                                                              :display_order 0})]
                           (todo-items/delete-item! {:id (:id item)})
                           (is (= [] (todo-items/get-items-by-list-id (:id list)))))))

(deftest reorder-items-test
  (testing-with-rollback "アイテムの並び替えができる"
                         (let [list (create-test-list!)
                               i1 (todo-items/insert-item! {:todo_list_id (:id list) :content "A" :display_order 0})
                               i2 (todo-items/insert-item! {:todo_list_id (:id list) :content "B" :display_order 1})
                               i3 (todo-items/insert-item! {:todo_list_id (:id list) :content "C" :display_order 2})]
                           (todo-items/reorder-items! [(:id i3) (:id i1) (:id i2)])
                           (let [items (todo-items/get-items-by-list-id (:id list))]
                             (is (= ["C" "A" "B"] (mapv :content items)))))))

(deftest update-item-api-test
  (testing-with-rollback "apiでアイテムの内容を更新できる"
                         (let [list (create-test-list!)
                               item (todo-items/insert-item! {:todo_list_id (:id list)
                                                              :content "元の内容"
                                                              :display_order 0})]
                           (todo-items-api/update-item {:parameters
                                                        {:path {:item-id (:id item)}
                                                         :body {:content "新しい内容"}}})
                           (is (= "新しい内容" (:todo_items/content (todo-items/get-item-by-id (:id item)))))))

  (testing-with-rollback "apiでアイテムの完了状態を更新できる"
                         (let [list (create-test-list!)
                               item (todo-items/insert-item! {:todo_list_id (:id list)
                                                              :content "タスク"
                                                              :display_order 0})]
                           (todo-items-api/update-item {:parameters 
                                                        {:path {:item-id (:id item)}
                                                         :body {:done true}}})
                           (is (= true (:todo_items/done (todo-items/get-item-by-id (:id item))))))))