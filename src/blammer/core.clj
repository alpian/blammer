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
(def messages-url "https://www.yammer.com/api/v1/messages.json")

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
   (map 
     (fn [user] [:tr [:td (:first_name user)] [:td (:last_name user)] [:td (:kewlness user)]]) 
     users)])

(defn make-routes [] 
  (wrap-params 
    (defroutes blammer-routes
      (GET "/" [] 
          {:status 200
           :headers { "Content-Type" "text/html;charset=UTF-8" } 
           :body (html
                   [:head (include-css "/css/style.css")]
                   [:body [:h1 "The Kewlness"] (user-table (kewlness-rank))])})
      (resources "/"))))

(defn http-server [routes port] 
  (run-jetty routes {:port port :join? false}))

(defn start-http-server [port] (.start (http-server (make-routes) port)))

(defn -main [& args]
  (start-http-server 8082))
