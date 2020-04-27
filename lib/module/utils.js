import { Platform } from 'react-native';

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
  return Platform.OS === 'ios' ? convertToIOSOptions : options;
};

export { convertGetCardNonceOptions };
//# sourceMappingURL=utils.js.map