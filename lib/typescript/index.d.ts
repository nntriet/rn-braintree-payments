import { DropInOptions, PayPalOptions, GetCardNonceOptions, PaymentNonce } from './types';
declare const RNBraintreePayments: {
    showDropIn: (options: DropInOptions) => Promise<PaymentNonce>;
    showPayPal: (options: PayPalOptions) => Promise<PaymentNonce>;
    getCardNonce: (options: GetCardNonceOptions) => Promise<PaymentNonce>;
};
export * from './types';
export { RNBraintreePayments };
export default RNBraintreePayments;
