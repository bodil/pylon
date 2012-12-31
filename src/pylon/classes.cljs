(ns pylon.classes)

(defn create-ctor []
  (js* "function ctor() {
  var obj = this, p = Object.getPrototypeOf(this),
      superclass = p.__pylon$superclass;
  if (this.__pylon$bind) {
    this.__pylon$bind.forEach(function(f) {
      obj[f] = goog.bind(obj[f], obj);
    });
  }
  this.__pylon_invokeSuper = function(name) {
    var args = goog.array.toArray(arguments).slice(1);
    if (superclass.__pylon$classname) {
      name = \"__pylon$method$\" + name;
      args = goog.array.concat(obj, args);
    }
    if (name == \"constructor\" && !superclass.prototype[name])
      return superclass.apply(obj, args);
    else
      return superclass.prototype[name].apply(obj, args);
  };
  if (this.constructor) this.constructor.apply(this, arguments);
}"))

(defn apply-method [ctor func methodname funcname]
  (js* "(function(ctor, name, fn_name, fn) {
  var p = ctor.prototype;
  p[fn_name] = fn;
  p[name] = function() {
    var args = goog.array.concat(this, goog.array.toArray(arguments));
    return this[fn_name].apply(this, args);
  };
  if (!p.__pylon$bind) p.__pylon$bind = [];
  p.__pylon$bind.push(name);
})(~{ctor}, ~{methodname}, ~{funcname}, ~{func})"))

(defn define-superclass [childclass superclass]
  (when superclass
    (goog/inherits childclass superclass)
    (js* "~{childclass}.prototype.__pylon$superclass = ~{superclass}")))
