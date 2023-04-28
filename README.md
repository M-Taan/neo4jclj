# neo4jclj
A clojure wrapper around the neo4j java library

# Getting Started
~~~clojure
(require '[neo4jclj.core :as neo4j])

;; Create a connection
(def connection (neo4j/connect "bolt://localhost:7687" "neo4j" "mypassword"))

;; You can query neo4j using the connection
(neo4j/query connection "match (n {id: $id}) return n" {:id "1"})

;; You can query neo4j using a session
(def session (neo4j/create-session connection))

(neo4j/query session "match (n {id: $id}) return n" {:id "1"})

;; Closing the connection
(neo4j/disconnect connection)
~~~

# Version Matrix
| neo4jclj | Clojure | Java   | Neo4j Java Driver | Neo4j |
|----------|---------|--------|-------------------|-------|
| 1.0.0    | 1.11.1  | 17.x.x | 5.x.x             | 5.x   |

# Acknowledgements
neo4jclj has been inspired by the work of the following project(s):

[neo4j-clj](https://github.com/gorillalabs/neo4j-clj)

