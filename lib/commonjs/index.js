"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
var _exportNames = {
  RNBraintreePayments: true
};
exports.default = exports.RNBraintreePayments = void 0;

var _reactNative = require("react-native");

var _utils = require("./utils");

var _types = require("./types");

Object.keys(_types).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (Object.prototype.hasOwnProperty.call(_exportNames, key)) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function get() {
      return _types[key];
    }
  });
});
const {
  BraintreePayments
} = _reactNative.NativeModules;
const RNBraintreePayments = {
  showDropIn: BraintreePayments.showDropIn,
  showPayPal: BraintreePayments.showPayPal,
  getCardNonce: async options => {
    const convertOptions = (0, _utils.convertGetCardNonceOptions)(options);
    const result = await BraintreePayments.getCardNonce(convertOptions);
    return result;
  }
};
exports.RNBraintreePayments = RNBraintreePayments;
var _default = RNBraintreePayments;
exports.default = _default;
//# sourceMappingURL=index.js.map