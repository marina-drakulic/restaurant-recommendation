;Great tutorial Developing and Deploying a Simple Clojure Web Application 
;by Mark McGranaghan has been used to create this Simple Web Interface.
;Tutorial can be found at: 
;http://mmcgrana.github.com/2010/07/develop-deploy-clojure-web-applications.html
;Lines from tutorial have been used extensively in core.clj.
(ns rcmnd.core
	(:use compojure.core)
	(:use hiccup.core)
	(:use hiccup.page-helpers)
	(:use rcmnd.middleware)
	(:use rcmnd.foodmain)
	(:use ring.middleware.file)
	(:use ring.middleware.file-info)
	(:use ring.middleware.reload)
	(:use ring.middleware.stacktrace)
	(:use ring.util.response))

	
(def production?
  (= "production" (get (System/getenv) "APP_ENV")))

(def development?
  (not production?))
  
(def ratings
     {"Milena" {"Makao" 2.5 "Bamboo" 3.5
                "Veliki Sangaj" 3.0 "Peking" 3.5
                "Maotao" 2.5 "Novi Hong Kong" 3.0}
      "Nikola" {"Makao" 3.0 "Bamboo" 3.5
                "Peking" 1.5  "Maotao" 5.0
                "Novi Hong Kong" 3.0 "Xiao Mao" 3.5}
      "Mihajlo" {"Makao" 2.5 "Bamboo" 3.0
                 "Veliki Sangaj" 3.5  "Peking" 4.0}
      "Sandra" {"Bamboo" 3.5 "Veliki Sangaj" 3.0
                "Maotao" 4.5 "Novi Hong Kong" 4.0
                "Xiao Mao" 2.5}
      "Jovana" {"Makao" 3.0 "Bamboo" 4.0
                "Veliki Sangaj" 2.0 "Peking" 3.0
                "Maotao" 3.0 "Novi Hong Kong" 2.0}
      "Petar" {"Makao" 3.0 "Bamboo" 4.0
               "Peking" 3.0 "Maotao" 5.0
               "Xiao Mao" 3.5}
      "Marija" {"Makao" 4.5 "Bamboo" 1.0
                "Xiao Mao" 4.0}
      "Marina" {"Dva jelena" 4.5 "Frans Restaurant" 1.0
                "Jevrem" 4.0 "Kalemegdanska terasa" 3 "SeSir moj" 1
                "Zaplet" 2.5}
      "Kristina" {"Que Pasa" 4.5 "Trac" 1.0
                "Casa" 4.0 "Dorian Gray" 3 "Mamma mia" 1
                "Ipanema" 2.5}
      "Sima" {"Kovac" 4.5 "Langouste" 1.0
                "Lipov lad" 4.0 "Madera" 3 "Na Cosku" 1
                "Opera" 2.5}
      "Vlada" {"Reka" 4.5 "Langouste" 1.0
                "Tribeca" 4.0 "Zorba" 3 "Lorenzo i Kakalamba" 1
                "Varos Kapija" 2.5}
      "Sasa" {"Sta je tu je" 4.5 "Trpeza" 1.0
                "Kristal" 4.0 "Monte Cristo" 3 "Iguana" 1
                "Sushi bar" 2.5}
      "Djordje" {"Bella Napoli" 4.5 "Bella Italia" 1.0
                "Golub" 4.0 "Monte Cristo" 3 "Da Gino" 1
                "Panorama" 2.5}
      "Steva" {"Maharaja" 4.5 "Leila" 1.0
                "Zapata" 4.0 "Nachos" 3 "Borrito bar" 1
                "Mytologia" 2.5}
      "Srdjan" {"Etna" 4.5 "Novak" 1.0
                "Bar Latino" 4.0 "Skadarlijski cardak" 3 "Seher" 1
                "Mytologia" 2.5}})
 
(defn view-layout [& content]
	(html
		(doctype :xhtml-strict)
		(xhtml-tag "en"
			[:head
				[:meta {:http-equiv "Content-type"
						:content "text/html; charset=utf-8"}]
				[:title "Restaurant recommendation"]
				[:link {:href "/stil.css" :rel "stylesheet" :type "text/css"}]]
			[:body content])))

(defn view-input []
	(view-layout
		[:h2 "Restaurant recommendation"]
		[:h3 "Rate restaurants You have been in with marks between 1 and 5."]
		[:form {:method "post" :action "/"}
		[:div.restaurants
			(for [restaurant (restaurant-list ratings)] 
				[:div
					[:span restaurant]
           (for [ocena [1 2 3 4 5]] 
					[:input {:type "radio" :name restaurant :value ocena} ocena])])]
;		[:input.math {:type "text" :name "a"}] [:span.math " + "]
;		[:input.math {:type "text" :name "b"}] [:br]
		[:input {:type "submit" :value "Recommend"}]]))

(defn view-output [prms]
	(view-layout
	[:h2 "Recomended restaurants"]
	[:div.restaurants
	(for [item (reverse (rankings "Guest" (recommendation-main prms ratings)))] 
		[:div (for [i item] [:span.result i])])]
	[:a.action {:href "/"} "Try again"]))

(defn parse-input [a b]
	[(Integer/parseInt a) (Integer/parseInt b)])

(defroutes handler
	(GET "/" []	(view-input))

	(POST "/" {params :params} 
    (view-output params))
	
	(ANY "/*" [path]
    (redirect "/")))

(def app
  (-> #'handler
    (wrap-utf)
    (wrap-file "public")
    (wrap-file-info)
    (wrap-request-logging)
    (wrap-if development? wrap-reload '[rcmnd.middleware rcmnd.core])
    (wrap-bounce-favicon)
    (wrap-exception-logging)
    (wrap-if production?  wrap-failsafe)
    (wrap-if development? wrap-stacktrace)))

