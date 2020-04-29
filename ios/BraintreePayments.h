#import <React/RCTBridgeModule.h>

#import "BraintreeCore.h"
#import "BraintreePayPal.h"
#import "BraintreeCard.h"
#import "BraintreeDropIn.h"
#import "BraintreePaymentFlow.h"

@interface BraintreePayments : NSObject <RCTBridgeModule>

@property (nonatomic, strong) NSString *clientToken;
@property (nonatomic, strong) BTAPIClient *braintreeClient;
@property (nonatomic, strong, readwrite) BTPaymentFlowDriver *paymentFlowDriver;
@property (nonatomic, strong) UIViewController* _Nonnull reactRoot;

+ (NSString *)extractNumberFromText:(NSString *)text;
+ (BTThreeDSecureRequest *)getThreeDSecureRequest:(NSDictionary *)threeDSecureOptions nonce:(NSString* _Nullable)nonce;

- (BOOL)validateParams:(NSDictionary *)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
- (BOOL)setup:(NSDictionary *)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
- (NSMutableDictionary *)getPaymentNonce:(NSString* _Nonnull)nonce type:(NSString* _Nonnull)type lastDigits:(NSString* _Nullable)lastDigits isDefault:(BOOL)isDefault;
- (void)startPayPal:(BTPayPalRequest *)paypalRequest resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
- (void)startPayPalCheckout:(BTPayPalRequest *)paypalRequest resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject
- (void)setupPaymentFlowDriver

@end
