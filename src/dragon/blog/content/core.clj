(ns dragon.blog.content.core
  (:require [dragon.blog.content.rfc5322 :as rfc5322]
            [dragon.even.system.core as event]
            [dragon.event.tag :as tag]
            [trifl.fs :as fs]))

(defn parse
  [file-obj]
  (event/publish system tag/read-source-pre {:file file-obj})
  (let [file-type (fs/extension file-obj)
        content (slurp file-obj)]
    (event/publish system tag/read-source-post {:file file-obj})
    (event/publish system tag/parse-file-pre {:file-content content})
    (assoc
     (case file-type
       :rfc5322 (rfc5322/parse content)
       :default {:raw-data content
                 :error :parser-not-found})
     :file-type file-type)))
