(ns neo4jclj.core
  (:require [neo4jclj.parsing :refer [neo4j->clj]])
  (:import [org.neo4j.driver GraphDatabase AuthTokens Config SessionConfig]
           [org.neo4j.driver.internal.logging ConsoleLogging]
           [org.neo4j.driver.internal InternalDriver InternalSession InternalTransaction]
           [java.util.logging Level]))

(def ^:private +log-levels+
  "For convenience, represting Java logging levels in terms of clojure keywords"
  {:all Level/ALL
   :report Level/SEVERE
   :warn Level/WARNING
   :info Level/INFO
   :debug Level/FINEST
   :trace Level/FINE
   :off Level/OFF})

(defn- build-config [{:keys [logging]
                      :as opts}]
  (let [logging (ConsoleLogging. (get +log-levels+ logging :warn))]
    (-> (Config/builder)
        (.withLogging logging)
        (.build))))

(defn connect
  ([url username password]
   (GraphDatabase/driver url (AuthTokens/basic username password)))
  ([url username password options]
   (let [config (build-config options)]
     (GraphDatabase/driver url (AuthTokens/basic username password) config))))

(defn disconnect [conn]
  (.close conn))

(defn create-session
  ([conn]
   (.session conn))
  ([conn db]
   (.session conn (SessionConfig/forDatabase db))))

(defn- query-using-driver
  [conn q-string param-list]
  (let [session (create-session conn)]
    (neo4j->clj (.run session q-string param-list))))

(defn- query-using-session
  [session q-string param-list]
  (neo4j->clj (.run session q-string param-list)))

(defn- query-using-transaction [transaction q-string param-list])

(defn query
  ([obj q-string]
   (query obj q-string {}))
  ([obj q-string param-list]
   (condp = (class obj)
     InternalDriver (query-using-driver obj q-string param-list)
     InternalSession (query-using-session obj q-string param-list)
     InternalTransaction (query-using-transaction obj q-string param-list))))

