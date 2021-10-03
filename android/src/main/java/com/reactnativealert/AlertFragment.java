/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.reactnativealert;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/** A fragment used to display the dialog. */
public class AlertFragment extends DialogFragment implements DialogInterface.OnClickListener {

  /* package */ static final String ARG_TITLE = "title";
  /* package */ static final String ARG_THEME = "theme";
  /* package */ static final String ARG_TYPE = "type";
  /* package */ static final String ARG_MESSAGE = "message";
  /* package */ static final String ARG_DEFAULT_VALUE = "defaultValue";
  /* package */ static final String ARG_KEYBOARD_TYPE = "keyboardType";
  /* package */ static final String ARG_BUTTON_POSITIVE = "button_positive";
  /* package */ static final String ARG_BUTTON_NEGATIVE = "button_negative";
  /* package */ static final String ARG_BUTTON_NEUTRAL = "button_neutral";
  /* package */ static final String ARG_ITEMS = "items";

  private final @Nullable AlertFragmentListener mListener;

  public AlertFragment() {
    mListener = null;
  }

  @SuppressLint("ValidFragment")
  public AlertFragment(@Nullable AlertFragmentListener listener, Bundle arguments) {
    mListener = listener;
    setArguments(arguments);
  }




  @Override
  public void onClick(DialogInterface dialog, int which) {
    if (mListener != null) {
      Bundle arguments = getArguments();
      if (arguments == null) return;
      int key = -1;
      int[] buttonIndices = arguments.getIntArray("buttonIndices");
      for (int i = 0; i < buttonIndices.length; i++) {
        if (buttonIndices[i] == which) {
          key = i;
          break;
        }
      }
      mListener.onClick(dialog, key);
    }
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    super.onDismiss(dialog);
    if (mListener != null) {
      mListener.onDismiss(dialog);
    }
  }
}
