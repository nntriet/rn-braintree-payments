#import "BraintreePayments.h"

@implementation BraintreePayments

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()



+ (NSString *)extractNumberFromText:(NSString *)text
{
  NSCharacterSet *nonDigitCharacterSet = [[NSCharacterSet decimalDigitCharacterSet] invertedSet];
  return [[text componentsSeparatedByCharactersInSet:nonDigitCharacterSet] componentsJoinedByString:@""];
}

+ (BTThreeDSecureRequest *)getThreeDSecureRequest: (NSDictionary *)threeDSecureOptions nonce:(NSString* _Nullable)nonce
{
    BTThreeDSecureRequest *threeDSecureRequest = [[BTThreeDSecureRequest alloc] init];
    threeDSecureRequest.versionRequested = BTThreeDSecureVersion2;
    threeDSecureRequest.amount = threeDSecureOptions[@"amount"];
    threeDSecureRequest.email = threeDSecureOptions[@"email"];

    BTThreeDSecurePostalAddress *address = [BTThreeDSecurePostalAddress new];
    address.givenName = threeDSecureOptions[@"givenName"]; // ASCII-printable characters required, else will throw a validation error
    address.surname = threeDSecureOptions[@"surname"]; // ASCII-printable characters required, else will throw a validation error
    address.phoneNumber = threeDSecureOptions[@"phoneNumber"];
    address.streetAddress = threeDSecureOptions[@"streetAddress"];
    address.extendedAddress = threeDSecureOptions[@"extendedAddress"];
    address.locality = threeDSecureOptions[@"locality"];
    address.region = threeDSecureOptions[@"region"];
    address.postalCode = threeDSecureOptions[@"postalCode"];
    address.countryCodeAlpha2 = threeDSecureOptions[@"countryCodeAlpha2"];

    // Optional additional information.
    // For best results, provide as many of these elements as possible.
    BTThreeDSecureAdditionalInformation *additionalInformation = [BTThreeDSecureAdditionalInformation new];
    additionalInformation.shippingAddress = address;

    threeDSecureRequest.billingAddress = address;
    threeDSecureRequest.additionalInformation = additionalInformation;
    if(nonce) {
        threeDSecureRequest.nonce = nonce;
    }
    return threeDSecureRequest;
}

RCT_REMAP_METHOD(showDropIn,
                 showDropInWithOptions:(NSDictionary*)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    BOOL isValidParams = [self validateParams :options resolver:resolve rejecter:reject];
    if (!isValidParams) {  
       return;
    }

    BTDropInRequest *request = [[BTDropInRequest alloc] init];
    if(!options[@"enablePayPal"]) {
        request.paypalDisabled = YES;
    } else {
        BOOL enablePayPal = [options[@"enablePayPal"] boolValue];
        request.paypalDisabled = !enablePayPal;
    }
    if (options[@"vaultManager"]) {
        request.vaultManager = [options[@"vaultManager"] boolValue];
    }
    if (options[@"cardholderNameSetting"] != nil) {
        request.cardholderNameSetting = options[@"cardholderNameStatus"];
    }

    

    NSDictionary* threeDSecureOptions = options[@"threeDSecure"];
    if (threeDSecureOptions) {
        BTThreeDSecureRequest *threeDSecureRequest = [[self class] getThreeDSecureRequest :threeDSecureOptions nonce:nil];
        if (!threeDSecureRequest) {
            request.threeDSecureVerification = YES;
            request.threeDSecureRequest = threeDSecureRequest;
        }
    }

    NSString* clientToken = options[@"clientToken"];
    BTDropInController *dropIn = [[BTDropInController alloc] initWithAuthorization:clientToken request:request handler:^(BTDropInController * _Nonnull controller, BTDropInResult * _Nullable result, NSError * _Nullable error) {
            [self.reactRoot dismissViewControllerAnimated:YES completion:nil];

            if (error) {
                reject(error.localizedDescription, error.localizedDescription, error);
            } else if (result.cancelled) {
                reject(@"USER_CANCELLATION", @"The user cancelled", nil);
            } else {
                if (threeDSecureOptions && [result.paymentMethod isKindOfClass:[BTCardNonce class]]) {
                    BTCardNonce *cardNonce = (BTCardNonce *)result.paymentMethod;
                    if (!cardNonce.threeDSecureInfo.liabilityShiftPossible && cardNonce.threeDSecureInfo.wasVerified) {
                        reject(@"3DSECURE_NOT_ABLE_TO_SHIFT_LIABILITY", @"3D Secure liability cannot be shifted", nil);
                    } else if (!cardNonce.threeDSecureInfo.liabilityShifted && cardNonce.threeDSecureInfo.wasVerified) {
                        reject(@"3DSECURE_LIABILITY_NOT_SHIFTED", @"3D Secure liability was not shifted", nil);
                    } else {
                        resolve( [self getPaymentNonce :cardNonce.nonce type:cardNonce.type lastDigits:cardNonce.lastFour isDefault:cardNonce.isDefault ] );
                    }
                } else {
                    if ( [result.paymentMethod isKindOfClass:[BTCardNonce class]] ) {
                        BTCardNonce *cardNonce = (BTCardNonce *)result.paymentMethod;
                        resolve( [self getPaymentNonce :cardNonce.nonce type:cardNonce.type lastDigits:cardNonce.lastFour isDefault:cardNonce.isDefault ] );
                    } else {
                        resolve( [self getPaymentNonce :result.paymentMethod.nonce type:result.paymentMethod.type lastDigits:result.paymentDescription isDefault:result.paymentMethod.isDefault ] );
                    }
                }
            }
        }];

    if (dropIn) {
        [self.reactRoot presentViewController:dropIn animated:YES completion:nil];
    } else {
        reject(@"INVALID_CLIENT_TOKEN", @"The client token seems invalid", nil);
    }
}

RCT_REMAP_METHOD(showPayPal,
                 showPayPalWithOptions:(NSDictionary*)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    BOOL isSetup = [self setup :options resolver:resolve rejecter:reject];
    if (!isSetup) {  
       return;
    }

    BTPayPalRequest *request;
    NSNumber* amount = options[@"amount"];
    if (amount != nil) {
        request = [[BTPayPalRequest alloc] initWithAmount:[amount stringValue]];
        if (options[@"offerCredit"]) {
            request.offerCredit = [options[@"offerCredit"] boolValue];
        }
        [self startPayPalCheckout:request resolver:resolve rejecter:reject];
    } else {
        request = [[BTPayPalRequest alloc] init];
        if (options[@"offerCredit"]) {
            request.offerCredit = [options[@"offerCredit"] boolValue];
        }
        [self startPayPal:request resolver:resolve rejecter:reject];
    }
}

RCT_REMAP_METHOD(getCardNonce,
                 getCardNonceWithOptions:(NSDictionary*)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    BOOL isSetup = [self setup :options resolver:resolve rejecter:reject];
    if (!isSetup) {  
       return;
    }

    BTCardClient *cardClient = [[BTCardClient alloc] initWithAPIClient:self.braintreeClient];
    BTCard *card = [[BTCard alloc] initWithParameters:options];
    if(!options[@"validate"]) {
        card.shouldValidate = NO;
    } else {
        BOOL validate = [options[@"validate"] boolValue];
        card.shouldValidate = validate;
    }
    

    [cardClient tokenizeCard:card
                    completion:^(BTCardNonce *tokenizedCard, NSError *cardClientError) {
        if (cardClientError) {
            reject(cardClientError.localizedDescription, cardClientError.localizedDescription, cardClientError);
        } else if (tokenizedCard) {
            NSDictionary* threeDSecureOptions = options[@"threeDSecure"];
            if (threeDSecureOptions) {
                BTThreeDSecureRequest *threeDSecureRequest = [[self class] getThreeDSecureRequest :threeDSecureOptions nonce:tokenizedCard.nonce];
                if (!threeDSecureRequest) {
                    // Make sure that self conforms to the BTThreeDSecureRequestDelegate protocol
                    threeDSecureRequest.threeDSecureRequestDelegate = self;

                    [self.paymentFlowDriver startPaymentFlow:threeDSecureRequest completion:^(BTPaymentFlowResult *result, NSError *paymentFlowDriverError) {
                        if (paymentFlowDriverError) {
                            reject(paymentFlowDriverError.localizedDescription, paymentFlowDriverError.localizedDescription, paymentFlowDriverError);
                        } else if (result) {
                            BTThreeDSecureResult *threeDSecureResult = (BTThreeDSecureResult *)result;
                            BTCardNonce *cardNonce = (BTCardNonce *)threeDSecureResult.tokenizedCard;
                            if (!cardNonce.threeDSecureInfo.liabilityShiftPossible && cardNonce.threeDSecureInfo.wasVerified) {
                                // 3D Secure authentication was not possible
                                reject(@"3DSECURE_NOT_ABLE_TO_SHIFT_LIABILITY", @"3D Secure liability cannot be shifted", nil);
                            } else if (!cardNonce.threeDSecureInfo.liabilityShifted && cardNonce.threeDSecureInfo.wasVerified) {
                                // 3D Secure authentication failed
                                reject(@"3DSECURE_LIABILITY_NOT_SHIFTED", @"3D Secure liability was not shifted", nil);
                            } else {
                                // 3D Secure authentication success
                                resolve( [self getPaymentNonce :cardNonce.nonce type:cardNonce.type lastDigits:cardNonce.lastFour isDefault:cardNonce.isDefault ] );
                            }
                        } else {
                            reject(@"USER_CANCELLATION", @"The user cancelled", nil);
                        }
                            
                    
                    }];
                }
                
                
            } else {
                resolve( [self getPaymentNonce :tokenizedCard.nonce type:tokenizedCard.type lastDigits:tokenizedCard.lastFour isDefault:tokenizedCard.isDefault ] );
            }
        } else {
            reject(@"USER_CANCELLATION", @"The user cancelled", nil);
        }
    }];
}

- (BOOL)validateParams:(NSDictionary *)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject
{
    NSString* clientToken = options[@"clientToken"];
    if (!clientToken) {
        reject(@"NO_CLIENT_TOKEN", @"You must provide a client token", nil);
        return NO;
    }
    NSDictionary* threeDSecureOptions = options[@"threeDSecure"];
    if (threeDSecureOptions) {
        NSNumber* threeDSecureAmount = threeDSecureOptions[@"amount"];
        if (!threeDSecureAmount) {
            reject(@"NO_3DS_AMOUNT", @"You must provide an amount for 3D Secure", nil);
            return NO;
        }
    }

    self.clientToken = clientToken;
    return YES;
}

- (BOOL)setup:(NSDictionary *)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject
{
    BOOL isValidParams = [self validateParams :options resolver:resolve rejecter:reject];
    if (!isValidParams) {  
       return NO;
    }

    NSString* clientToken = options[@"clientToken"];
    self.braintreeClient = [[BTAPIClient alloc] initWithAuthorization:clientToken];
    if (!self.braintreeClient) {
        reject(@"NO_BRAINTREE_CLIENT", @"There is no self.braintreeClient", nil);
        return NO;
    }
    return YES;
}

- (NSMutableDictionary *)getPaymentNonce:(NSString* _Nonnull)nonce type:(NSString* _Nonnull)type lastDigits:(NSString* _Nullable)lastDigits isDefault:(BOOL)isDefault
{
    NSString *extractNumberFromLastDigits = [[self class] extractNumberFromText :lastDigits];
    NSMutableDictionary* jsResult = [NSMutableDictionary new];
    [jsResult setObject:nonce forKey:@"nonce"];
    [jsResult setObject:type forKey:@"type"];
    [jsResult setObject:extractNumberFromLastDigits forKey:@"lastDigits"];
    [jsResult setObject:self.clientToken forKey:@"clientToken"];
    [jsResult setObject:[NSNumber numberWithBool:isDefault] forKey:@"isDefault"];
    return jsResult;
    // resolve(jsResult);
}

- (void)startPayPal:(BTPayPalRequest *)paypalRequest resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject
{
    BTPayPalDriver *paypalDriver = [[BTPayPalDriver alloc] initWithAPIClient:self.braintreeClient];
    paypalDriver.viewControllerPresentingDelegate = self;
    // paypalDriver.appSwitchDelegate = self; // Optional

    [paypalDriver requestBillingAgreement:paypalRequest
                            completion:^(BTPayPalAccountNonce *tokenizedPayPalAccount, NSError *error) {
        if (error) {
            reject(error.localizedDescription, error.localizedDescription, error);
        } else if (tokenizedPayPalAccount) {
            resolve( [self getPaymentNonce :tokenizedPayPalAccount.nonce type:tokenizedPayPalAccount.type lastDigits:@"Paypal" isDefault:tokenizedPayPalAccount.isDefault ] );
        } else {
            reject(@"USER_CANCELLATION", @"The user cancelled", nil);
        }
    }];
}

- (void)startPayPalCheckout:(BTPayPalRequest *)paypalRequest resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject
{
    BTPayPalDriver *paypalDriver = [[BTPayPalDriver alloc] initWithAPIClient:self.braintreeClient];
    paypalDriver.viewControllerPresentingDelegate = self;
    // paypalDriver.appSwitchDelegate = self; // Optional

    [paypalDriver requestOneTimePayment:paypalRequest
                            completion:^(BTPayPalAccountNonce *tokenizedPayPalAccount, NSError *error) {
        if (error) {
            reject(error.localizedDescription, error.localizedDescription, error);
        } else if (tokenizedPayPalAccount) {
            resolve( [self getPaymentNonce :tokenizedPayPalAccount.nonce type:tokenizedPayPalAccount.type lastDigits:@"Paypal" isDefault:tokenizedPayPalAccount.isDefault ] );
        } else {
            reject(@"USER_CANCELLATION", @"The user cancelled", nil);
        }
    }];
}

#pragma mark - BTViewControllerPresentingDelegate

- (void)paymentDriver:(id)paymentDriver requestsPresentationOfViewController:(UIViewController *)viewController 
{
    [self.reactRoot presentViewController:viewController animated:YES completion:nil];
}

- (void)paymentDriver:(id)paymentDriver requestsDismissalOfViewController:(UIViewController *)viewController 
{
    if (!viewController.isBeingDismissed) {
        [viewController.presentingViewController dismissViewControllerAnimated:YES completion:nil];
    }
}

- (void)setupPaymentFlowDriver 
{
    self.paymentFlowDriver = [[BTPaymentFlowDriver alloc] initWithAPIClient:self.braintreeClient];
    self.paymentFlowDriver.viewControllerPresentingDelegate = self;
}

- (UIViewController*)reactRoot 
{
    UIViewController *root  = [UIApplication sharedApplication].keyWindow.rootViewController;
    UIViewController *maybeModal = root.presentedViewController;

    UIViewController *modalRoot = root;

    if (maybeModal != nil) {
        modalRoot = maybeModal;
    }

    return modalRoot;
}

@end
