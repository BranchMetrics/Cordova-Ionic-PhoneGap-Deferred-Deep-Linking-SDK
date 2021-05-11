#import "BranchSDK.h"

NSString * const pluginVersion = @"%BRANCH_PLUGIN_VERSION%";

@interface BranchSDK()

@property (strong, nonatomic) NSString *deepLinkUrl;

- (void)doShareLinkResponse:(int)callbackId sendResponse:(NSDictionary*)response;

@end

@implementation BranchSDK

- (void)pluginInitialize
{
  self.branchUniversalObjArray = [[NSMutableArray alloc] init];
  [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleOpenURLNotification:) name:CDVPluginHandleOpenURLNotification object:nil];
}

- (void)handleOpenURLNotification:(NSNotification*)notification
{
    NSURL* url = [notification object];
    [[Branch getInstance] application:[UIApplication sharedApplication]  openURL:url options:@{}];
}

#pragma mark - Private APIs
#pragma mark - Global Instance Accessors

- (Branch *)getInstance
{
  return [Branch getInstance];
}

- (Branch *)getInstance:(NSString *)branchKey
{
  if (branchKey) {
    return [Branch getInstance:branchKey];
  }
  else {
    return [Branch getInstance];
  }
}

- (Branch *)getTestInstance
{
  return [Branch getTestInstance];
}

#pragma mark - Deep Linking Handlers

- (id)handleDeepLink:(CDVInvokedUrlCommand*)command
{
  NSString *arg = [command.arguments objectAtIndex:0];
  NSURL *url = [NSURL URLWithString:arg];
  self.deepLinkUrl = [url absoluteString];

  return [NSNumber numberWithBool:[[Branch getInstance] handleDeepLink:url]];
}

- (id)handleDeepLinkWithNewSession:(CDVInvokedUrlCommand*)command
{
  NSString *arg = [command.arguments objectAtIndex:0];
  NSURL *url = [NSURL URLWithString:arg];
  self.deepLinkUrl = [url absoluteString];

  return [NSNumber numberWithBool:[[Branch getInstance] handleDeepLinkWithNewSession:url]];
}

- (void)continueUserActivity:(CDVInvokedUrlCommand*)command
{

  NSString *activityType = (NSString *)[command.arguments objectAtIndex:0];
  NSDictionary *userInfo = (NSDictionary*)[command.arguments objectAtIndex:1];

  NSUserActivity *userActivity = [[NSUserActivity alloc] initWithActivityType:activityType];
  [userActivity setUserInfo:userInfo];

  if ([userActivity.activityType isEqualToString:NSUserActivityTypeBrowsingWeb]) {
    self.deepLinkUrl = [userActivity.webpageURL absoluteString];
  }

  [[Branch getInstance] continueUserActivity:userActivity];
}

#pragma mark - Public APIs
#pragma mark - Branch Basic Methods

- (void)enableTestMode:(CDVInvokedUrlCommand*)command
{
  [Branch setUseTestBranchKey:TRUE];
}

- (void)initSession:(CDVInvokedUrlCommand*)command
{
  [[Branch getInstance] registerPluginName:@"CordovaIonic" version:pluginVersion];
  [[Branch getInstance] initSessionWithLaunchOptions:nil andRegisterDeepLinkHandler:^(NSDictionary *params, NSError *error) {

    NSString *resultString = nil;
    CDVPluginResult *pluginResult = nil;
    bool enableCallBack = [[command.arguments objectAtIndex:0] boolValue];

    if (!error) {
      if (params != nil && [params count] > 0) {

        NSError *err;
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params options:0 error:&err];

        if (!jsonData) {
          NSLog(@"Parsing Error: %@", [err localizedDescription]);
          NSDictionary *errorDict = [NSDictionary dictionaryWithObjectsAndKeys:[err localizedDescription], @"error", nil];
          NSData* errorJSON = [NSJSONSerialization dataWithJSONObject:errorDict options:NSJSONWritingPrettyPrinted error:&err];

          resultString = [[NSString alloc] initWithData:errorJSON encoding:NSUTF8StringEncoding];
          pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:resultString];
        } else {
          resultString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
          pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:params];
        }
      }
    }
    else {
      NSLog(@"Init Error: %@", [error localizedDescription]);

      // We create a JSON string result, because we're getting an error if we directly return a string result.
      NSDictionary *errorDict = [NSDictionary dictionaryWithObjectsAndKeys:[error localizedDescription], @"error", nil];
      NSData* errorJSON = [NSJSONSerialization dataWithJSONObject:errorDict options:NSJSONWritingPrettyPrinted error:&error];

      resultString = [[NSString alloc] initWithData:errorJSON encoding:NSUTF8StringEncoding];
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:resultString];
    }

    if (command != nil) {
      if(enableCallBack){
        [pluginResult setKeepCallbackAsBool:YES];
      }
      [self.commandDelegate sendPluginResult: pluginResult callbackId: command.callbackId];
    }
  }];
}

- (void)setRequestMetadata:(CDVInvokedUrlCommand*)command
{

  [[Branch getInstance] setRequestMetadataKey:[command.arguments objectAtIndex:0] value:[command.arguments objectAtIndex:1]];

}

- (void)disableTracking:(CDVInvokedUrlCommand*)command
{

  bool enabled = [[command.arguments objectAtIndex:0] boolValue];
  [Branch setTrackingDisabled:enabled];

  CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:enabled];

  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)setDebug:(CDVInvokedUrlCommand*)command
{
  bool enableDebug = [[command.arguments objectAtIndex:0] boolValue];
  if (enableDebug) {
    [[Branch getInstance] setDebug];
  }

  CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:enableDebug];

  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)getAutoInstance:(CDVInvokedUrlCommand*)command
{
  [self initSession:nil];
}

- (void)getLatestReferringParams:(CDVInvokedUrlCommand*)command
{
  Branch *branch = [self getInstance];
  NSDictionary *sessionParams = [branch getLatestReferringParams];

  CDVPluginResult* pluginResult = nil;

  if (sessionParams != nil && [sessionParams count] > 0) {
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:sessionParams];
  } else {
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:FALSE];
  }
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)getFirstReferringParams:(CDVInvokedUrlCommand*)command
{
  Branch *branch = [self getInstance];
  NSDictionary *installParams = [branch getFirstReferringParams];

  CDVPluginResult* pluginResult = nil;

  if (installParams != nil && [installParams count] > 0) {
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:installParams];
  } else {
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:FALSE];
  }
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)setIdentity:(CDVInvokedUrlCommand*)command
{
  Branch *branch = [self getInstance];

  [branch setIdentity:[command.arguments objectAtIndex:0] withCallback:^(NSDictionary *params, NSError *error) {

    CDVPluginResult* pluginResult = nil;
    if (!error) {
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:params];
    }
    else {
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error localizedDescription]];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  }];
}

- (void)registerDeepLinkController:(CDVInvokedUrlCommand*)command
{
  UIViewController<BranchDeepLinkingController> *controller = (UIViewController<BranchDeepLinkingController>*)self.viewController;
  Branch *branch = [self getInstance];
  [branch registerDeepLinkController:controller forKey:[command.arguments objectAtIndex:0]];
}

- (void)userCompletedAction:(CDVInvokedUrlCommand*)command
{
  NSString *name;
  NSDictionary *state;

  // if a state dictionary is passed as an argument
  if ([command.arguments count] == 2) {
    name = [command.arguments objectAtIndex:0];
    state = [command.arguments objectAtIndex:1];
  }
  else {
    name = [command.arguments objectAtIndex:0];
  }

  Branch *branch = [self getInstance];

  if (state) {
    [branch userCompletedAction:name withState:state];
  }
  else {
    [branch userCompletedAction:name];
  }

  // TODO: iOS Branch.userCompletedAction needs a callback for success or failure
  CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: @"Success"];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

-(void)sendBranchEvent:(CDVInvokedUrlCommand*)command
{
    NSString *eventName = [command.arguments objectAtIndex:0];
    NSDictionary *metadata;
    if ([command.arguments count] == 2) {
        metadata = [command.arguments objectAtIndex:1];
    }
    BranchEvent *event = [BranchEvent customEventWithName:eventName];
    for (id key in metadata) {
        if ([key isEqualToString:@"transactionID"]) {
            event.transactionID = [metadata objectForKey:key];
        }
        else if ([key isEqualToString:@"currency"]) {
            event.currency = [metadata objectForKey:key];
        }
        else if ([key isEqualToString:@"shipping"]) {
            NSString *value = ([[metadata objectForKey:key] isKindOfClass:[NSString class]]) ? [metadata objectForKey:key] : [[metadata objectForKey:key] stringValue];
            event.shipping = [NSDecimalNumber decimalNumberWithString:value];
        }
        else if ([key isEqualToString:@"tax"]) {
            NSString *value = ([[metadata objectForKey:key] isKindOfClass:[NSString class]]) ? [metadata objectForKey:key] : [[metadata objectForKey:key] stringValue];
            event.tax = [NSDecimalNumber decimalNumberWithString:value];
        }
        else if ([key isEqualToString:@"coupon"]) {
            event.coupon = [metadata objectForKey:key];
        }
        else if ([key isEqualToString:@"affiliation"]) {
            event.affiliation = [metadata objectForKey:key];
        }
        else if ([key isEqualToString:@"eventDescription"]) {
            event.eventDescription = [metadata objectForKey:key];
        }
        else if ([key isEqualToString:@"revenue"]) {
            NSString *value = ([[metadata objectForKey:key] isKindOfClass:[NSString class]]) ? [metadata objectForKey:key] : [[metadata objectForKey:key] stringValue];
            event.revenue = [NSDecimalNumber decimalNumberWithString:value];
        }
        else if ([key isEqualToString:@"searchQuery"]) {
            event.searchQuery = [metadata objectForKey:key];
        }
        else if ([key isEqualToString:@"description"]) {
            event.eventDescription = [metadata objectForKey:key];
        }
        else if ([key isEqualToString:@"customerEventAlias"]) {
            event.alias = [metadata objectForKey:key];
        }
        else if ([key isEqualToString:@"customData"] && [[metadata objectForKey:key] isKindOfClass:[NSMutableDictionary class]]) {
            event.customData = [metadata objectForKey:key];
        }
    }
    [event logEvent];
}

- (void)sendCommerceEvent:(CDVInvokedUrlCommand*)command
{
  NSDictionary *data = [command.arguments objectAtIndex:0];
  NSDictionary *metadata;
  BNCCommerceEvent *commerce = [[BNCCommerceEvent alloc] init];
  NSArray *categories = [NSArray arrayWithObjects:BNCProductCategoryAnimalSupplies, BNCProductCategoryApparel, BNCProductCategoryArtsEntertainment, BNCProductCategoryBabyToddler, BNCProductCategoryBusinessIndustrial, BNCProductCategoryCamerasOptics, BNCProductCategoryElectronics, BNCProductCategoryFoodBeverageTobacco, BNCProductCategoryFurniture, BNCProductCategoryHardware, BNCProductCategoryHealthBeauty, BNCProductCategoryHomeGarden, BNCProductCategoryLuggageBags, BNCProductCategoryMature, BNCProductCategoryMedia, BNCProductCategoryOfficeSupplies, BNCProductCategoryReligious, BNCProductCategorySoftware, BNCProductCategorySportingGoods, BNCProductCategoryToysGames, BNCProductCategoryVehiclesParts, nil];
  NSArray *currencies = [NSArray arrayWithObjects:@"AED", @"AFN", @"ALL", @"AMD", @"ANG", @"AOA", @"ARS", @"AUD", @"AWG", @"AZN", @"BAM", @"BBD", @"BDT", @"BGN", @"BHD", @"BIF", @"BMD", @"BND", @"BOB", @"BOV", @"BRL", @"BSD", @"BTN", @"BWP", @"BYN", @"BYR", @"BZD", @"CAD", @"CDF", @"CHE", @"CHF", @"CHW", @"CLF", @"CLP", @"CNY", @"COP", @"COU", @"CRC", @"CUC", @"CUP", @"CVE", @"CZK", @"DJF", @"DKK", @"DOP", @"DZD", @"EGP", @"ERN", @"ETB", @"EUR", @"FJD", @"FKP", @"GBP", @"GEL", @"GHS", @"GIP", @"GMD", @"GNF", @"GTQ", @"GYD", @"HKD", @"HNL", @"HRK", @"HTG", @"HUF", @"IDR", @"ILS", @"INR", @"IQD", @"IRR", @"ISK", @"JMD", @"JOD", @"JPY", @"KES", @"KGS", @"KHR", @"KMF", @"KPW", @"KRW", @"KWD", @"KYD", @"KZT", @"LAK", @"LBP", @"LKR", @"LRD", @"LSL", @"LYD", @"MAD", @"MDL", @"MGA", @"MKD", @"MMK", @"MNT", @"MOP", @"MRO", @"MUR", @"MVR", @"MWK", @"MXN", @"MXV", @"MYR", @"MZN", @"NAD", @"NGN", @"NIO", @"NOK", @"NPR", @"NZD", @"OMR", @"PAB", @"PEN", @"PGK", @"PHP", @"PKR", @"PLN", @"PYG", @"QAR", @"RON", @"RSD", @"RUB", @"RWF", @"SAR", @"SBD", @"SCR", @"SDG", @"SEK", @"SGD", @"SHP", @"SLL", @"SOS", @"SRD", @"SSP", @"STD", @"SYP", @"SZL", @"THB", @"TJS", @"TMT", @"TND", @"TOP", @"TRY", @"TTD", @"TWD", @"TZS", @"UAH", @"UGX", @"USD", @"USN", @"UYI", @"UYU", @"UZS", @"VEF", @"VND", @"VUV", @"WST", @"XAF", @"XAG", @"XAU", @"XBA", @"XBB", @"XBC", @"XBD", @"XCD", @"XDR", @"XFU", @"XOF", @"XPD", @"XPF", @"XPT", @"XSU", @"XTS", @"XUA", @"XXX", @"YER", @"ZAR", @"ZMW", nil];

  if ([command.arguments count] == 2) {
    metadata = [command.arguments objectAtIndex:1];
  }

  for (id key in data) {
    if ([key isEqualToString:@"revenue"]) {
      NSString *value = ([[data objectForKey:key] isKindOfClass:[NSString class]]) ? [data objectForKey:key] : [[data objectForKey:key] stringValue];
      commerce.revenue = [NSDecimalNumber decimalNumberWithString:value];
    }
    else if ([key isEqualToString:@"currency"]) {
      NSString *value = ([[data objectForKey:key] isKindOfClass:[NSString class]]) ? [data objectForKey:key] : [[data objectForKey:key] stringValue];
      commerce.currency = [currencies objectAtIndex:[value intValue]];
    }
    else if ([key isEqualToString:@"transactionID"]) {
      commerce.transactionID = [data objectForKey:key];
    }
    else if ([key isEqualToString:@"shipping"]) {
      NSString *value = ([[data objectForKey:key] isKindOfClass:[NSString class]]) ? [data objectForKey:key] : [[data objectForKey:key] stringValue];
      commerce.shipping = [NSDecimalNumber decimalNumberWithString:value];
    }
    else if ([key isEqualToString:@"tax"]) {
      NSString *value = ([[data objectForKey:key] isKindOfClass:[NSString class]]) ? [data objectForKey:key] : [[data objectForKey:key] stringValue];
      commerce.tax = [NSDecimalNumber decimalNumberWithString:value];
    }
    else if ([key isEqualToString:@"coupon"]) {
      commerce.coupon = [data objectForKey:key];
    }
    else if ([key isEqualToString:@"affiliation"]) {
      commerce.affiliation = [data objectForKey:key];
    }
    else if ([key isEqualToString:@"products"] && [[data objectForKey:key] isKindOfClass:[NSArray class]]) {
      NSMutableArray *products = [[NSMutableArray alloc] init];
      NSArray *dataProducts = [data objectForKey:key];
      for (NSDictionary *dataDictionary in dataProducts) {
        BNCProduct *product = [[BNCProduct alloc] init];
        for (id key in dataDictionary) {
          if ([key isEqualToString:@"sku"]) {
            product.sku = [dataDictionary objectForKey:key];
          }
          else if ([key isEqualToString:@"name"]) {
            product.name = [dataDictionary objectForKey:key];
          }
          else if ([key isEqualToString:@"price"]) {
            NSString *value = ([[dataDictionary objectForKey:key] isKindOfClass:[NSString class]]) ? [dataDictionary objectForKey:key] : [[dataDictionary objectForKey:key] stringValue];
            product.price = [NSDecimalNumber decimalNumberWithString:value];
          }
          else if ([key isEqualToString:@"quantity"]) {
            NSString *value = ([[dataDictionary objectForKey:key] isKindOfClass:[NSString class]]) ? [dataDictionary objectForKey:key] : [[dataDictionary objectForKey:key] stringValue];
            product.quantity = [NSNumber numberWithInt:[value intValue]];
          }
          else if ([key isEqualToString:@"brand"]) {
            product.brand = [dataDictionary objectForKey:key];
          }
          else if ([key isEqualToString:@"category"]) {
            NSString *value = ([[dataDictionary objectForKey:key] isKindOfClass:[NSString class]]) ? [dataDictionary objectForKey:key] : [[dataDictionary objectForKey:key] stringValue];
            product.category = [categories objectAtIndex:[value intValue]];
          }
          else if ([key isEqualToString:@"variant"]) {
            product.variant = [dataDictionary objectForKey:key];
          }
        }
        [products addObject:product];
      }
      commerce.products = products;
    }
  }

  [[Branch getInstance] sendCommerceEvent:commerce metadata:metadata withCompletion:^(NSDictionary *response, NSError *error) {
    CDVPluginResult *pluginResult = nil;
    if (!error) {
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: @"Success"];
    } else {
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error localizedDescription]];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  }];
}

- (void)logout:(CDVInvokedUrlCommand*)command
{
  Branch *branch = [self getInstance];
  [branch logoutWithCallback:^(BOOL changed, NSError *error) {
    CDVPluginResult *pluginResult = nil;
    if (!error) {
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:changed];
    } else {
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error localizedDescription]];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  }];
  self.branchUniversalObjArray = [[NSMutableArray alloc] init];
}

- (void)delayInitToCheckForSearchAds:(CDVInvokedUrlCommand*)command
{
  bool enabled = [[command.arguments objectAtIndex:0] boolValue];
  if (enabled) {
    [[Branch getInstance] delayInitToCheckForSearchAds];
  }

  CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:enabled];

  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

#pragma mark - Branch Referral Reward System

- (void)loadRewards:(CDVInvokedUrlCommand*)command
{
  Branch *branch = [self getInstance];
  NSString *bucket = @"";

  if ([command.arguments count] == 1) {
    bucket = [command.arguments objectAtIndex:0];
  }

  [branch loadRewardsWithCallback:^(BOOL changed, NSError *error) {

    CDVPluginResult* pluginResult = nil;
    if(!error) {
      int credits = 0;

      if ([bucket length]) {
        credits = (int)[branch getCreditsForBucket:bucket];
      } else {
        credits = (int)[branch getCredits];
      }

      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:credits];
    }
    else {
      NSLog(@"Load Rewards Error: %@", [error localizedDescription]);
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error localizedDescription]];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  }];
}

- (void)redeemRewards:(CDVInvokedUrlCommand*)command
{

  NSInteger amount = ((NSNumber *)[command.arguments objectAtIndex:0]).integerValue;
  Branch *branch = [self getInstance];

  if ([command.arguments count] == 2) {

    NSString *bucket = [command.arguments objectAtIndex:1];

    [branch redeemRewards:(NSInteger)amount forBucket:(NSString *)bucket callback:^(BOOL changed, NSError *error) {
      CDVPluginResult* pluginResult = nil;
      if (!error) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:changed];
      }
      else {
        NSLog(@"Redeem Rewards Error: %@", [error localizedDescription]);
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error localizedDescription]];
      }
      [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
  } else {
    [branch redeemRewards:amount callback:^(BOOL changed, NSError *error) {
      CDVPluginResult* pluginResult = nil;
      if (!error) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:changed];
      }
      else {
        NSLog(@"Redeem Rewards Error: %@", [error localizedDescription]);
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error localizedDescription]];
      }
      [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
  }
}

- (void)getCreditHistory:(CDVInvokedUrlCommand*)command
{
  Branch *branch = [self getInstance];

  [branch getCreditHistoryWithCallback:^(NSArray *list, NSError *error) {
    CDVPluginResult* pluginResult = nil;
    if (!error) {
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:list];
    }
    else {
      NSLog(@"Credit History Error: %@", [error localizedDescription]);
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error localizedDescription]];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  }];
}

#pragma mark - Branch Universal Object Methods

- (void)createBranchUniversalObject:(CDVInvokedUrlCommand*)command
{
  NSDictionary *properties = [command.arguments objectAtIndex:0];
  BranchUniversalObject *branchUniversalObj = [[BranchUniversalObject alloc] init];

  for (id key in properties) {
    if ([key isEqualToString:@"contentMetadata"]){
      NSDictionary *metadata = (NSDictionary *)[properties valueForKey:key];

      for (id key_ in metadata) {
        [branchUniversalObj addMetadataKey:key_ value:[metadata valueForKey:key_]];
      }
    }
    else if ([key isEqualToString:@"contentIndexingMode"]) {
      NSString *indexingMode = [properties valueForKey:key];
      // Default contentIndexMode is always public
      if ([indexingMode isEqualToString:@"private"]) {
        branchUniversalObj.contentIndexMode = BranchContentIndexModePrivate;
      }
      else {
        branchUniversalObj.contentIndexMode = BranchContentIndexModePublic;
      }
    }
    else if ([key isEqualToString:@"canonicalIdentifier"]) {
      branchUniversalObj.canonicalIdentifier = [properties valueForKey:key];
    }
    else if ([key isEqualToString:@"title"]) {
      branchUniversalObj.title = [properties valueForKey:key];
    }
    else if ([key isEqualToString:@"contentDescription"]) {
      branchUniversalObj.contentDescription = [properties valueForKey:key];
    }
    else if ([key isEqualToString:@"contentImageUrl"]){
      NSString *imageUrl = [properties valueForKey:key];
      branchUniversalObj.imageUrl = imageUrl;
    }
    else {
      [branchUniversalObj setValue:[properties objectForKey:key] forKey:key];
    }
  }

  // [self.branchUniversalObjArray addObject:branchUniversalObj];

  // Instantiate callback ids
  NSMutableDictionary *branchUniversalObjDict = [NSMutableDictionary dictionaryWithDictionary:@{
                                                                                                @"branchUniversalObj": branchUniversalObj,
                                                                                                @"onShareSheetDismissed": command.callbackId,
                                                                                                @"onShareSheetLaunched": command.callbackId,
                                                                                                @"onLinkShareResponse": command.callbackId,
                                                                                                @"onChannelSelected": command.callbackId
                                                                                                }];
  [self.branchUniversalObjArray addObject:branchUniversalObjDict];

  NSNumber *branchUniversalObjectId = [[NSNumber alloc] initWithInteger:([self.branchUniversalObjArray count] - 1)];
  NSString *message = @"createBranchUniversalObject Success";
  NSDictionary *params = [[NSDictionary alloc] initWithObjectsAndKeys:message, @"message", branchUniversalObjectId, @"branchUniversalObjectId", nil];

  CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:params];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)registerView:(CDVInvokedUrlCommand*)command
{
  int branchUniversalObjectId = [[command.arguments objectAtIndex:0] intValue];

  NSMutableDictionary *branchUniversalObjDict = [self.branchUniversalObjArray objectAtIndex:branchUniversalObjectId];
  BranchUniversalObject *branchUniversalObj = [branchUniversalObjDict objectForKey:@"branchUniversalObj"];

  [branchUniversalObj registerViewWithCallback:^(NSDictionary *params, NSError *error) {
    CDVPluginResult *pluginResult = nil;
    if (!error) {
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:params];
    } else {
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error localizedDescription]];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  }];
}

- (void)generateShortUrl:(CDVInvokedUrlCommand*)command
{

  int branchUniversalObjectId = [[command.arguments objectAtIndex:0] intValue];
  NSDictionary *arg1 = [command.arguments objectAtIndex:1];
  NSDictionary *arg2 = [command.arguments objectAtIndex:2];

  NSMutableDictionary *branchUniversalObjDict = [self.branchUniversalObjArray objectAtIndex:branchUniversalObjectId];
  BranchUniversalObject *branchUniversalObj = [branchUniversalObjDict objectForKey:@"branchUniversalObj"];

  BranchLinkProperties *props = [[BranchLinkProperties alloc] init];

  for (id key in arg1) {
    if ([key isEqualToString:@"duration"]) {
      props.matchDuration = (NSUInteger)[((NSNumber *)[arg1 objectForKey:key]) integerValue];
    }
    else if ([key isEqualToString:@"feature"]) {
      props.feature = [arg1 objectForKey:key];
    }
    else if ([key isEqualToString:@"stage"]) {
      props.stage = [arg1 objectForKey:key];
    }
    else if ([key isEqualToString:@"campaign"]) {
      props.campaign = [arg1 objectForKey:key];
    }
    else if ([key isEqualToString:@"alias"]) {
      props.alias = [arg1 objectForKey:key];
    }
    else if ([key isEqualToString:@"channel"]) {
      props.channel = [arg1 objectForKey:key];
    }
    else if ([key isEqualToString:@"tags"] && [[arg1 objectForKey:key] isKindOfClass:[NSArray class]]) {
      props.tags = [arg1 objectForKey:key];
    }
  }
  if (arg2) {
    for (id key in arg2) {
      [props addControlParam:key withValue:[arg2 objectForKey:key]];
    }
  }

  [branchUniversalObj getShortUrlWithLinkProperties:props andCallback:^(NSString *url, NSError *error) {
    CDVPluginResult* pluginResult = nil;

    if (url) {
      NSError *err;
      NSDictionary *jsonObj = [[NSDictionary alloc] initWithObjectsAndKeys:url, @"url", 0, @"options", &err, @"error", nil];

      if (!jsonObj) {
        NSLog(@"Parsing Error: %@", [err localizedDescription]);
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[err localizedDescription]];
      } else {
        NSLog(@"Success");
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:jsonObj];
      }
    }
    else {
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error localizedDescription]];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  }];
}

- (void)showShareSheet:(CDVInvokedUrlCommand*)command
{
  NSString *shareText = @"Share Link";

  if ([command.arguments count] >= 4) {
    shareText = [command.arguments objectAtIndex:3];
  }

  int branchUniversalObjectId = [[command.arguments objectAtIndex:0] intValue];
  NSDictionary *arg1 = [command.arguments objectAtIndex:1];
  NSDictionary *arg2 = [command.arguments objectAtIndex:2];

  NSMutableDictionary *branchUniversalObjDict = [self.branchUniversalObjArray objectAtIndex:branchUniversalObjectId];
  BranchUniversalObject *branchUniversalObj = [branchUniversalObjDict objectForKey:@"branchUniversalObj"];

  BranchLinkProperties *linkProperties = [[BranchLinkProperties alloc] init];

  for (id key in arg1) {
    if ([key isEqualToString:@"duration"]) {
      linkProperties.matchDuration = (NSUInteger)[((NSNumber *)[arg1 objectForKey:key]) integerValue];
    }
    else {
      [linkProperties setValue:[arg1 objectForKey:key] forKey:key];
    }
  }

  if (arg2) {
    for (id key in arg2) {
      [linkProperties addControlParam:key withValue:[arg2 objectForKey:key]];
    }
  }

  [branchUniversalObj showShareSheetWithLinkProperties:linkProperties andShareText:shareText fromViewController:self.viewController completion:^(NSString *activityType, BOOL completed) {

    int listenerCallbackId = [[command.arguments objectAtIndex:0] intValue];

    if (completed) {
      NSLog(@"Share link complete");
      [branchUniversalObj getShortUrlWithLinkProperties:linkProperties andCallback:^(NSString *url, NSError *error) {
        if (!error) {
          NSDictionary *response = [[NSDictionary alloc] initWithObjectsAndKeys:url, @"sharedLink", activityType, @"sharedChannel", nil];
          [self doShareLinkResponse:listenerCallbackId sendResponse:response];
        }
      }];
    }

    CDVPluginResult *shareDialogDismissed = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];

    NSMutableDictionary *branchUniversalObjDict = [self.branchUniversalObjArray objectAtIndex:listenerCallbackId];

    [shareDialogDismissed setKeepCallbackAsBool:TRUE];

    [self.commandDelegate sendPluginResult:shareDialogDismissed callbackId:[branchUniversalObjDict objectForKey:@"onShareSheetDismissed"]];
  }];
}

- (void)doShareLinkResponse:(int)callbackId sendResponse:(NSDictionary*)response {
  CDVPluginResult *linkShareResponse = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:response];
  NSMutableDictionary *branchUniversalObjDict = [self.branchUniversalObjArray objectAtIndex:callbackId];

  [linkShareResponse setKeepCallbackAsBool:TRUE];

  [self.commandDelegate sendPluginResult:linkShareResponse callbackId:[branchUniversalObjDict objectForKey:@"onLinkShareResponse"]];
}

- (void)onShareLinkDialogDismissed:(CDVInvokedUrlCommand*)command
{
  int listenerCallbackId = [[command.arguments objectAtIndex:0] intValue];

  NSMutableDictionary *newBranchUniversalObjDict = [self.branchUniversalObjArray objectAtIndex:listenerCallbackId];
  [newBranchUniversalObjDict setObject:command.callbackId forKey:@"onShareSheetDismissed"];

  [self.branchUniversalObjArray replaceObjectAtIndex:listenerCallbackId withObject:newBranchUniversalObjDict];
}

- (void)onLinkShareResponse:(CDVInvokedUrlCommand*)command
{
  int listenerCallbackId = [[command.arguments objectAtIndex:0] intValue];

  NSMutableDictionary *newBranchUniversalObjDict = [self.branchUniversalObjArray objectAtIndex:listenerCallbackId];
  [newBranchUniversalObjDict setObject:command.callbackId forKey:@"onLinkShareResponse"];

  [self.branchUniversalObjArray replaceObjectAtIndex:listenerCallbackId withObject:newBranchUniversalObjDict];
}

- (void)listOnSpotlight:(CDVInvokedUrlCommand*)command {
  int branchUniversalObjectId = [[command.arguments objectAtIndex:0] intValue];

  NSMutableDictionary *branchUniversalObjDict = [self.branchUniversalObjArray objectAtIndex:branchUniversalObjectId];
  BranchUniversalObject *branchUniversalObj = [branchUniversalObjDict objectForKey:@"branchUniversalObj"];

  [branchUniversalObj listOnSpotlightWithCallback:^(NSString *string, NSError *error) {
    CDVPluginResult* pluginResult = nil;
    if (!error) {
      NSError *err;
      NSData *jsonData = [NSJSONSerialization dataWithJSONObject:@{@"result":string} options:0 error:&err];
      if (!jsonData) {
        NSLog(@"Parsing Error: %@", [err localizedDescription]);
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[err localizedDescription]];
      } else {
        NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:jsonString];
      }
    }
    else {
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error localizedDescription]];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  }];
}

#pragma mark Branch Query Methods

- (void)crossPlatformIds:(CDVInvokedUrlCommand *)command {
  NSMutableDictionary *json = [NSMutableDictionary new];

  Branch *branch = [self getInstance];
  [branch crossPlatformIdDataWithCompletion:^(BranchCrossPlatformID *cpid) {
    CDVPluginResult* pluginResult = nil;
    if (cpid) {
      // Convert the ObjC object back into JSON.  Should have kept the raw JSON response.
      [json setObject:cpid.developerID forKey:@"developer_identity"];
      [json setObject:cpid.crossPlatformID forKey:@"cross_platform_id"];
      [json setObject:cpid.pastCrossPlatformIDs forKey:@"past_cross_platform_ids"];

      NSMutableArray *probCPIDs = [NSMutableArray new];
      for (BranchProbabilisticCrossPlatformID *tmp in cpid.probabiliticCrossPlatformIDs) {
        if (tmp.crossPlatformID && tmp.score) {
          NSMutableDictionary *pair = [NSMutableDictionary new];
          [pair setObject:tmp.crossPlatformID forKey:@"id"];
          [pair setObject:tmp.score forKey:@"probability"];
          [probCPIDs addObject:pair];
        }
      }
      [json setObject:probCPIDs forKey:@"prob_cross_platform_ids"];

      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:json];
    } else {
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"No CPIDs available"];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  }];
}

- (void)lastAttributedTouchData:(CDVInvokedUrlCommand *)command {
  NSMutableDictionary *json = [NSMutableDictionary new];

  Branch *branch = [self getInstance];
  [branch lastAttributedTouchDataWithAttributionWindow:30 completion:^(BranchLastAttributedTouchData * _Nullable latd, NSError * _Nullable error) {
    CDVPluginResult* pluginResult = nil;
    if (latd) {
      [json setObject:latd.attributionWindow forKey:@"attribution_window"];
      [json setObject:latd.lastAttributedTouchJSON forKey:@"last_attributed_touch_data"];

      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:json];
    } else {
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"No LATD available"];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  }];
}


#pragma mark - URL Methods (not fully implemented YET!)

- (NSString *)getShortURL:(CDVInvokedUrlCommand*)command
{
  Branch *branch = [self getInstance];
  return [branch getShortURL];
}

- (id)getShortURLWithParams:(CDVInvokedUrlCommand*)command
{
  Branch *branch = [self getInstance];
  NSDictionary *params = [command.arguments objectAtIndex:0];

  return [branch getShortURLWithParams:params];
}

- (NSString *)getLongURLWithParams:(CDVInvokedUrlCommand*)command
{
  id params = [command.arguments objectAtIndex:0];
  return [[self getInstance] getLongURLWithParams:params];
}

- (void)getBranchActivityItemWithParams:(CDVInvokedUrlCommand*)command
{
  UIActivityItemProvider *provider = [Branch getBranchActivityItemWithParams:[command.arguments objectAtIndex:0]];

  UIActivityViewController *shareViewController = [[UIActivityViewController alloc] initWithActivityItems:@[ provider ] applicationActivities:nil];

  [self.viewController presentViewController:shareViewController animated:YES completion:nil];
}

@end
