(ns neo4jclj.parsing
  (:require [clojure.walk :as walk])
  (:import [org.neo4j.driver.internal
            InternalRecord
            InternalPair
            InternalRelationship
            InternalNode
            InternalResult]
           [org.neo4j.driver.internal.value
            NodeValue
            NullValue
            ListValue
            MapValue
            RelationshipValue
            StringValue
            BooleanValue
            NumberValueAdapter
            ObjectValueAdapter]
           [java.util Map List]
           [clojure.lang ISeq]))

(defmulti neo4j->clj class)

(defn clj->neo4j [params]
  (walk/stringify-keys params))

(defn transform [m]
  (let [f (fn [[k v]]
            [(keyword k) (neo4j->clj v)])]
    (walk/postwalk
     (fn [x]
       (if (or (map? x) (instance? Map x))
         (with-meta (into {} (map f x))
           (meta x))
         x))
     m)))

(defmethod neo4j->clj NodeValue [value]
  (transform (into {} (.asMap value))))

(defmethod neo4j->clj RelationshipValue [value]
  (transform (into {} (.asMap (.asRelationship value)))))

(defmethod neo4j->clj MapValue [l]
  (transform (into {} (.asMap l))))

(defmethod neo4j->clj Map [m]
  (transform (into {} m)))

(defmethod neo4j->clj InternalResult [record]
  (map neo4j->clj (iterator-seq record)))

(defmethod neo4j->clj InternalRecord [record]
  (apply merge (map neo4j->clj (.fields record))))

(defmethod neo4j->clj InternalPair [pair]
  (let [k (-> pair .key keyword)
        v (-> pair .value neo4j->clj)]
    {k v}))

(defmethod neo4j->clj StringValue [v]
  (.asObject v))

(defmethod neo4j->clj ObjectValueAdapter [v]
  (.asObject v))

(defmethod neo4j->clj BooleanValue [v]
  (.asBoolean v))

(defmethod neo4j->clj NumberValueAdapter [v]
  (.asNumber v))

(defmethod neo4j->clj ListValue [l]
  (map neo4j->clj (into [] (.asList l))))

(defmethod neo4j->clj ISeq [s]
  (map neo4j->clj s))

(defmethod neo4j->clj InternalNode [n]
  (with-meta (transform (into {} (.asMap n)))
             {:labels (.labels n)
              :id     (.id n)}))

(defmethod neo4j->clj InternalRelationship [r]
  (neo4j->clj (.asValue r)))

(defmethod neo4j->clj NullValue [n]
  nil)

(defmethod neo4j->clj List [l]
  (map neo4j->clj (into [] l)))

(defmethod neo4j->clj :default [x]
  x)
