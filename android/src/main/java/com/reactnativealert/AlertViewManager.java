package com.reactnativealert;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextThemeWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.WhichButton;
import com.afollestad.materialdialogs.input.DialogInputExtKt;
import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.SoftAssertions;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.common.MapBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import kotlin.Unit;

public class AlertViewManager extends ReactContextBaseJavaModule implements LifecycleEventListener {
  public static final String REACT_CLASS = "AlertManager";
  /* package */ static final String ACTION_BUTTON_CLICKED = "buttonClicked";
  /* package */ static final String ACTION_DISMISSED = "dismissed";
  /* package */ static final String KEY_TITLE = "title";
  /* package */ static final String KEY_MESSAGE = "message";
  /* package */ static final String KEY_BUTTON_POSITIVE = "buttonPositive";
  /* package */ static final String KEY_BUTTON_NEGATIVE = "buttonNegative";
  /* package */ static final String KEY_BUTTON_NEUTRAL = "buttonNeutral";
  /* package */ static final String KEY_ITEMS = "buttons";

  /* package */ static final String FRAGMENT_TAG =
    "com.reactnativealert.AlertViewManager";


  /* package */ static final Map<String, Object> CONSTANTS =
    MapBuilder.of(
      ACTION_BUTTON_CLICKED, ACTION_BUTTON_CLICKED,
      ACTION_DISMISSED, ACTION_DISMISSED,
      KEY_BUTTON_POSITIVE, DialogInterface.BUTTON_POSITIVE,
      KEY_BUTTON_NEGATIVE, DialogInterface.BUTTON_NEGATIVE,
      KEY_BUTTON_NEUTRAL, DialogInterface.BUTTON_NEUTRAL);

  private boolean isInForeground;

  public AlertViewManager(@Nullable ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  @NonNull
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  public void initialize() {
    getReactApplicationContext().addLifecycleEventListener(this);
  }


  private class FragmentManagerHelper {
    private final @NonNull
    FragmentManager mFragmentManager;

    private @Nullable
    Object mFragmentToShow;

    public FragmentManagerHelper(@NonNull FragmentManager fragmentManager) {
      mFragmentManager = fragmentManager;
    }

    public void showPendingAlert() {
      UiThreadUtil.assertOnUiThread();
      SoftAssertions.assertCondition(isInForeground, "showPendingAlert() called in background");
      if (mFragmentToShow == null) {
        return;
      }

      dismissExisting();
      ((AlertFragment) mFragmentToShow).show(mFragmentManager, FRAGMENT_TAG);
      mFragmentToShow = null;
    }

    private void dismissExisting() {
      if (!isInForeground) return;
      AlertFragment oldFragment = (AlertFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TAG);
      if (oldFragment != null && oldFragment.isResumed()) {
        oldFragment.dismiss();
      }
    }

    private Unit onClick(int which, Bundle arguments, Callback callback, @Nullable CharSequence inputText) {
      if (arguments == null) return Unit.INSTANCE;
      int key = -1;
      int[] buttonIndices = arguments.getIntArray("buttonIndices");
      for (int i = 0; i < buttonIndices.length; i++) {
        if (buttonIndices[i] == which) {
          key = i;
          break;
        }
      }
      if (which == Dialog.BUTTON_POSITIVE && inputText != null) callback.invoke(key, inputText.toString());
      else callback.invoke(key);
      return Unit.INSTANCE;
    }

    @SuppressLint("CheckResult")
    public void showNewAlert(Bundle arguments, Callback actionCallback) {
      UiThreadUtil.assertOnUiThread();
      dismissExisting();

      String type = arguments.getString("type");
      String positive = arguments.getString(AlertFragment.ARG_BUTTON_POSITIVE);
      String neutral = arguments.getString(AlertFragment.ARG_BUTTON_NEUTRAL);
      String negative = arguments.getString(AlertFragment.ARG_BUTTON_NEGATIVE);
      String title = arguments.getString(AlertFragment.ARG_TITLE);
      String message = arguments.getString(AlertFragment.ARG_MESSAGE);
      String keyBoardType = arguments.getString(AlertFragment.ARG_KEYBOARD_TYPE);

      ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getCurrentActivity(), R.style.MyDialogStyleLight);
      MaterialDialog materialDialog = new MaterialDialog(contextThemeWrapper, MaterialDialog.getDEFAULT_BEHAVIOR());
      materialDialog.cancelable(false);

      if (title != null) materialDialog.title(null, title);
      if (message != null) materialDialog.message(null, message, null);



      if (neutral != null) {
        materialDialog.neutralButton(null, neutral, m ->
          onClick(Dialog.BUTTON_NEUTRAL, arguments, actionCallback, null));
        materialDialog.getView().getButtonsLayout().actionButtons[WhichButton.NEUTRAL.getIndex()].setTypeface(Typeface.DEFAULT_BOLD);
      }

      if (positive != null) {
        materialDialog.positiveButton(null, positive, m -> {
          if (!type.equals("default")) return Unit.INSTANCE;
          return onClick(Dialog.BUTTON_POSITIVE, arguments, actionCallback, null);
        });
      }

      if (negative != null) {
        materialDialog.negativeButton(null, negative, m ->
          onClick(Dialog.BUTTON_NEGATIVE, arguments, actionCallback, null));
        materialDialog.getView().getButtonsLayout().actionButtons[WhichButton.NEGATIVE.getIndex()].updateTextColor(Color.RED);
      }

      int inputType = InputType.TYPE_CLASS_TEXT;
      if (keyBoardType != null && (keyBoardType.equals("number-pad") || keyBoardType.equals("decimal-pad"))) {
        inputType = InputType.TYPE_CLASS_NUMBER;
      }
      if (type.equals("secure-text")) inputType |= InputType.TYPE_TEXT_VARIATION_PASSWORD;

      if (!type.equals("default")) {
        DialogInputExtKt.input(
          materialDialog,
          null,
          null,
          arguments.getString("defaultValue"),
          null,
          inputType,
          null,
          true,
          true,
          (materialDialog1, charSequence) -> {
            onClick(Dialog.BUTTON_POSITIVE, arguments, actionCallback, charSequence);
            return Unit.INSTANCE;
          });
      }

      materialDialog.show();
    }
  }

  @ReactMethod
  public void alertWithArgs(ReadableMap options, Callback actionCallback) {
    final FragmentManagerHelper fragmentManagerHelper = getFragmentManagerHelper();
    if (fragmentManagerHelper == null) {
      throw new RuntimeException("Tried to show an alert while not attached to an Activity");
    }

    final Bundle args = new Bundle();
    if (options.hasKey("theme")) {
      args.putString(AlertFragment.ARG_THEME, options.getString("theme"));
    }
    if (options.hasKey(KEY_TITLE)) {
      args.putString(AlertFragment.ARG_TITLE, options.getString(KEY_TITLE));
    }

    if (options.hasKey("type")) {
      args.putString(AlertFragment.ARG_TYPE, options.getString("type"));
    }
    if (options.hasKey(KEY_MESSAGE)) {
      args.putString(AlertFragment.ARG_MESSAGE, options.getString(KEY_MESSAGE));
    }

    if (options.hasKey("defaultValue")) {
      args.putString(AlertFragment.ARG_DEFAULT_VALUE, options.getString("defaultValue"));
    }

    if (options.hasKey("keyboardType")) {
      args.putString(AlertFragment.ARG_KEYBOARD_TYPE, options.getString("keyboardType"));
    }

    String cancelButtonKey = options.getString("cancelButtonKey");
    String destructiveButtonKey = options.getString("destructiveButtonKey");

    if (options.hasKey(KEY_ITEMS)) {
      ReadableArray items = Objects.requireNonNull(options.getArray(KEY_ITEMS));

      int[] ints = new int[items.size()];
      Arrays.fill(ints, Integer.MIN_VALUE);
      if (cancelButtonKey != null) {
        int value = Integer.parseInt(cancelButtonKey);
        ints[value] = Dialog.BUTTON_NEUTRAL;
      }
      if (destructiveButtonKey != null) {
        int value = Integer.parseInt(destructiveButtonKey);
        ints[value] = Dialog.BUTTON_NEGATIVE;
      }
      for (int i = 0; i < ints.length; i++) {
        if (ints[i] == Integer.MIN_VALUE) ints[i] = Dialog.BUTTON_POSITIVE;
      }
      args.putIntArray("buttonIndices", ints);

      for (int i = 0; i < items.size(); i++) {
        ReadableMap button = items.getMap(i);
        HashMap<String, Object> hashMap = button.toHashMap();
        Collection<Object> values = hashMap.values();
        Set<String> keys = hashMap.keySet();
        for (String key : keys) {
          String title = "";
          for (Object buttonTitle : values) title = (String) buttonTitle;
          if (key.equals(cancelButtonKey)) {
            args.putString(AlertFragment.ARG_BUTTON_NEUTRAL, title);
          } else if (key.equals(destructiveButtonKey)) {
            args.putString(AlertFragment.ARG_BUTTON_NEGATIVE, title);
          } else {
            args.putString(AlertFragment.ARG_BUTTON_POSITIVE, title);
          }
        }
      }
    }

    UiThreadUtil.runOnUiThread(
      () -> fragmentManagerHelper.showNewAlert(args, actionCallback));
  }

  /**
   * Creates a new helper to work with FragmentManager. Returns null if we're not attached to an
   * Activity.
   *
   * <p>DO NOT HOLD LONG-LIVED REFERENCES TO THE OBJECT RETURNED BY THIS METHOD, AS THIS WILL CAUSE
   * MEMORY LEAKS.
   */
  private @Nullable
  FragmentManagerHelper getFragmentManagerHelper() {
    Activity activity = getCurrentActivity();
    if (!(activity instanceof FragmentActivity)) {
      return null;
    }
    return new FragmentManagerHelper(((FragmentActivity) activity).getSupportFragmentManager());
  }

  @Override
  public void invalidate() {
    super.invalidate();

    ReactApplicationContext applicationContext = getReactApplicationContextIfActiveOrWarn();
    if (applicationContext != null) {
      applicationContext.removeLifecycleEventListener(this);
    }
  }

  @Override
  public void onHostPause() {
    // Don't show the dialog if the host is paused.
    isInForeground = false;
  }

  @Override
  public void onHostDestroy() {
  }

  @Override
  public void onHostResume() {
    isInForeground = true;
    // Check if a dialog has been created while the host was paused, so that we can show it now.
    FragmentManagerHelper fragmentManagerHelper = getFragmentManagerHelper();
    if (fragmentManagerHelper != null) {
      fragmentManagerHelper.showPendingAlert();
    } else {
      FLog.w(AlertViewManager.class, "onHostResume called but no FragmentManager found");
    }
  }
}
