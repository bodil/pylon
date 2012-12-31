(ns pylon.macros)

(defn- method-fn-name
  [method-name]
  (str "__pylon$method$" method-name))

(defn- method-def
  [method-name sig body]
  (let [sig-with-this (apply vector 'this sig)]
    `(fn ~(symbol method-name) ~sig-with-this
       (let [~'__pylon_method_name ~method-name]
         ~@body))))

(defn- method-from-spec [spec]
  (if (= 'defn (first spec)) (method-from-spec (rest spec))
    (let [name (name (first spec))]
      {:name name
       :fn-name (method-fn-name name)
       :sig (second spec)
       :body (drop 2 spec)})))

(defn- parse-args [args]
  (loop [args args opts {}]
    (cond
     (keyword? (first args))
     (recur (drop 2 args) (assoc opts (first args) (second args)))
     :else [opts args])))

(defmacro defclass
  [class-name & args]
  (let [[{:keys [extends]} specs] (parse-args args)
        methods (map method-from-spec specs)
        ctor (gensym "ctor")]
    `(let [~ctor (pylon.classes/create-ctor)]
       (aset ~ctor "__pylon$classname" ~(name class-name))
       (def ~class-name ~ctor)
       (pylon.classes/define-superclass ~ctor ~extends)
       ~@(for [{:keys [name fn-name sig body]} methods]
           `(let [func# ~(method-def name sig body)]
              (pylon.classes/apply-method ~ctor func# ~name ~fn-name))))))

(defmacro super [& args]
  `(.__pylon_invokeSuper ~'this ~'__pylon_method_name ~@args))
