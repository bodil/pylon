(ns pylon.macros)

(defn- method-fn-name
  [method-name]
  (str "__pylon$method$" method-name))

(defn- method-def
  [method-name sig body]
  (let [sig-with-this (apply vector 'this sig)]
    `(fn ~(symbol method-name) ~sig-with-this ~@body)))

(defn- method-from-spec [spec]
  (let [name (name (first spec))]
    {:name name
     :fn-name (method-fn-name name)
     :sig (second spec)
     :body (drop 2 spec)}))

(defmacro defclass
  [class-name & specs]
  (let [methods (map method-from-spec specs)
        ctor (gensym "ctor")]
    `(let [~ctor (pylon.classes/create-ctor)]
       (def ~class-name ~ctor)
       ~@(for [{:keys [name fn-name sig body]} methods]
           `(let [func# ~(method-def name sig body)]
              (pylon.classes/apply-method ~ctor func# ~name ~fn-name))))))
