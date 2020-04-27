import { Platform } from 'react-native';
import { Options, GetCardNonceOptions } from './types';

interface IOSGetCardNonceOptions extends Options {
  number: string;
  cvv: string;
  expirationDate: string;
  cardholderName?: string;
  billingAddress: {
    firstName?: string;
    lastName?: string;
    company?: string;
    countryName?: string;
    countryCodeAlpha2?: string;
    countryCodeAlpha3?: string;
    countryCodeNumeric?: string;
    locality?: string;
    postalCode?: string;
    region?: string;
    streetAddress?: string;
    extendedAddress?: string;
  };
}
const convertGetCardNonceOptions = (options: GetCardNonceOptions) => {
  const convertToIOSOptions: IOSGetCardNonceOptions = {
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
      extendedAddress: options.extendedAddress,
    },
  };
  return Platform.OS === 'ios' ? convertToIOSOptions : options;
};

export { convertGetCardNonceOptions };
