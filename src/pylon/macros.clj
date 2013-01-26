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
  (let [[{:keys [extends mixin]} specs] (parse-args args)
        methods (map method-from-spec specs)
        ctor (gensym "ctor")
        class-string (str (ns-name *ns*) "." class-name)]
    `(let [~ctor (pylon.classes/create-ctor)]

       ;; Build the constructor
       (aset ~ctor "__pylon$classname" ~(name class-name))
       (def ~class-name ~ctor)

       ;; Extend with superclass prototype
       ~(when extends
          `(do
             (goog/inherits ~class-name ~extends)
             (aset (.-prototype ~class-name) "__pylon$superclass" ~extends)))

       ;; Extend with mixins
       ~@(for [m mixin]
           `(let [proto# (or (.-prototype ~m) ~m)]
              (goog/mixin (.-prototype ~class-name) proto#)))

       ;; Define methods
       ~@(for [{:keys [name fn-name sig body]} methods
               :let [dashname (symbol (str "-" name))]]
           `(let [func# ~(method-def name sig body)]
              ;; Apply the method to the prototype
              (pylon.classes/apply-method ~ctor func# ~name ~fn-name)
              (set! (.. ~class-name -prototype ~dashname)
                    (pylon.classes/method-wrapper ~fn-name))
              ;; Export the method name
              (goog/exportProperty (.-prototype ~class-name)
                                   ~name (.. ~class-name -prototype ~dashname))))

       ;; Export the class
       (goog/exportSymbol ~class-string ~class-name))))

(defmacro super [& args]
  `(.__pylon_invokeSuper ~'this ~'__pylon_method_name ~@args))
