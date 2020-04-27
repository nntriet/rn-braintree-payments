import { NativeModules } from 'react-native';
import { convertGetCardNonceOptions } from './utils';
const {
  BraintreePayments
} = NativeModules;
const RNBraintreePayments = {
  showDropIn: BraintreePayments.showDropIn,
  showPayPal: BraintreePayments.showPayPal,
  getCardNonce: async options => {
    const convertOptions = convertGetCardNonceOptions(options);
    const result = await BraintreePayments.getCardNonce(convertOptions);
    return result;
  }
};
export * from './types';
export { RNBraintreePayments };
export default RNBraintreePayments;
//# sourceMappingURL=index.js.map