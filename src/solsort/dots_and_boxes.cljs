(ns solsort.dots-and-boxes
  #_(:require
   [clojure.core.matrix :as m])
  )

(js/setTimeout #(.play (js/document.getElementById "video")) 1000)


(defn log [& args] (js/console.log.apply js/console (clj->js args)))
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
(def possible-lines (doall (distinct (sort (apply concat boxes)))))
(defn coords [[x y]] (get (get point-coords x) y))
(def box-centers
  [[240 160] [370 175] [490 187] [230 265] [365 275] [483 277] [200 377] [350 380] [479 383]])
(defn square [x] (* x x))
(defn dist [[x1 y1] [x2 y2]] (js/Math.sqrt (+ (square (- x1 x2)) (square (- y1 y2)))))
(defonce canvas (js/document.getElementById "canvas"))
(defonce ctx (.getContext canvas "2d"))

(defn load-image [name] (doto (js/Image.) (aset "src" (str "assets/" name ".png"))))
(defn load-images [type]
  (doall (map #(load-image (str type %))
        (range 9))))
(def images
  {:win (load-image "win")
   :lose (load-image "lose")
   :human (load-images "human")
   :computer (load-images "computer")})

(def lines (atom #{}))
(def wins (atom [false false false false false false false false false]))
(defn new-game []
  (reset! lines #{})
  (reset! wins  [false false false false false false false false false])
  (.clearRect ctx 0 0 1024 512)
  )

(defn line [a b]
  (doto ctx
    (.beginPath)
    (aset "lineWidth" 2)
    (.moveTo (first a) (second a))
    (.lineTo (first b) (second b))
    (.stroke)))
(defn add-line [a b player]
  (if (@lines [a b])
    false
    (do (swap! lines conj [a b])
        (line (coords a) (coords b))
        (reset! wins
                (doall
                 (map
                  (fn [win box]
                    (or win (and (not (some nil? (map @lines box))) player)))
                  @wins boxes)))
        (doall
         (map
          (fn [winner pos id]
            (when winner
              (.fillRect ctx (first pos) (second pos) 2 2)
              (let [img (nth (winner images) id)]
                (.drawImage ctx img
                            (- (first pos) (/ (.-width img) 2))
                            (- (second pos) (/ (.-height img) 2)))))
            )
          @wins box-centers (range 9)))
        true)
   ; (log @wins)
    ))
(defn neighbours [line] (filter #(some #{line} %) boxes))
(defn degree [box] (count (remove nil? (map @lines box))))

(defn game-done []
  (.drawImage ctx ((if (< 4 (count (filter #{:human} @wins))) 
                       :win :lose)
                   images) 200 100))
(defn scoring-function [line]
  (let [d (map degree (neighbours line))
        mx (apply max d)
        mn (apply min d)]
    (if (= mx 3)
      -1
      mx)))

(defn computer-turn []
  (if-not (some not @wins)
    (js/setTimeout game-done 500)
    (let [prev-wins @wins
          selected (first
                    (sort-by scoring-function
                              (remove @lines (shuffle possible-lines))))]
      (add-line (first selected) (second selected) :computer)
      (when-not (= prev-wins @wins)
        (computer-turn)))))
(defn handle-click [x y]
  (if (some not @wins)
    (let [scale (/ 1024 js/window.innerWidth)
         x (* scale x)
         y (* scale y)
         pos [x y]
         [a b] (sort (take 2 (sort-by #(dist pos (coords %)) pts)))
         prev-winners @wins]
     (when (add-line a b :human)
       (when (= prev-winners @wins)
         (js/setTimeout computer-turn 100)
         ))
     (when-not (some not @wins)
       (js/setTimeout game-done 100))
     )
    (new-game)))
(count @lines)

(aset canvas "onclick" #(handle-click (aget % "clientX") (aget % "clientY")))
(doto ctx
  (.clearRect 0 0 1024 512))

