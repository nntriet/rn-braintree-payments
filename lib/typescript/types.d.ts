export interface Options {
    clientToken: string;
    threeDSecure?: {
        amount: number;
        email?: string;
        givenName?: string;
        surname?: string;
        phoneNumber?: string;
        streetAddress?: string;
        extendedAddress?: string;
        locality?: string;
        region?: string;
        postalCode?: string;
        countryCodeAlpha2?: string;
    };
}
export declare enum CardholderNameStatus {
    FIELD_DISABLED = 0,
    FIELD_OPTIONAL = 1,
    FIELD_REQUIRED = 2
}
export interface DropInOptions extends Options {
    enablePayPal?: boolean;
    vaultManager?: boolean;
    cardholderNameStatus?: CardholderNameStatus;
}
export interface PayPalOptions extends Options {
    amount?: number;
    offerCredit?: boolean;
}
interface GetCardNonceOptionsAndroid {
    /**
     * @platform android
     */
    countryCode?: string;
}
interface GetCardNonceOptionsIOS {
    /**
     * @platform ios
     */
    countryName?: string;
    /**
     * @platform ios
     */
    countryCodeAlpha2?: string;
    /**
     * @platform ios
     */
    countryCodeAlpha3?: string;
    /**
     * @platform ios
     */
    countryCodeNumeric?: string;
}
interface GetCardNonceBaseOptions extends Options {
    number: string;
    cvv: string;
    expirationDate: string;
    cardholderName?: string;
    firstName?: string;
    lastName?: string;
    company?: string;
    locality?: string;
    postalCode?: string;
    region?: string;
    streetAddress?: string;
    extendedAddress?: string;
    validate?: boolean;
}
export declare type GetCardNonceOptions = GetCardNonceBaseOptions & GetCardNonceOptionsIOS & GetCardNonceOptionsAndroid;
export interface PaymentNonce {
    nonce: string;
    type: string;
    lastDigits: string;
    clientToken: string;
    isDefault: boolean;
}
export {};
