(ns hello-seesaw.core
  (:use [seesaw.core]
       [seesaw.color])
  (:require [seesaw.bind :as b]
            [seesaw.table :as t]
            [seesaw.widgets.log-window :as lw]))


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

(defn- map-app-to-color [app]
  (let [running-mode (:running-mode app)]
    (case running-mode
      "RUNNING" (color 20 20 20)
      "STOPPED" (color 30 30 30)
      "IN TRANSITION" (color 40 40 40)
      (color 120 120 120))))

(defn make-app-table []
  (let [render-pro (proxy [javax.swing.table.DefaultTableCellRenderer] []
                                      (getTableCellRendererComponent [comp value selected? focused? row col]
                                                                     (doto
                                                                       (proxy-super getTableCellRendererComponent comp value selected? focused? row col)
                                                                       (.setBackground (map-app-to-color (t/value-at comp row))))))]
    (scrollable (doto (table :id :app-table :model [:columns [{:key :app-id :text "App ID"}
                                                              {:key :cluster-id :text "Cluster ID"}
                                                              {:key :default-mode :text "Default Mode"}
                                                              {:key :running-mode :text "Running Mode" :default "NONE"}
                                                              {:key :target-mode :text "Target Mode"}
                                                              {:key :active-world  :text "Active World"}
                                                              {:key :current-state :text "Current State"}
                                                              {:key :pid :text "PID"}
                                                              {:key :status :text "Status"}]
                                                    :rows []])
                  (.setDefaultRenderer Object render-pro)))))
(defn make-app-details-panel []
  (horizontal-panel :id :app-details-panel :border [2 "App Details"]
                    :items [(button :text "ACTIVE")]))

(defn make-log-panel []
  (vertical-panel :background :red
                  :items [(scrollable (lw/log-window :id :log-panel :rows 15 :border [2 "LOGS"] :font :monospaced))
                          (horizontal-panel :border 2
                           :items
                           [(button :id :clear-logs-btn :text "CLR")
                            (button :id :toggle-autoscroll-btn :text "SCRL")
                            (text :text "HARDCODED LOG" :editable? false)])]))

(defn make-app-panel []
  (border-panel :north (make-app-table) :center (make-app-details-panel) :south (make-log-panel)))

(defn add-behaviours [app-f]
  (let [auto-scroll? (atom true)
        {:keys [log-panel toggle-logs-btn clear-logs-btn toggle-autoscroll-btn]} (group-by-id app-f)]
    (listen clear-logs-btn :action (fn [e] (lw/clear log-panel)))
    (listen toggle-autoscroll-btn :action (fn [e] (swap! auto-scroll? not)))
    (b/bind auto-scroll? (b/tee (b/property log-panel :auto-scroll?) (b/property toggle-autoscroll-btn :selected?)))))


(def app-id-map (atom {}))
(defn update-app [app-table app-id new-vals]
  (t/update-at! app-table (@app-id-map app-id) new-vals)
  (.repaint app-table))

(defn insert-app [app-table app]
  (let [idx (t/row-count app-table)]
    (t/insert-at! app-table idx app)
    (swap! app-id-map assoc (:app-id app) idx)))
 

(defn make-frame []
  (frame :title "Status Monitor"
         :content (make-app-panel)
         :on-close :exit))

(def app-frame (make-frame))

(defn display [content]
  (config! app-frame :content content)
  content)

(defn -main [& args]
  (do
    (native!)
    (invoke-later
     (-> app-frame
         pack!
         show!))))
