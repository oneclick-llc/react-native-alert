/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

#import <UIKit/UIKit.h>

#import <React/RCTBridgeModule.h>
#import <React/RCTInvalidating.h>
#import "TAlertController.h"

@interface TAlertManager : NSObject <RCTBridgeModule, RCTInvalidating>
@property (weak, nonatomic) TAlertController *presentedAlert;
@end
