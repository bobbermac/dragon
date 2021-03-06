(ns dragon.config.core
  (:require [dragon.components.core :as components]
            [dragon.config.defaults :as default]
            [dragon.util :as util]
            [leiningen.core.project :as project]
            [taoensso.timbre :as log])
  (:refer-clojure :exclude [name read]))

(defn build
  ""
  []
  (util/deep-merge
   default/config
   (:dragon (project/read))))

(defn domain
  [system]
  (components/get-config system :domain))

(defn domain-urn
  [system]
  (->> system
       (domain)
       (util/dots->dashes)
       (format "urn:%s")))

(defn name
  [system]
  (components/get-config system :name))

(defn description
  [system]
  (components/get-config system :description))

(defn port
  [system]
  (components/get-config system :port))

(defn output-dir
  [system]
  (components/get-config system :output-dir))

(defn output-file-tmpl
  [system]
  (components/get-config system :output-file-tmpl))

(defn base-path
  [system]
  (components/get-config system :base-path))

(defn posts-path
  [system]
  (components/get-config system :posts-path))

(defn posts-path-src
  [system]
  (components/get-config system :posts-path-src))

(defn feed-count
  [system]
  (components/get-config system :feed-count))

(defn link-tmpl
  [system]
  (components/get-config system :link-tmpl))

(defn cli
  [system]
  (components/get-config system :cli))

(defn log-level
  [system]
  (components/get-config system :cli :log-level))

(defn log-nss
  [system]
  (components/get-config system :cli :log-nss))

(defn repl
  [system]
  (components/get-config system :repl))

(defn repl-log-level
  [system]
  (components/get-config system :repl :log-level))

(defn repl-log-nss
  [system]
  (components/get-config system :repl :log-nss))

(defn app
  [system]
  (components/get-config system :app))

(defn app-log-level
  [system]
  (components/get-config system :app :log-level))

(defn app-log-nss
  [system]
  (components/get-config system :app :log-nss))

(defn templating
  [system]
  (components/get-config system :templating))

(defn template-skip-marker
  [system]
  (components/get-config system :templating :skip-marker))

(defn parsing
  [system]
  (components/get-config system :parsing))

(defn word-separator
  [system]
  (components/get-config system :parsing :word-separator))

(defn word-joiner
  [system]
  (components/get-config system :parsing :word-joiner))

(defn paragraph-separator
  [system]
  (components/get-config system :parsing :paragraph-separator))

(defn tag-separator
  [system]
  (components/get-config system :parsing :tag-separator))

(defn sentence-end
  [system]
  (components/get-config system :parsing :sentence-end))

(defn ellipsis
  [system]
  (components/get-config system :parsing :ellipsis))

(defn period-ellipsis
  [system]
  (components/get-config system :parsing :period-ellipsis))

(defn db
  [system]
  (components/get-config system :db))

(defn db-type
  [system]
  (components/get-config system :db :type))

(defn db-config
  [system]
  ((db-type system) (db system)))

(defn db-conn
  [system]
  (:conn (db-config system)))

(defn db-start-config
  [system]
  (:start (db-config system)))

(defn db-version
  [system]
  (:version (db-config system)))

(defn db-start-delay
  [system]
  (get-in (db-config system) [:start :delay]))

(defn db-start-retry
  [system]
  (get-in (db-config system) [:start :retry-delay]))

(defn db-start-retry-timeout
  [system]
  (get-in (db-config system) [:start :retry-timeout]))

(defn processor-type
  [system]
  (components/get-config system :processor :type))

(defn processor-constructor
  [system]
  (components/get-config system :processor :constructor))
