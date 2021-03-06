(ns dragon.blog.post.impl.default
  (:require [clojure.string :as string]
            [dragon.blog.content.core :as content]
            [dragon.blog.content.core :as content]
            [dragon.blog.post.util :as post-util]
            [dragon.config.core :as config]
            [dragon.event.system.core :as event]
            [dragon.event.tag :as tag]
            [dragon.util :as util]
            [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Data Transforms   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-post-data
  ""
  [this data]
  (let [file (:file data)]
    (log/infof "Adding post data for '%s' ..." file)
    (->> file
         (content/parse (:system this))
         (merge data)
         (post-util/copy-original-body))))

(defn get-post-file-data
  [this data]
  (log/debug "Adding file data ...")
  (let [file-obj (:file data)
        file-src (.getPath file-obj)
        filename-old (.getName file-obj)
        filename (format (config/output-file-tmpl (:system this))
                         (util/sanitize-str (:title data)))]
    (assoc data
           :filename filename
           :src-file file-src
           :src-dir (.getParent file-obj)
           :uri-path (-> file-src
                         (string/replace filename-old filename)
                         (util/sanitize-post-path)
                         (string/replace-first "posts/" "")))))

(defn get-post-counts
  [this data]
  (log/debug "Adding counts ...")
  (let [body (:body data)]
    (log/trace "Body data:" data)
    (assoc
      data
      :char-count (util/count-chars body)
      :word-count (util/count-words body)
      :line-count (util/count-lines body))))

(defn get-post-link
  [this data]
  (log/debug "Adding post link ...")
  (let [system (:system this)
        url (str (config/posts-path system) "/" (:uri-path data))
        link (format (config/link-tmpl system) url (:title data))]
    (assoc data :url url
                :link link)))

(defn get-post-dates
  [this data]
  (log/debug "Adding post dates ...")
  (let [date (util/path->date (:src-file data))
        timestamp (util/format-timestamp date)
        timestamp-clean (string/replace timestamp #"[^\d]" "")
        datestamp (util/format-datestamp date)]
    (assoc
      data
      :date date
      :month (util/month->name (:month date))
      :month-short (util/month->short-name (:month date))
      :timestamp timestamp
      :timestamp-long (Long/parseLong timestamp-clean)
      :datestamp datestamp
      :now-timestamp (util/format-timestamp (util/datetime-now))
      :now-datestamp (util/format-datestamp (util/datetime-now)))))

(defn get-post-tags
  [this data]
  (log/debug "Updating tags ...")
  (assoc data :tags (apply sorted-set (string/split
                                       (:tags data)
                                       (config/tag-separator
                                        (:system this))))))

(defn get-post-excerpts
  [this data]
  (log/debug "Converting post body ...")
  (let [system (:system this)
        paragraphs (string/split
                    (:body data) (config/paragraph-separator system))
        words (-> paragraphs
                  first
                  (string/split (config/word-separator system)))
        words-100 (take 100 words)
        excerpt-100 (post-util/join-excerpt system words-100 100)
        excerpt-50 (post-util/join-excerpt system words-100 50)
        excerpt-25 (post-util/join-excerpt system words-100 25)]
    (assoc data
           :excerpt-100 (post-util/md->html system excerpt-100)
           :excerpt-50 (post-util/md->html system excerpt-50)
           :excerpt-25 (post-util/md->html system excerpt-25))))

(defn get-post-body
  [this data]
  (log/debug "Converting post body ...")
  (let [system (:system this)]
    (event/publish system tag/parse-content-pre {:body (:body data)})
    (->> data
         :content-type
         keyword
         (post-util/convert-body! system data)
         (event/publish->> system
                           tag/parse-content-post
                           {:body (:body data)}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord DefaultBlogPostProcessor [system])

(def behaviour
  {:get-post-data get-post-data
   :get-post-file-data get-post-file-data
   :get-post-counts get-post-counts
   :get-post-link get-post-link
   :get-post-dates get-post-dates
   :get-post-tags get-post-tags
   :get-post-excerpts get-post-excerpts
   :get-post-body get-post-body})

(defn new-processor
  [system]
  (->DefaultBlogPostProcessor system))
