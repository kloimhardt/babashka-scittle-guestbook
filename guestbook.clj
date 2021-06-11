(require '[clojure.edn :as edn]
         '[clojure.java.browse :as browse]
         '[clojure.java.io :as io]
         '[cognitect.transit :as transit]
         '[org.httpkit.server :as srv]
         '[hiccup.core :as hp])

(import 'java.time.format.DateTimeFormatter
        'java.time.LocalDateTime
        'java.io.ByteArrayOutputStream)

(def port 8083)

#_(def bb-web-js (slurp "bb_web.js"))

(defn html [cljs-code]
  (str
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

        [:script {:src "https://borkdude.github.io/scittle/js/scittle.js"  :type "application/javascript"}]
        [:script {:src "https://borkdude.github.io/scittle/js/scittle.cljs-ajax.js" :type "application/javascript"}]
        [:script {:src "https://borkdude.github.io/scittle/js/scittle.reagent.js" :type "application/javascript"}]

        [:title "Guestbook"]]
       [:body
        [:div {:id "content"}]
        [:script {:type "application/x-scittle"}
          "(defn my-alert []
       (js/alert \"You clicked!\"))
      ;; export function to use from JavaScript:
      (set! (.-my_alert js/window) my-alert)"]
        [:button {:onclick  "my_alert()"} "hu"]
        [:script {:type "application/x-scittle"}
         cljs-code]]])))

#_(defn html [cljs-code]
  (println (html7 nil))
  (str "
  <!DOCTYPE html>
  <html>
  <head>
  <meta charset=\"UTF-8\">
  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">
  <link rel=\"shortcut icon\" href=\"data:,\">
  <link rel=\"icon\" href=\"data:,\">
  <link rel=\"apple-touch-icon\" href=\"data:,\">
  <link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/bulma@0.9.0/css/bulma.min.css\">
  <script>"
       bb-web-js
       "</script>
  <title>Guestbook 1</title>
  </head>
  <body>
  <div id=\"cljs-app\">"
       cljs-code
       "</div>
  <script>bb_web.app.run(\"cljs-app\")</script>
  </body>
  </html>"))

(defn date [] (LocalDateTime/now))

(def formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss"))

(def filename "m.txt")

(defn readfile []
  (if (.exists (io/file filename))
    (edn/read-string (slurp filename))
    {:messages []}))

(defn db-save-message! [params]
  (->> formatter
       (.format (date))
       (assoc params :timestamp)
       (update (readfile) :messages conj)
       pr-str
       (spit filename)))

(defn db-get-messages []
  (:messages (readfile)))

(defn home-save-message! [req]
  (let [params (transit/read (transit/reader (:body req) :json))]
    (db-save-message! params)
    "post success!"))

(defn home-message-list [_]
  (let [out (ByteArrayOutputStream. 4096)
        writer (transit/writer out :json)]
    (transit/write writer {:messages (vec (db-get-messages))})
    (.toString out)))

(def cmd-line-args *command-line-args*)

(defn home-page [request] (html (slurp (first cmd-line-args))))

(defn home-routes [{:keys [:request-method :uri] :as req}]
  (case [request-method uri]
    [:get "/"] {:body (home-page req)
                :status 200}
    [:get "/messages"] {:headers {"Content-type" "application/transit+json"}
                        :body (home-message-list req)
                        :status 200}
    [:post "/message"] {:body (home-save-message! req)
                        :status 200}))

(defn core-http-server []
  (srv/run-server home-routes {:port port}))

(let [url (str "http://localhost:" port "/")]
  (core-http-server)
  (println "serving" url)
  (browse/browse-url url)
  @(promise))
