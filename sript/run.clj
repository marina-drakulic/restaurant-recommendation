(use 'ring.adapter.jetty)
(require 'rcmnd.core)

(let [port (Integer/parseInt (get (System/getenv) "PORT" "8080"))]
  (run-jetty #'rcmnd.core/app {:port port}))