(ns rcmnd.foodmain)

(defn shared-items
  "Get shared items with sustracted vals"
  [person1 person2]
  (reduce (fn [my-map value]
            (assoc my-map value (- (person1 value) (person2 value))))
          {}(filter person1 (keys person2))))

(defn sum-of-squares
  "Get sum of squares of shared items values"
  [person1 person2]
  (let [sum (map
              (fn [x] (* x x))
              (vals (shared-items person1 person2)))]
    (reduce + sum)))

(defn sim-distance 
  "Calculate Euclidean distance between 2 person"
  [person1 person2] 
  (if (zero? (count (shared-items person1 person2)))
    0
    (/ 1 (+ 1 (sum-of-squares person1 person2)))))

(defn all-matches 
  "Returns similarity weight, calculate Euclidean distance with every other"
  [persons-name data]
  (reduce (fn [my-map value]
            (assoc my-map (key value) (sim-distance (data persons-name) (data (key value)))))
          {} (dissoc data persons-name)))

(defn all-matches-weighted
  "Get all matches weighted: similarity * ratings for all persons"
 ([person1 persons-name data] 
  (reduce (fn [my-map value]
            (assoc my-map
              (key value) (* ((all-matches persons-name data) person1) ((data person1) (key value)))))
          {} (data person1)))
([persons-name data]
  (reduce (fn [my-map value]
            (assoc my-map 
              (key value) (all-matches-weighted (key value) persons-name data)))
          {} (dissoc data persons-name))))

(defn total-sum
  "Get total Recommendations for one person, merged"
  [persons-name data]
  (reduce (fn [my-map value]
            (merge-with + my-map value))
          {} (vals (all-matches-weighted persons-name data))))

(defn havent-been
  "Restaurants I havent been jet"
  [persons-name data]
   (remove (data persons-name) (keys (total-sum persons-name data))))

(defn my-total-sum
  "Total Recommendations without my restaurants"
  [persons-name data]
  (loop [total {}
         havent (havent-been persons-name data)]
    (if (empty? havent)
      total
      (recur (assoc total 
               (first havent) ((total-sum persons-name data) (first havent))) (rest havent)))))

(defn my-final-sum
  "Remove zero values"
  [persons-name data]
  (filter (fn [s] (not(zero? (val s)))) (my-total-sum persons-name data)))

(defn sim-sums 
  "Sum of similarities for one restaurant"
  [persons-name rest-name data]
  (let [persons-who-rated 
        (keys (filter (fn [s]
                        (contains? (val s) rest-name)) (all-matches-weighted persons-name data)))]
        (apply + (vals (select-keys (all-matches persons-name data) persons-who-rated)))))

(defn rankings
  "Create the normalized sorted list"
  [persons-name data]
  (sort-by val
    (reduce (fn [my-map value]
              (assoc my-map (key value) (/ (val value)(sim-sums persons-name (key value) data))))
            {} (my-final-sum persons-name data))))

(defn restaurant-list
  "Get a list of restaurants for web page"
  [data]
   (keys (reduce (fn [my-map value]
                   (merge my-map value))
                 {} (vals data))))

(defn web-parse
  "Parse string values to Integer"
  [data]
  (reduce (fn [my-map value]
            (assoc my-map (key value) (Integer/parseInt (val value))))
          {} data))

(defn recommendation-main
  "Create list with web page ratings data"
  [guest1 data]
  (assoc data "Guest" (web-parse guest1)))