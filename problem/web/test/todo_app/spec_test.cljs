(ns todo-app.spec-test
  (:require [todo-app.vitest :refer [deftest is testing]]
            [malli.core :as m]
            [todo-app.spec.models.todo-list :as todo-list]
            [todo-app.spec.models.todo-item :as todo-item]
            [todo-app.spec.requests.todo-lists :as req-lists]
            [todo-app.spec.requests.todo-items :as req-items]))

(deftest todo-list-id-spec-test
  (testing "todo-list/idのバリデーション"
    (is (true? (m/validate todo-list/id 1)))
    (is (true? (m/validate todo-list/id 100)))
    (is (false? (m/validate todo-list/id 0)))
    (is (false? (m/validate todo-list/id -1)))
    (is (false? (m/validate todo-list/id "abc")))))

(deftest todo-list-name-spec-test
  (testing "todo-list/nameのバリデーション"
    (is (true? (m/validate todo-list/name "買い物リスト")))
    (is (false? (m/validate todo-list/name "")))
    (is (false? (m/validate todo-list/name 123)))))

(deftest todo-item-content-spec-test
  (testing "todo-item/contentのバリデーション"
    (is (true? (m/validate todo-item/content "牛乳を買う")))
    (is (true? (m/validate todo-item/content "")))
    (is (false? (m/validate todo-item/content 123)))))

(deftest create-todo-list-request-test
  (testing "create-todo-listリクエストのバリデーション"
    (is (true? (m/validate req-lists/create-todo-list {:name "新しいリスト"})))
    (is (false? (m/validate req-lists/create-todo-list {})))
    (is (false? (m/validate req-lists/create-todo-list {:name ""})))))

(deftest create-todo-item-request-test
  (testing "create-todo-itemリクエストのバリデーション"
    (is (true? (m/validate req-items/create-todo-item {})))
    (is (true? (m/validate req-items/create-todo-item {:content "タスク"})))))

(deftest update-todo-item-request-test
  (testing "update-todo-itemリクエストのバリデーション"
    (is (true? (m/validate req-items/update-todo-item {:content "更新後"})))
    (is (true? (m/validate req-items/update-todo-item {:done true})))
    (is (true? (m/validate req-items/update-todo-item {:content "更新後" :done false})))))

;; フィルター機能のテスト
(defn apply-filter 
  "アイテムリストにフィルターを適用する関数
  problem.web.src.todo_app.state.todo_items.cljs>::filtered-itemsの中身を抜き出した"
  [items filter-type]
  (case filter-type
    :active (filterv (complement :done) items)
    :done (filterv :done items)
    items))

(deftest filter-todo-items-test
  (let [items [{:id 1 :content "完了したタスク" :done true}
               {:id 2 :content "未完了のタスク1" :done false}
               {:id 3 :content "未完了のタスク2" :done false}]]
    
    (testing "フィルター機能の動作"
      (testing ":allフィルター - すべてのアイテムが返される"
        (is (= 3 (count (apply-filter items :all))))
        (is (= items (apply-filter items :all))))
      
      (testing ":activeフィルター - 未完了(:done=false)のアイテムだけが返される"
        (is (= 2 (count (apply-filter items :active))))
        (is (= [{:id 2 :content "未完了のタスク1" :done false}
                {:id 3 :content "未完了のタスク2" :done false}]
               (apply-filter items :active))))
      
      (testing ":doneフィルター - 完了(:done=true)のアイテムだけが返される"
        (is (= 1 (count (apply-filter items :done))))
        (is (= [{:id 1 :content "完了したタスク" :done true}]
               (apply-filter items :done))))
      
      (testing "空のリストでのフィルター"
        (is (= 0 (count (apply-filter [] :all))))
        (is (= 0 (count (apply-filter [] :active))))
        (is (= 0 (count (apply-filter [] :done))))))))

(deftest filter-edge-cases-test
  (testing "フィルターのエッジケース"
    (let [items [{:id 1 :content "タスク1" :done false}]]
      
      (testing "単一アイテムでのフィルター"
        (is (= 1 (count (apply-filter items :all))))
        (is (= 1 (count (apply-filter items :active))))
        (is (= 0 (count (apply-filter items :done)))))
      
      (testing "すべて完了している場合"
        (let [all-done [{:id 1 :done true} {:id 2 :done true}]]
          (is (= 2 (count (apply-filter all-done :all))))
          (is (= 0 (count (apply-filter all-done :active))))
          (is (= 2 (count (apply-filter all-done :done))))))
      
      (testing "すべて未完了の場合"
        (let [all-active [{:id 1 :done false} {:id 2 :done false}]]
          (is (= 2 (count (apply-filter all-active :all))))
          (is (= 2 (count (apply-filter all-active :active))))
          (is (= 0 (count (apply-filter all-active :done)))))))))
