(require '[clojure.edn :as edn]
         '[clojure.java.browse :as browse]
         '[clojure.java.io :as io]
         '[cognitect.transit :as transit]
         '[org.httpkit.server :as srv]
         '[hiccup.core :as hp])

(import 'java.io.ByteArrayOutputStream)

(def port 8083)

(def filename "messages.txt")

(defn html [cljs-file]
  (hp/html
    [:html
     [:head
      [:meta {:charset "UTF-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
      [:link {:rel "shortcut icon" :href "data:,"}]
      [:link {:rel "apple-touch-icon" :href "data:,"}]
      [:link {:rel "stylesheet" :href "https://cdn.jsdelivr.net/npm/bulma@0.9.0/css/bulma.min.css"}]
      [:script {:crossorigin nil :src "https://unpkg.com/react@17/umd/react.production.min.js"}]
      [:script {:crossorigin nil :src "https://unpkg.com/react-dom@17/umd/react-dom.production.min.js"}]
      [:script {:src "https://cdn.jsdelivr.net/npm/scittle@0.6.15/dist/scittle.js" :type "application/javascript"}]
      [:script {:src "https://cdn.jsdelivr.net/npm/scittle@0.6.15/dist/scittle.reagent.js" :type "application/javascript"}]
      [:script {:src "https://cdn.jsdelivr.net/npm/scittle@0.6.15/dist/scittle.cljs-ajax.js" :type "application/javascript"}]
      [:title "Guestbook"]]
     [:body
      [:div {:id "content"}]
      [:script {:type "application/x-scittle" :src cljs-file}]]]))

(defn home-save-message! [req]
  (let [params (transit/read (transit/reader (:body req) :json))
        text (prn-str (assoc params :timestamp (java.util.Date.)))]
    (spit filename text :append true)
    "post success!"))

(defn db-get-messages []
  (if (.exists (io/file filename))
    (edn/read-string (str "[" (slurp filename) "]"))
    []))

(defn home-message-list [_]
  (let [out (ByteArrayOutputStream. 4096)
        writer (transit/writer out :json)]
    (transit/write writer {:messages (db-get-messages)})
    (.toString out)))

(defn home-page [_request cljs-file]
  (html cljs-file))

(defn home-routes [{:keys [:request-method :uri] :as req}]
  (case [request-method uri]
    [:get "/"] {:body (home-page req "guestbook.cljs")
                :status 200}
    [:get "/messages"] {:headers {"Content-type" "application/transit+json"}
                        :body (home-message-list req)
                        :status 200}
    [:post "/message"] {:body (home-save-message! req)
                        :status 200}
    [:get "/guestbook.cljs"] {:body (slurp"guestbook.cljs")
                             :status 200}))

(defn core-http-server []
  (srv/run-server home-routes {:port port}))

(let [url (str "http://localhost:" port "/")]
  (core-http-server)
  (println "serving" url)
  (browse/browse-url url)
  @(promise))
