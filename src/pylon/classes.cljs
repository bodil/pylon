(ns pylon.classes)

(defn- method-fn-name
  [method-name]
  (str "__pylon$method$" method-name))

(defn create-ctor []
  (fn ctor [& args]
    (this-as
     this
     (let [p (js/Object.getPrototypeOf this)
           superclass (aget p "__pylon$superclass")]
       (when-let [binds (aget p "__pylon$bind")]
         (doseq [bind binds]
           (aset this bind (goog/bind (aget this bind) this))))
       (aset this "__pylon_invokeSuper"
             (fn [name & args]
               (let [name (if (aget superclass "__pylon$classname")
                            (method-fn-name name) name)
                     args (if (aget superclass "__pylon$classname")
                            (cons this args) args)]
                 (let [super-method (aget (.-prototype superclass) name)
                       super-fn (if (and (= name "constructor")
                                         (not super-method))
                               superclass super-method)
                       args (into-array args)]
                   (.apply super-fn this args))))))
     (when (.hasOwnProperty this "constructor")
       (apply (.-constructor this) args))
     this)))

(defn apply-method [ctor func methodname funcname]
  (let [p (.-prototype ctor)]
    (aset p funcname func)
    (aset p methodname
          (fn [& args]
            (this-as this (apply (aget this funcname) (cons this args)))))
    (when-not (aget p "__pylon$bind")
      (aset p "__pylon$bind" (array)))
    (.push (aget p "__pylon$bind") methodname)))

(defn define-superclass [ctor superclass]
  (when superclass
    (goog/inherits ctor superclass)
    (let [p (.-prototype ctor)]
      (aset p "__pylon$superclass" superclass))))

(defn apply-mixins [ctor mixins]
  (when (seq mixins)
    (doseq [mixin mixins]
      (goog/mixin (.-prototype ctor) (or (.-prototype mixin) mixin)))))
