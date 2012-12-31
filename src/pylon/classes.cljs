(ns pylon.classes)

(defn create-ctor []
  (js* "function ctor() {
  var obj = this;
  if (this.__pylon$bind) {
    this.__pylon$bind.forEach(function(f) {
      obj[f] = goog.bind(obj[f], obj);
    });
  }
//  if (this.constructor) this.constructor();
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
