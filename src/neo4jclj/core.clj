(ns neo4jclj.core
  (:require [neo4jclj.parsing :refer [neo4j->clj clj->neo4j]])
  (:import [org.neo4j.driver GraphDatabase AuthTokens Config SessionConfig]
           [org.neo4j.driver.internal.logging ConsoleLogging]
           [org.neo4j.driver.internal InternalSession InternalTransaction]
           [java.util.logging Level]))

(defrecord ^:private CustomInternalConnection [driver database])

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
                      :or {logging :warn}
                      :as opts}]
  (let [logging (ConsoleLogging. (get +log-levels+ logging))]
    (-> (Config/builder)
        (.withLogging logging)
        (.build))))

(defn connect
  ([url username password]
   (CustomInternalConnection. (GraphDatabase/driver url (AuthTokens/basic username password)) nil))
  ([url username password options]
   (let [config (build-config options)]
     (CustomInternalConnection. (GraphDatabase/driver url (AuthTokens/basic username password) config) (:database options)))))

(defn disconnect [^CustomInternalConnection conn]
  (.close (:driver conn)))

(defn create-session
  [^CustomInternalConnection {:keys [driver database] :as conn}]
  (if database
    (.session driver (SessionConfig/forDatabase database))
    (.session driver)))

(defmulti query (fn [obj & args] (class obj)))

(defmethod query CustomInternalConnection
  ([conn q-string]
   (query conn q-string {}))
  ([conn q-string param-list]
   (with-open [session (create-session conn)]
     (doall (neo4j->clj (.run session q-string (clj->neo4j param-list)))))))

(defmethod query InternalSession
  ([session q-string]
   (query session q-string {}))
  ([session q-string param-list]
   (neo4j->clj (.run session q-string (clj->neo4j param-list)))))
