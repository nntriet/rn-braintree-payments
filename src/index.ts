import { NativeModules } from 'react-native';
import { convertGetCardNonceOptions } from './utils';
import { DropInOptions, PayPalOptions, GetCardNonceOptions, PaymentNonce } from './types';

const { BraintreePayments } = NativeModules;

const RNBraintreePayments: {
  showDropIn: (options: DropInOptions) => Promise<PaymentNonce>;
  showPayPal: (options: PayPalOptions) => Promise<PaymentNonce>;
  getCardNonce: (options: GetCardNonceOptions) => Promise<PaymentNonce>;
} = {
  showDropIn: BraintreePayments.showDropIn,
  showPayPal: BraintreePayments.showPayPal,
  getCardNonce: async (options: GetCardNonceOptions) => {
    const convertOptions = convertGetCardNonceOptions(options);
    const result = await BraintreePayments.getCardNonce(convertOptions);
    return result;
  },
};

export * from './types';
export { RNBraintreePayments };
export default RNBraintreePayments;
