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
}
export interface GetCardNonceOptions extends Options {
    number: string;
    cvv: string;
    expirationDate: string;
    cardholderName?: string;
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
}
export interface PaymentNonce {
    nonce: string;
    type: string;
    description: string;
    isDefault: boolean;
}
