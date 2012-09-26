(ns blammer.core
  (:require [clj-http.client :as client]
            [clojure.data.json :as json])
  (:use [ring.adapter.jetty :only [run-jetty]]
        [compojure.core :only [GET POST PUT context defroutes]]
        [compojure.route :only [resources]]
        [ring.middleware.params :only [wrap-params]]
        [hiccup.core :only [html]]
        [hiccup.page :only [include-css]]))

(def access-token "")
(def users-url "https://www.yammer.com/api/v1/users.json")

(defn get-users-by-page [page]
  (let [url (str users-url "?access_token=" access-token "&page=" page)]
    (println "Hitting:" url)
    (json/read-json (:body (client/get url)))))

(defn list-all-users []
  (loop [users-so-far [] 
         page 1]
    (let [page-users (get-users-by-page page)]
      (if (empty? page-users)
        users-so-far
        (recur (concat users-so-far page-users) (inc page))))))

(defn kewlness [user]
  (let [user-stats (:stats user)] 
    (* (- (:followers user-stats) (:following user-stats)) (:updates user-stats))))

(defn add-kewlness-to-users [users]
  (map #(assoc % :kewlness (kewlness %)) users))

(defn kewlness-rank []
  (->> (list-all-users) 
           add-kewlness-to-users
           (sort-by (comp - :kewlness))))

(defn user-table [users]
  [:table 
   [:tr [:th "Rank"] [:th "Name"] [:th "Kewlness"] [:th "Followers"] [:th "Following"] [:th "Updates"]]
   (map 
     (fn [user rank] [:tr 
                 [:td rank]
                 [:td (:first_name user) " " (:last_name user)] 
                 [:td (:kewlness user)]
                 [:td (-> user :stats :followers)]
                 [:td (-> user :stats :following)]
                 [:td (-> user :stats :updates)]]) 
     users
     (map inc (range)))])

(defn make-routes [] 
  (wrap-params 
    (defroutes blammer-routes
      (GET "/" [] 
          {:status 200
           :headers { "Content-Type" "text/html;charset=UTF-8" } 
           :body (html
                   [:head (include-css "/css/style.css")]
                   [:body 
                    [:h1 "The Kewlness"]
                    [:p [:em "kewlness = (followers - following) * updates"]]
                    (user-table (kewlness-rank))])})
      (resources "/"))))

(defn -main [& args]
  (let [port (Integer/parseInt (first args))] 
    (.start (run-jetty (make-routes) {:port port :join? false}))))
