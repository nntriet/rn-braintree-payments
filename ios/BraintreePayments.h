#import <React/RCTBridgeModule.h>

#import "BraintreeCore.h"
#import "BraintreePayPal.h"
#import "BraintreeCard.h"
#import "BraintreeDropIn.h"
#import "Braintree3DSecure.h"
#import "BraintreePaymentFlow.h"

#import "BTThreeDSecureRequest.h"

@interface BraintreePayments : NSObject <RCTBridgeModule>

@property (nonatomic, strong) BTAPIClient *braintreeClient;
@property (nonatomic, strong, readwrite) BTPaymentFlowDriver *paymentFlowDriver;
@property (nonatomic, strong, readwrite) BTThreeDSecureDriver *threeDSecure;
@property (nonatomic, strong) UIViewController* _Nonnull reactRoot;

+ (instancetype)sharedInstance;
+ (NSMutableDictionary *)getPaymentNonce:(BTDropInResult* _Nullable)result;
+ (BTThreeDSecureRequest *)getThreeDSecureRequest:(NSDictionary *)threeDSecureOpts nonce:(NSString* _Nullable)nonce;
+ (BOOL)validateParams:(NSDictionary *)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
+ (BOOL)setup:(NSDictionary *)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;

@end