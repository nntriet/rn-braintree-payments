"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.convertGetCardNonceOptions = void 0;

var _reactNative = require("react-native");

const convertGetCardNonceOptions = options => {
  const convertToIOSOptions = {
    clientToken: options.clientToken,
    threeDSecure: options.threeDSecure,
    number: options.number,
    cvv: options.cvv,
    expirationDate: options.expirationDate,
    cardholderName: options.cardholderName,
    billingAddress: {
      firstName: options.firstName,
      lastName: options.lastName,
      company: options.company,
      countryName: options.countryName,
      countryCodeAlpha2: options.countryCodeAlpha2,
      countryCodeAlpha3: options.countryCodeAlpha3,
      countryCodeNumeric: options.countryCodeNumeric,
      locality: options.locality,
      postalCode: options.postalCode,
      region: options.region,
      streetAddress: options.streetAddress,
      extendedAddress: options.extendedAddress
    }
  };
  return _reactNative.Platform.OS === 'ios' ? convertToIOSOptions : options;
};

exports.convertGetCardNonceOptions = convertGetCardNonceOptions;
//# sourceMappingURL=utils.js.map