(ns pylon.macros)

(defn- method-fn-name
  [method-name]
  (str "__pylon$method$" method-name))

(defn- method-def
  [ctor method-name sig body]
  (let [sig-with-this (apply vector 'this sig)]
    `(fn ~(symbol method-name) ~sig-with-this
       (let [~'__pylon_method_name ~method-name
             ~'__pylon_prototype (.-prototype ~ctor)]
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

       (aset (.-prototype ~ctor) "__pylon$classname" ~class-string)

       ;; Define methods
       ~@(for [{:keys [name fn-name sig body]} methods
               :let [dashname (symbol (str "-" name))]]
           `(let [func# ~(method-def class-name name sig body)]
              ;; Apply the method to the prototype
              (aset (.-prototype ~class-name) ~fn-name func#)
              (set! (.. ~class-name -prototype ~dashname)
                    (pylon.classes/method-wrapper ~fn-name))
              ;; Export the method name
              (goog/exportProperty (.-prototype ~class-name)
                                   ~name (.. ~class-name -prototype ~dashname))))

       ;; Export the class
       (goog/exportSymbol ~class-string ~class-name))))

(defmacro super [& args]
  `(let [super# (aget ~'__pylon_prototype "__pylon$superclass")]
     (pylon.classes/invoke-super super# ~'__pylon_method_name ~'this ~args)))
