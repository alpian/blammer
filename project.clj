(defproject blammer "0.1.0-SNAPSHOT"
  
  :description "Blammer: analysing the chatter"
  
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [clj-http "0.5.5"]
                 [org.clojure/data.json "0.1.3"]
                 [ring/ring-core "1.1.2"]
                 [ring/ring-jetty-adapter "1.1.2"]
                 [compojure "1.1.1"]
                 [hiccup "1.0.1"]]
  
  :main blammer.core)
