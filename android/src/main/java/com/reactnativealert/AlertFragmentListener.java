package com.reactnativealert;

import static com.reactnativealert.AlertViewManager.ACTION_DISMISSED;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;

class AlertFragmentListener implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener {
  private final Callback mCallback;
  private boolean mCallbackConsumed = false;
  @Nullable
  private final ReactApplicationContext reactContext;

  public AlertFragmentListener(@Nullable ReactApplicationContext reactContext, Callback callback) {
    this.reactContext = reactContext;
    this.mCallback = callback;
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    if (!mCallbackConsumed) {
      if (reactContext != null && reactContext.hasActiveCatalystInstance()) {
        mCallback.invoke(which);
        mCallbackConsumed = true;
      }
    }
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    if (!mCallbackConsumed) {
      if (reactContext != null && reactContext.hasActiveCatalystInstance()) {
        mCallback.invoke(ACTION_DISMISSED);
        mCallbackConsumed = true;
      }
    }
  }
}
