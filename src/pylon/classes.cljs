(ns pylon.classes)

(defn- method-fn-name
  [method-name]
  (str "__pylon$method$" method-name))

(defn- pylon-prop? [prop]
  (= "__pylon$" (subs prop 0 8)))

(defn- find-methods [p]
  (remove pylon-prop? (.getOwnPropertyNames js/Object p)))

(defn- prototype-for-class [obj class-name]
  (let [this-class (aget obj "__pylon$classname")]
    (console/log "PFC" class-name "==" this-class)
    (cond
     (nil? this-class) obj
     (= class-name this-class) obj
     :else (prototype-for-class (aget obj "__pylon$superclass") class-name))))

(defn create-ctor []
  (fn ctor [& args]
    (this-as
     this
     (let [p (.getPrototypeOf js/Object this)
           superclass (aget p "__pylon$superclass")]
       (when-let [binds (find-methods p)]
         (doseq [bind binds]
           (aset this bind (goog/bind (aget this bind) this))))
       ;; (aset this "__pylon_invokeSuper"
       ;;       (fn [class-name name & args]
       ;;         (console/log "invokeSuper" class-name name)
       ;;         (let [p (prototype-for-class
       ;;                  (js/Object.getPrototypeOf this) class-name)
       ;;               superclass (aget p "__pylon$superclass")
       ;;               name (if (aget superclass "__pylon$classname")
       ;;                      (method-fn-name name) name)
       ;;               args (if (aget superclass "__pylon$classname")
       ;;                      (cons this args) args)]
       ;;           (let [super-method (aget (.-prototype superclass) name)
       ;;                 super-fn (if (and (= name "constructor")
       ;;                                   (not super-method))
       ;;                         superclass super-method)
       ;;                 args (into-array args)]
       ;;             (console/log "this" p)
       ;;             (console/log "super" (.-prototype superclass))
       ;;             (console/log "method" super-method)
       ;;             (console/log "fn" super-fn)
       ;;             (console/log "args" args)
       ;;             (.apply super-fn this args)))))
       )
     (when (.hasOwnProperty this "constructor")
       (apply (.-constructor this) args))
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
