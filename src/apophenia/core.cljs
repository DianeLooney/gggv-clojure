(ns apophenia.core)

(defn clamp [low high value]
  (cond (> low value) low
        (< high value) high
        :else value))

(defn env-static [beat velocity] velocity)

(defn env-linear [beat velocity]
  (clamp 0 1 (- 1 beat)))

(defn env-over [duration envelope] (fn [beat velocity] (envelope (/ beat duration) velocity)))

(defn time->beat [bpm sync-time sync-beat now-time]
  (let [beats-per-ms (/ (/ bpm 60) 1000)
        elapsed-time (- now-time sync-time)
        elapsed-beats (* elapsed-time beats-per-ms)]
    (+ sync-beat elapsed-beats)))

(defn ls [core] (:seqs @core))

(defn kill [core name]
  (swap! core #(update-in %1 [:seqs] dissoc %2) name))

(defn kill-all [core]
  (swap! core #(assoc % :seqs {})))

(defn =>
  ([core name pattern]
   (=> core name {} pattern))
  ([core name props pattern]
   (swap! core #(update-in %1 [:seqs name] (constantly %2)) {:props props, :pattern pattern})))

(defn bpm [core t bpm]
  (swap! core
         (fn [c] (assoc c
                        :sync-beat (time->beat (:bpm c) (:sync-time c) (:sync-beat c) t)
                        :sync-time t
                        :bpm bpm))
         bpm))

(defn dump [core]
  (let [c @core]
    (apply concat
           [(list 'bpm (:bpm c))]
           [(map (fn [[k v]] (list '=> k (:props v) (:pattern v))) (:seqs c))])))

(defn status-seq [global-beat [name seq]]
  (let [pattern (:pattern seq)
        beat (mod global-beat (count pattern))
        gain (or (-> seq :props :gain) 1)
        envelope (or (-> seq :props :envelope) env-static)]
    [name (* gain (envelope (mod beat 1) (nth pattern beat)))]))

(defn status [core t]
  (let [c @core
        beat (time->beat (:bpm c) (:sync-time c) (:sync-beat c) t)]
    (into {} (map (partial status-seq beat)) (:seqs c))))

(defn clock-now [] (.now js/Date))

(defn make []
  (atom {:seqs {}
         :bpm 120
         :sync-beat 0
         :sync-time (clock-now)}))
