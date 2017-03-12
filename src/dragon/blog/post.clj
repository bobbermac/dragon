(ns dragon.blog.post
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [dragon.content.rfc5322 :as rfc5322]
            [dragon.util :as util]
            [markdown.core :as markdown]))

(defn update-tags
  [data]
  (assoc data :tags (apply sorted-set (string/split (:tags data) #",\s?"))))

(defn convert-body
  [data]
  (case (keyword (:content-type data))
    :md (assoc data :body (markdown/md-to-html-string (:body data)))))

(defn add-file-data
  [data]
  (let [file-obj (:file data)
        file-src (.getPath file-obj)
        filename-old (.getName file-obj)
        filename (format "%s.html" (util/sanitize-str (:title data)))]
    (assoc data :filename filename
                :file-src file-src
                :uri-path (-> file-src
                              (string/replace filename-old filename)
                              (string/replace-first "posts/" "")))))

(defn add-link
  [uri-base data]
  (let [link-template "<a href=\"%s/%s\">%s</a>"
        link (format link-template uri-base (:uri-path data) (:title data))]
    (assoc data :link link)))

(defn add-dates
  [data]
  (let [date (util/path->date (:file-src data))
        timestamp (util/format-date date)
        timestamp-clean (string/replace timestamp #"[^\d]" "")]
    (-> data
        (assoc :date date
               :month (util/month->name (:month date))
               :month-short (util/month->short-name (:month date))
               :timestamp timestamp
               :timestamp-long (Long/parseLong timestamp-clean)))))

(defn add-post-data
  ""
  [data]
  (->> data
       :file
       (slurp)
       (rfc5322/parse)
       (merge data)))

(defn process
  ""
  [uri-base file-obj]
  (->> file-obj
       (add-post-data)
       (add-file-data)
       (add-link uri-base)
       (add-dates)
       (convert-body)
       (update-tags)))