(ns untangled-template.ui.html5-routing-spec
  (:require
    [untangled-template.ui.html5-routing :as r]
    [untangled-spec.core :refer [specification provided behavior assertions when-mocking]]
    [bidi.bidi :as bidi]
    [pushy.core :as pushy]
    [untangled.client.routing :as ur]))

(specification "Valid handlers"
  (assertions
    "is a set of the pages that we can nav to"
    (set? r/valid-handlers) => true
    (contains? r/valid-handlers :login) => true
    (contains? r/valid-handlers :new-user) => true))

(specification "App Routes (bidi config)"
  (assertions
    "Maps /index.html to :main"
    (bidi/match-route r/app-routes "/index.html") => {:handler :main}
    "Maps /login.html to :login"
    (bidi/match-route r/app-routes "/login.html") => {:handler :login}
    "Maps /preferences.html to :preferences"
    (bidi/match-route r/app-routes "/preferences.html") => {:handler :preferences}
    "Maps /signup.html to :new-user"
    (bidi/match-route r/app-routes "/signup.html") => {:handler :new-user}))

(specification "Invalid route"
  (assertions "Correctly identifies route handlers that are usable"
    (r/invalid-route? :login) => false
    (r/invalid-route? :preferences) => false
    (r/invalid-route? :new-user) => false
    (r/invalid-route? :main) => false
    (r/invalid-route? :goo) => true))

(specification "Redirect* (mutation helper)"
  (do
    (reset! r/use-html5-routing true)
    (provided "HTML5 Routing is in use"
      (bidi/path-for r h & p) => "/preferences.html"
      (pushy/set-token! h p) => (assertions
                                  "Generates a URI HISTORY set token"
                                  p => "/preferences.html")

      (let [actual (r/redirect* {:state-map true} {:handler :preferences})]
        (assertions
          "Returns the state map"
          actual => {:state-map true}))))
  (try
    (reset! r/use-html5-routing false)
    (provided "HTML5 Routing is NOT in use"
      (ur/update-routing-links s m) => (assertions
                                         "Just updates the in-memory state"
                                         s => {:state-map true}
                                         m => {:handler :preferences})

      (r/redirect* {:state-map true} {:handler :preferences}))
    (finally (reset! r/use-html5-routing true))))

(specification "set-route!* (mutation helper)" :focused
  (behavior "Routing to login or new user"
    (when-mocking
      (ur/update-routing-links s m) => (assoc s :routed m)

      (assertions
        "Updates the route when the page is :new-user, even when logged out"
        (r/set-route!* {:logged-in? true} {:handler :new-user}) => {:logged-in? true :routed {:handler :new-user}}
        (r/set-route!* {:logged-in? false} {:handler :new-user}) => {:logged-in? false :routed {:handler :new-user}}
        "Updates the route when the page is :login even when logged out"
        (r/set-route!* {:logged-in? true} {:handler :login}) => {:logged-in? true :routed {:handler :login}}
        (r/set-route!* {:logged-in? false} {:handler :login}) => {:logged-in? false :routed {:handler :login}})))
  (behavior "Routing to any other page when not logged in"
    (do
      (reset! r/history "history")
      (when-mocking
        (pushy/get-token h) => "/preferences.html"
        (r/redirect* m h) => (do
                               (assertions
                                 "triggers a redirect* with the updated state and :login as the target"
                                 m => {:logged-in? false :loaded-uri "/preferences.html"}
                                 h => {:handler :login})
                               m)

        (assertions
          "returns the updated state, with the original target URI in :loaded-uri"
          (r/set-route!* {:logged-in? false} {:handler :preferences}) => {:logged-in? false :loaded-uri "/preferences.html"}))))
  (behavior "Routing to an invalid page (logged in)"
    (do
      (reset! r/history "history")
      (when-mocking
        (r/invalid-route? k) => true
        (r/redirect* m h) => (do
                               (assertions
                                 "triggers a redirect* to :main"
                                 m => {:logged-in? true}
                                 h => {:handler :main})
                               (assoc m :routed :main))

        (assertions
          "returns the updated state"
          (r/set-route!* {:logged-in? true} {:handler :goop}) => {:logged-in? true :routed :main}))))
  (behavior "Routing to an valid page (logged in)"
    (do
      (reset! r/history "history")
      (when-mocking
        (r/invalid-route? k) => false
        (ur/update-routing-links s m) => (do
                                           (assertions
                                             "Updates the route in app state"
                                             m => {:handler :preferences})
                                           (assoc s :routed m))

        (assertions
          "returns the updated state"
          (r/set-route!* {:logged-in? true} {:handler :preferences}) => {:logged-in? true :routed {:handler :preferences}})))))
