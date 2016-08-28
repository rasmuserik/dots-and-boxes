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
(def boxes (doall (for [x (range 3) y (range 3)]
                          (sort (for [x1 [0 1] y1 [0 1]]
                                [(+ x x1) + y y1])))))
(defn coords [[x y]]
  (map #(+ % -6 (* 12 (js/Math.random)))
   (get (get point-coords x) y)))
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
    (swap! wins
           (map
            (fn [win box])
            
            ) wins boxes)
    ))
(defn handle-click [x y]
  (let [scale (/ 1024 js/window.innerWidth)
        x (* scale x)
        y (* scale y)
        pos [x y]
        [a b] (sort (take 2 (sort-by #(dist pos (coords %)) pts)))]
    (add-line a b :human)
    (log (prn-str pts))))

(aset canvas "onmousedown" #(handle-click (aget % "clientX") (aget % "clientY")))
(doto ctx
  (.clearRect 0 0 1024 512))

