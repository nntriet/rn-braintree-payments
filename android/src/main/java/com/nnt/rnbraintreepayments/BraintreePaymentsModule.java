package com.nnt.rnbraintreepayments;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
// import com.google.gson.Gson;

import android.content.Intent;
import android.content.Context;
import android.app.Activity;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;


import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.exceptions.BraintreeError;
import com.braintreepayments.api.interfaces.BraintreeListener;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.Card;
import com.braintreepayments.api.models.CardBuilder;
import com.braintreepayments.api.PayPal;
import com.braintreepayments.api.models.PayPalRequest;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.ThreeDSecure;
import com.braintreepayments.api.models.ThreeDSecureInfo;
import com.braintreepayments.api.models.ThreeDSecureRequest;
import com.braintreepayments.api.models.ThreeDSecurePostalAddress;
import com.braintreepayments.api.models.ThreeDSecureAdditionalInformation;
import com.braintreepayments.api.interfaces.ThreeDSecureLookupListener;
import com.braintreepayments.api.models.ThreeDSecureLookup;

public class BraintreePaymentsModule extends ReactContextBaseJavaModule {
    private static final int REQUEST_CODE = 0x444;

    private String clientToken;
    private ReadableMap threeDSecureOptions;

    private BraintreeFragment mBraintreeFragment;
    private Promise mPromise;

    private boolean isThreeDSecureVerificationComplete = false;

    public BraintreePaymentsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(mActivityListener);
    }

    @Override
    public String getName() {
        return "BraintreePayments";
    }

    private class ListenerHolder {
        public List<BraintreeListener> listeners = new ArrayList<>();
    }

    private void resetListeners(BraintreeFragment fragment, ListenerHolder listenerHolder,
                                       List<BraintreeListener> listeners) {
        for (BraintreeListener listener : listenerHolder.listeners) {
            fragment.removeListener(listener);
        }

        for (BraintreeListener previousListener : listeners) {
            fragment.addListener(previousListener);
        }
    }

    private final WritableMap getPaymentNonce(String nonce, String type, String lastDigits, boolean isDefault) {
        WritableMap jsResult = Arguments.createMap();
        jsResult.putString("nonce", nonce);
        jsResult.putString("type", type);
        jsResult.putString("lastDigits", lastDigits);
        jsResult.putString("clientToken", clientToken);
        jsResult.putBoolean("isDefault", isDefault);
        return jsResult;
    }

    private final ThreeDSecureRequest getThreeDSecureRequest(final ReadableMap threeDSecureOpts, final String nonce) {
        String amount = String.valueOf(threeDSecureOpts.getDouble("amount")); // Required
        String email = null;
        
        String givenName = null;
        String surname = null;
        String phoneNumber = null;
        String streetAddress = null;
        String extendedAddress = null;
        String locality = null;
        String region = null;
        String postalCode = null;
        String countryCodeAlpha2 = null;

        if (threeDSecureOpts.hasKey("email")) {
            email = threeDSecureOpts.getString("email");
        }
        if (threeDSecureOpts.hasKey("givenName")) {
            givenName = threeDSecureOpts.getString("givenName");
        }
        if (threeDSecureOpts.hasKey("surname")) {
            surname = threeDSecureOpts.getString("surname");
        }
        if (threeDSecureOpts.hasKey("phoneNumber")) {
            phoneNumber = threeDSecureOpts.getString("phoneNumber"); // Only number
        }
        if (threeDSecureOpts.hasKey("streetAddress")) {
            streetAddress = threeDSecureOpts.getString("streetAddress");
        }
        if (threeDSecureOpts.hasKey("extendedAddress")) {
            extendedAddress = threeDSecureOpts.getString("extendedAddress");
        }
        if (threeDSecureOpts.hasKey("locality")) {
            locality = threeDSecureOpts.getString("locality");
        }
        if (threeDSecureOpts.hasKey("region")) {
            region = threeDSecureOpts.getString("region");
        }
        if (threeDSecureOpts.hasKey("postalCode")) {
            postalCode = threeDSecureOpts.getString("postalCode");
        }
        if (threeDSecureOpts.hasKey("countryCodeAlpha2")) {
            countryCodeAlpha2 = threeDSecureOpts.getString("countryCodeAlpha2");
        }

        ThreeDSecurePostalAddress address = new ThreeDSecurePostalAddress()
            .givenName(givenName) // ASCII-printable characters required, else will throw a validation error
            .surname(surname) // ASCII-printable characters required, else will throw a validation error
            .phoneNumber(phoneNumber)
            .streetAddress(streetAddress)
            .extendedAddress(extendedAddress)
            .locality(locality)
            .region(region)
            .postalCode(postalCode)
            .countryCodeAlpha2(countryCodeAlpha2);

        // For best results, provide as many additional elements as possible.
        ThreeDSecureAdditionalInformation additionalInformation = new ThreeDSecureAdditionalInformation()
            .shippingAddress(address);

        ThreeDSecureRequest threeDSecureRequest = new ThreeDSecureRequest()
            .versionRequested(ThreeDSecureRequest.VERSION_2) // Required
            .amount(amount) // Required
            .email(email)
            .billingAddress(address)
            .additionalInformation(additionalInformation);
        
        if (nonce != null) {
            threeDSecureRequest.nonce(nonce);
        }

        return threeDSecureRequest;
    }
    
    private final void checkCardThreeDSecure(CardNonce cardNonce, final Promise promise) {
        ThreeDSecureInfo threeDSecureInfo = cardNonce.getThreeDSecureInfo();
        if (!threeDSecureInfo.isLiabilityShiftPossible()) {
            promise.reject("3DSECURE_NOT_ABLE_TO_SHIFT_LIABILITY", "3D Secure liability cannot be shifted");
        } else if (!threeDSecureInfo.isLiabilityShifted()) {
            promise.reject("3DSECURE_LIABILITY_NOT_SHIFTED", "3D Secure liability was not shifted");
        } else {
            promise.resolve(getPaymentNonce(cardNonce.getNonce(), cardNonce.getTypeLabel(), cardNonce.getLastFour(), cardNonce.isDefault()));
        }
    }
    
    private final boolean validateParams(final ReadableMap options, final Promise promise) {
        if (!options.hasKey("clientToken")) {
            promise.reject("NO_CLIENT_TOKEN", "You must provide a client token");
            return false;
        }
        if (options.hasKey("threeDSecure")) {
            final ReadableMap threeDSecureOpts = options.getMap("threeDSecure");
            if (!threeDSecureOpts.hasKey("amount")) {
                promise.reject("NO_3DS_AMOUNT", "You must provide an amount for 3D Secure");
                return false;
            }
            threeDSecureOptions = threeDSecureOpts;
        } else {
            threeDSecureOptions = null;
        }

        clientToken = options.getString("clientToken");
        mPromise = promise;
        return true;
    }
    
    private boolean setup(final ReadableMap options, final Promise promise) {
        if (!validateParams(options, promise)) return false;
            
        String token = options.getString("clientToken");
        try {
            mBraintreeFragment = BraintreeFragment.newInstance((AppCompatActivity) getCurrentActivity(),  token);
        } catch (InvalidArgumentException e) {
            promise.reject("INVALID_ARGUMENT_ERROR", e.getMessage());
            return false;
        }

        final List<BraintreeListener> previousListeners = mBraintreeFragment.getListeners();
        final ListenerHolder listenerHolder = new ListenerHolder();
        try {
            BraintreeCancelListener cancelListener = new BraintreeCancelListener() {
                @Override
                public void onCancel(int requestCode) {
                    resetListeners(mBraintreeFragment, listenerHolder, previousListeners);

                    promise.reject("USER_CANCELLATION", "The user cancelled");
                }
            };
            listenerHolder.listeners.add(cancelListener);

            BraintreeErrorListener errorListener = new BraintreeErrorListener() {
                @Override
                public void onError(Exception error) {
                    resetListeners(mBraintreeFragment, listenerHolder, previousListeners);
                    
                    if (error instanceof ErrorWithResponse) {
                        ErrorWithResponse errorWithResponse = (ErrorWithResponse) error;
                        BraintreeError cardErrors = errorWithResponse.errorFor("creditCard");
                        if (cardErrors != null) {
                            // Gson gson = new Gson();
                            // final Map<String, String> errors = new HashMap<>();
                            BraintreeError numberError = cardErrors.errorFor("number");
                            BraintreeError cvvError = cardErrors.errorFor("cvv");
                            BraintreeError expirationDateError = cardErrors.errorFor("expirationDate");
                            BraintreeError cardholderNameError = cardErrors.errorFor("cardholderName");

                            WritableMap errors = Arguments.createMap();
                            if (numberError != null) {
                                errors.putString("card_number", numberError.getMessage());
                            }
                            if (cvvError != null) {
                                errors.putString("cvv", cvvError.getMessage());
                            }
                            if (expirationDateError != null) {
                                errors.putString("expiration_date", expirationDateError.getMessage());
                            }
                            if (cardholderNameError != null) {
                                errors.putString("cardholder_name", cardholderNameError.getMessage());
                            }
                            promise.reject("CARD_ERROR", errors);
                        } else {
                            promise.reject("CARD_ERROR", errorWithResponse.getErrorResponse());
                        }
                    } else {
                        promise.reject("CARD_ERROR", error.getMessage());
                    }
                }
            };
            listenerHolder.listeners.add(errorListener);

            PaymentMethodNonceCreatedListener nonceListener = new PaymentMethodNonceCreatedListener() {
                @Override
                public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
                    if (threeDSecureOptions != null && paymentMethodNonce instanceof CardNonce) {
                        CardNonce cardNonce = (CardNonce) paymentMethodNonce;
                        if (isThreeDSecureVerificationComplete) {
                            resetListeners(mBraintreeFragment, listenerHolder, previousListeners);
                            isThreeDSecureVerificationComplete = false;
                            checkCardThreeDSecure(cardNonce, promise);
                        } else {
                            ThreeDSecureRequest threeDSecureRequest = getThreeDSecureRequest(threeDSecureOptions, cardNonce.getNonce());
                            ThreeDSecure.performVerification(mBraintreeFragment, threeDSecureRequest, new ThreeDSecureLookupListener() {
                                @Override
                                public void onLookupComplete(ThreeDSecureRequest request, ThreeDSecureLookup lookup) {
                                    // Optionally inspect the lookup result and prepare UI if a challenge is required
                                    isThreeDSecureVerificationComplete = true;
                                    ThreeDSecure.continuePerformVerification(mBraintreeFragment, request, lookup);
                                }
                            });
                        }
                    } else {
                        resetListeners(mBraintreeFragment, listenerHolder, previousListeners);
                        if (paymentMethodNonce instanceof CardNonce) {
                            CardNonce cardNonce = (CardNonce) paymentMethodNonce;
                            promise.resolve(getPaymentNonce(cardNonce.getNonce(), cardNonce.getTypeLabel(), cardNonce.getLastFour(), cardNonce.isDefault()));
                        } else {
                            String lastDigits = "";
                            if (paymentMethodNonce.getDescription() != null) {
                                lastDigits = paymentMethodNonce.getDescription().replaceAll("\\D+","");
                            }
                            promise.resolve(getPaymentNonce(paymentMethodNonce.getNonce(), paymentMethodNonce.getTypeLabel(), lastDigits, paymentMethodNonce.isDefault()));
                        }
                    }

                }
            };
            listenerHolder.listeners.add(nonceListener);

            mBraintreeFragment.addListener(cancelListener);
            mBraintreeFragment.addListener(errorListener);
            mBraintreeFragment.addListener(nonceListener);

            return true;
        } catch (Exception e) {
            resetListeners(mBraintreeFragment, listenerHolder, previousListeners);
            promise.reject("LISTENER_ERROR", e.getMessage());

            return false;
        }
    }

    @ReactMethod
    public void showPayPal(final ReadableMap options, final Promise promise) {
        if (!setup(options, promise)) return;
        if (mBraintreeFragment == null) {
            promise.reject("NO_BRAINTREE_FRAGMENT", "There is no mBraintreeFragment");
            return;
        }

        PayPalRequest paypalRequest;
            // .offerCredit(true); // Optional: Offer PayPal Credit
            // .currencyCode(...) // Optional: A valid ISO currency code to use for the transaction. Defaults to merchant currency code if not set. If unspecified, the currency code will be chosen based on the active merchant account in the client token.
            // .localeCode(...) // Optional: da_DK, de_DE, en_AU, en_GB, en_US, es_ES, es_XC, fr_CA, fr_FR, fr_XC, id_ID, it_IT, ja_JP, ko_KR, nl_NL, no_NO, pl_PL, pt_BR, pt_PT, ru_RU, sv_SE, th_TH, tr_TR, zh_CN, zh_HK, zh_TW, zh_XC.
            // .billingAgreementDescription(...); // Optional: Display a custom description to the user for a billing agreement.
        if (!options.hasKey("amount")) {
            paypalRequest = new PayPalRequest();
            if (options.hasKey("offerCredit")) {
                paypalRequest.offerCredit(options.getBoolean("offerCredit"));
            }
            PayPal.requestBillingAgreement(mBraintreeFragment, paypalRequest);
        } else {
            String amount = String.valueOf(options.getDouble("amount"));
            paypalRequest = new PayPalRequest(amount);
            if (options.hasKey("offerCredit")) {
                paypalRequest.offerCredit(options.getBoolean("offerCredit"));
            }
            PayPal.requestOneTimePayment(mBraintreeFragment, paypalRequest);
        }
    }

    @ReactMethod
    public void getCardNonce(final ReadableMap options, final Promise promise) {
        if (!setup(options, promise)) return;
        if (mBraintreeFragment == null) {
            promise.reject("NO_BRAINTREE_FRAGMENT", "There is no mBraintreeFragment");
            return;
        }

        CardBuilder cardBuilder = new CardBuilder();
            
        if (options.hasKey("number")) {
            cardBuilder.cardNumber(options.getString("number"));
        }
        if (options.hasKey("cvv")) {
            cardBuilder.cvv(options.getString("cvv"));
        }
        // In order to keep compatibility with iOS implementation, do not accept expirationMonth and exporationYear,
        // accept rather expirationDate (which is combination of expirationMonth/expirationYear)
        if (options.hasKey("expirationDate")) {
            cardBuilder.expirationDate(options.getString("expirationDate"));
        }
        // if (options.hasKey("merchantAccountId")) {
        //     cardBuilder.merchantAccountId(options.getString("merchantAccountId"));
        // }
        if (options.hasKey("cardholderName")) {
            cardBuilder.cardholderName(options.getString("cardholderName"));
        }
        if (options.hasKey("firstName")) {
            cardBuilder.firstName(options.getString("firstName"));
        }
        if (options.hasKey("lastName")) {
            cardBuilder.lastName(options.getString("lastName"));
        }
        if (options.hasKey("company")) {
            cardBuilder.company(options.getString("company"));
        }
        if (options.hasKey("countryCode")) {
            cardBuilder.countryCode(options.getString("countryCode"));
        }
        if (options.hasKey("locality")) {
            cardBuilder.locality(options.getString("locality"));
        }
        if (options.hasKey("postalCode")) {
            cardBuilder.postalCode(options.getString("postalCode"));
        }
        if (options.hasKey("region")) {
            cardBuilder.region(options.getString("region"));
        }
        if (options.hasKey("streetAddress")) {
            cardBuilder.streetAddress(options.getString("streetAddress"));
        }
        if (options.hasKey("extendedAddress")) {
            cardBuilder.extendedAddress(options.getString("extendedAddress"));
        }
        if (options.hasKey("extendedAddress")) {
            cardBuilder.extendedAddress(options.getString("extendedAddress"));
        }
        if (!options.hasKey("validate")) {
            cardBuilder.validate(false);
        } else {
            cardBuilder.validate(options.getBoolean("validate"));
        }
        Card.tokenize(mBraintreeFragment, cardBuilder);
    }

    @ReactMethod
    public void showDropIn(final ReadableMap options, final Promise promise) {
        if (!validateParams(options, promise)) return;

        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            promise.reject("NO_ACTIVITY", "There is no current activity");
            return;
        }

        DropInRequest dropInRequest = new DropInRequest().clientToken(options.getString("clientToken"));      
        boolean isEnablePayPalDropIn = false;  
        if (options.hasKey("enablePayPal")) {
            isEnablePayPalDropIn = options.getBoolean("enablePayPal");
        }
        if (!isEnablePayPalDropIn) {
            dropInRequest.disablePayPal();
        }
        
        if (options.hasKey("vaultManager")) {
            dropInRequest.vaultManager(options.getBoolean("vaultManager"));
        }

        if (options.hasKey("cardholderNameStatus")) {
            dropInRequest.cardholderNameStatus(options.getInt("cardholderNameStatus"));
        }

        // Check 3DS options
        if (threeDSecureOptions != null) {
            dropInRequest
                .requestThreeDSecureVerification(true)
                .threeDSecureRequest(getThreeDSecureRequest(threeDSecureOptions, null));
        }
        
        currentActivity.startActivityForResult(dropInRequest.getIntent(currentActivity), REQUEST_CODE);
    }

    private final ActivityEventListener mActivityListener = new BaseActivityEventListener() {
        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
            // super.onActivityResult(requestCode, resultCode, data);
            if (requestCode != REQUEST_CODE || mPromise == null) {
                return;
            }

            if (resultCode == Activity.RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce paymentMethodNonce = result.getPaymentMethodNonce();

                if (threeDSecureOptions != null && paymentMethodNonce instanceof CardNonce) {
                    CardNonce cardNonce = (CardNonce) paymentMethodNonce;
                    checkCardThreeDSecure(cardNonce, mPromise);
                } else {
                    if (paymentMethodNonce instanceof CardNonce) {
                        CardNonce cardNonce = (CardNonce) paymentMethodNonce;
                        mPromise.resolve(getPaymentNonce(cardNonce.getNonce(), cardNonce.getTypeLabel(), cardNonce.getLastFour(), cardNonce.isDefault()));
                    } else {
                        String lastDigits = "";
                        if (paymentMethodNonce.getDescription() != null) {
                            lastDigits = paymentMethodNonce.getDescription().replaceAll("\\D+","");
                        }
                        mPromise.resolve(getPaymentNonce(paymentMethodNonce.getNonce(), paymentMethodNonce.getTypeLabel(), lastDigits, paymentMethodNonce.isDefault()));
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                mPromise.reject("USER_CANCELLATION", "The user cancelled");
            } else {
                Exception exception = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                mPromise.reject(exception.getMessage(), exception.getMessage());
            }

            mPromise = null;
        }
    };
}
