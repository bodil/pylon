(ns pylon.classes)

(defn- method-fn-name
  [method-name]
  (str "__pylon$method$" method-name))

(defn- pylon-prop? [prop]
  (= "__pylon$" (subs prop 0 8)))

(defn- pylon-parent-proto [p]
  (when-let [parent (aget p "__pylon$superclass")]
    (when-let [proto (.-prototype parent)]
      (when (.hasOwnProperty proto "__pylon$classname")
        proto))))

(defn- find-props [p]
  (let [parent (pylon-parent-proto p)
        props (remove pylon-prop? (.getOwnPropertyNames js/Object p))]
    (if parent
      (concat props (find-props parent))
      props)))

(defn create-ctor []
  (fn ctor [& args]
    (this-as
     this
     (let [p (.getPrototypeOf js/Object this)
           superclass (aget p "__pylon$superclass")]
       (doseq [bind (apply hash-set (find-props p))]
         (let [func (aget this bind)]
           (when (fn? func)
             (aset this bind (goog/bind func this))))))
     (when-let [constructor (.-constructor this)]
       (.apply constructor this (into-array args)))
     this)))

(defn invoke-super [superclass method context args]
  (let [proto (.-prototype superclass)
        foreign? (nil? (aget proto "__pylon$classname"))
        method-name (if foreign? method (method-fn-name method))
        args (if foreign? args (cons context args))
        super-method (aget proto method-name)
        super-fn (if (and (= method "constructor") (not super-method))
                   superclass super-method)
        args (into-array args)]
    (.apply super-fn context args)))

(defn method-wrapper [funcname]
  (fn [& args]
    (this-as this (apply (aget this funcname) (cons this args)))))
