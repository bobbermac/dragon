(ns dragon.data.sources.core
  (:require [clojure.java.shell :as shell]
            [dragon.config.core :as config]
            [dragon.data.sources.impl.common :as common]
            [dragon.data.sources.impl.datomic :as datomic]
            [dragon.data.sources.impl.redis :as redis]
            [dragon.util :as util]
            [taoensso.timbre :as log])
  (:import (dragon.data.sources.impl.datomic DatomicConnector
                                             DatomicQuerier)
           (dragon.data.sources.impl.redis RedisConnector
                                           RedisQuerier)))

(defprotocol DBConnector
  (start-db! [this])
  (execute-db-command! [this])
  (setup-schemas [this])
  (setup-subscribers [this])
  (add-connection [this])
  (remove-connection [this])
  (stop-db! [this]))

(extend DatomicConnector
        DBConnector
        (merge common/connection-behaviour
               datomic/connection-behaviour))

(extend RedisConnector
        DBConnector
        (merge common/connection-behaviour
               redis/connection-behaviour))

(defn new-connector
  [component]
  (case (config/db-type component)
    :redis (redis/new-connector component)
    :datomic (datomic/new-connector component)))

(defprotocol DBQuerier
  (get-post-checksum [this post-key])
  (get-post-content [this post-key])
  (get-post-metadata [this post-key])
  (get-post-tags [this post-key])
  (get-post-stats [this post-key])
  (get-all-categories [this])
  (get-all-tags [this])
  (get-all-stats [this])
  (post-changed? [this post-key])
  (save-post [this data]))

(extend DatomicQuerier
        DBQuerier
        datomic/query-behaviour)

(extend RedisQuerier
        DBQuerier
        redis/query-behaviour)

(defn new-querier
  [component]
  (case (config/db-type component)
    :redis (redis/new-querier component)
    :datomic (datomic/new-querier component)))
