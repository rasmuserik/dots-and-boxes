(ns solsort.dots-and-boxes
  #_(:require
   [clojure.core.matrix :as m])
  )

(js/setTimeout #(.play (js/document.getElementById "video")) 1000)


(def point-coords
  [[[190 108] [170 205] [153 315] [132 440]]
   [[315 126] [298 217] [290 317] [281 436]]
   [[440 141] [432 222] [421 330] [419 434]]
   [[563 162] [557 243] [553 330] [542 436]]])
(def pts (doall (for [x (range 4) y (range 4)] [x y])))
(def boxes (doall (for [y (range 3) x (range 3)]
                    [[[x y] [x (inc y)]]
                      [[x y] [(inc x) y]]
                      [[(inc x) y] [(inc x) (inc y)]]
                      [[x (inc y)] [(inc x) (inc y)]]])))
(defn coords [[x y]] (get (get point-coords x) y))
(def box-centers
  [[240 160] [370 175] [490 187] [230 265] [365 275] [483 277] [200 377] [350 380] [479 383]])
(defn square [x] (* x x))
(defn dist [[x1 y1] [x2 y2]] (js/Math.sqrt (+ (square (- x1 x2)) (square (- y1 y2)))))
(defn log [& args] (js/console.log.apply js/console (clj->js args)))
(defonce canvas (js/document.getElementById "canvas"))
(defonce ctx (.getContext canvas "2d"))

(def lines (atom #{}))
(def wins (atom [nil nil nil nil nil nil nil nil nil]))
(defn line [a b]
  (doto ctx
    (.beginPath)
    (aset "lineWidth" 2)
    (.moveTo (first a) (second a))
    (.lineTo (first b) (second b))
    (.stroke)))
(defn add-line [a b player]
  (when-not (@lines [a b])
    (swap! lines conj [a b])
    (line (coords a) (coords b))
    (reset! wins
            (doall
             (map
              (fn [win box]
                (or win (and (not (some nil? (map @lines box))) player)))
              @wins boxes)))
    (doall
     (map
      (fn [winner pos]
        (log winner pos)
        (when winner
          (.fillRect ctx (first pos) (second pos) 2 2))
        )
      @wins box-centers))
   ; (log @wins)
    ))

(defn handle-click [x y]
  (let [scale (/ 1024 js/window.innerWidth)
        x (* scale x)
        y (* scale y)
        pos [x y]
        [a b] (sort (take 2 (sort-by #(dist pos (coords %)) pts)))]
    (add-line a b :human)
    (log pos)))

(aset canvas "onmousedown" #(handle-click (aget % "clientX") (aget % "clientY")))
(doto ctx
  (.clearRect 0 0 1024 512))

