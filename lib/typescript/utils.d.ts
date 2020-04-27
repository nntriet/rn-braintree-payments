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
declare const convertGetCardNonceOptions: (options: GetCardNonceOptions) => GetCardNonceOptions | IOSGetCardNonceOptions;
export { convertGetCardNonceOptions };
