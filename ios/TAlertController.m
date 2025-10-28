/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

#import <React/RCTUtils.h>

#import "TAlertController.h"

@interface TAlertController ()

@property (nonatomic, strong) UIWindow *alertWindow;

@end

@implementation TAlertController

- (UIWindow *)alertWindow
{
  if (_alertWindow == nil) {
    _alertWindow = [[UIWindow alloc] initWithWindowScene:RCTKeyWindow().windowScene];
    _alertWindow.rootViewController = [UIViewController new];
    _alertWindow.windowLevel = UIWindowLevelAlert + 1;
  }
  return _alertWindow;
}

- (void)show:(BOOL)animated completion:(void (^)(void))completion
{
  [self.alertWindow makeKeyAndVisible];
  [self.alertWindow.rootViewController presentViewController:self animated:animated completion:completion];
}

- (void)hide
{
  __weak typeof(self) weakSelf = self;
  [self.presentingViewController dismissViewControllerAnimated:YES completion:^{
    if (weakSelf == nil) return;
    [weakSelf.alertWindow setHidden:YES];

    weakSelf.alertWindow.windowScene = nil;

    weakSelf.alertWindow = nil;
  }];
}

@end
