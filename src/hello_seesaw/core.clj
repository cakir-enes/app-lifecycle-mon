(ns hello-seesaw.core
  (:use [seesaw.core]
        [seesaw.color])
  (:require [seesaw.bind :as b]
            [seesaw.table :as t]))


; {:app-id "app-1"
;  :cluster-id 1
;  :default-mode :master
;  :running-mode :master
;  :target-mode  :none
;  :active-world  :real
;  :current-state :active-running
;  :pid 484
;  :status "WAITING FOR CMD"}

(defn render-fn [renderer info]
  (config! renderer :background-color :red))

(defn make-app-table []
  (table :id :table :model [:columns [{:key :app-id :text "App ID"}
                                      {:key :cluster-id :text "Cluster ID"}
                                      {:key :default-mode :text "Default Mode"}
                                      {:key :running-mode :text "Running Mode" :default "NONE"}
                                      {:key :target-mode :text "Target Mode"}
                                      {:key :active-world  :text "Active World"}
                                      {:key :current-state :text "Current State"}
                                      {:key :pid :text "PID"}
                                      {:key :status :text "Status"}]
                            :rows []]))

(defn- map-app-to-color [app]
  (let [running-mode (:running-mode app)]
    (case running-mode
      "RUNNING" (color 20 20 20)
      "STOPPED" (color 30 30 30)
      "IN TRANSITION" (color 40 40 40)
      (color 120 120 120))))

(def render-pro (proxy [javax.swing.table.DefaultTableCellRenderer] []
                  (getTableCellRendererComponent [comp value selected? focused? row col]
                    (doto
                     (proxy-super getTableCellRendererComponent comp value selected? focused? row col)
                      (.setBackground (map-app-to-color (t/value-at comp row)))))))

(def app-table (doto
                (make-app-table)
                 (.setDefaultRenderer Object render-pro)))

(def app-id-map (atom {}))
(defn update-app [app-id new-vals]
  (do
    (t/update-at! app-table (@app-id-map app-id) new-vals)
    (.repaint app-table)))

(defn insert-app [app]
  (do
    (let [idx (t/row-count app-table)]
      (t/insert-at! app-table idx app)
      (swap! app-id-map assoc (:app-id app) idx))))



(defn make-frame []
  (frame :title "Status Monitor"
         :content (scrollable app-table)
         :on-close :exit))
(def app-frame (make-frame))

(defn display [content]
  (config! app-frame :content content)
  content)

(defn -main [& args]
  (do
    (native!)
    (invoke-later
     (-> (frame :title "Hello"
                :content (scrollable app-table)
                :on-close :exit)
         pack!
         show!))))
