package com.reactnativealert

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.input.input
import com.facebook.react.bridge.*
import java.lang.ref.WeakReference
import java.util.*

class AlertViewManager(reactContext: ReactApplicationContext?) : ReactContextBaseJavaModule(reactContext) {
  override fun getName(): String {
    return REACT_CLASS
  }

  private fun onClick(which: Int, arguments: Bundle?, callback: Callback, inputText: CharSequence? = null) {
    if (arguments == null) return
    var key = -1
    val buttonIndices = arguments.getIntArray("buttonIndices")
    for (i in buttonIndices!!.indices) {
      if (buttonIndices[i] == which) {
        key = i
        break
      }
    }
    if (which == Dialog.BUTTON_POSITIVE && inputText != null) callback.invoke(key, inputText.toString()) else callback.invoke(key)

    presentedDialog?.clear()
    presentedDialog = null
  }



  var presentedDialog: WeakReference<MaterialDialog>? = null

  @ReactMethod
  fun dismissTopPresented() {
    presentedDialog?.get()?.dismiss()
    presentedDialog?.clear()
    presentedDialog = null
  }

  @SuppressLint("CheckResult")
  fun showNewAlert(arguments: Bundle, actionCallback: Callback) {
    UiThreadUtil.assertOnUiThread()
    dismissTopPresented()
    val type = arguments.getString("type")
    val positive = arguments.getString(AlertFragment.ARG_BUTTON_POSITIVE)
    val neutral = arguments.getString(AlertFragment.ARG_BUTTON_NEUTRAL)
    val negative = arguments.getString(AlertFragment.ARG_BUTTON_NEGATIVE)
    val title = arguments.getString(AlertFragment.ARG_TITLE)
    val message = arguments.getString(AlertFragment.ARG_MESSAGE)
    val keyBoardType = arguments.getString(AlertFragment.ARG_KEYBOARD_TYPE)
    val theme = arguments.getString(AlertFragment.ARG_THEME)
    val contextThemeWrapper = ContextThemeWrapper(
      currentActivity,
      if (theme == "light") R.style.MyDialogStyleLight else R.style.MyDialogStyleDark
    )
    val materialDialog = MaterialDialog(contextThemeWrapper)
    materialDialog.cancelable(false)
    if (title != null && title.isNotEmpty()) {
      materialDialog.title(null, title)
    }
    if (message != null && message.isNotEmpty()) {
      materialDialog.message(null, message, null)
    } else {
      materialDialog.view.contentLayout.visibility = View.GONE
    }
    if (neutral != null) {
      materialDialog.neutralButton(text = neutral) { onClick(Dialog.BUTTON_NEUTRAL, arguments, actionCallback) }
      materialDialog.view.buttonsLayout!!.actionButtons[WhichButton.NEUTRAL.index].typeface = Typeface.DEFAULT_BOLD
    }
    if (positive != null) {
      materialDialog.positiveButton(text = positive) {
        if (type != "default") return@positiveButton
        onClick(Dialog.BUTTON_POSITIVE, arguments, actionCallback)
      }
    }
    if (negative != null) {
      materialDialog.negativeButton(null, negative) { onClick(Dialog.BUTTON_NEGATIVE, arguments, actionCallback) }
      materialDialog.view.buttonsLayout!!.actionButtons[WhichButton.NEGATIVE.index].updateTextColor(Color.RED)
    }
    var inputType = InputType.TYPE_CLASS_TEXT
    if (keyBoardType != null && (keyBoardType == "number-pad" || keyBoardType == "decimal-pad")) {
      inputType = InputType.TYPE_CLASS_NUMBER
    }
    if (type == "secure-text") inputType = inputType or InputType.TYPE_TEXT_VARIATION_PASSWORD
    if (type != "default") {
      materialDialog.input(
        prefill = arguments.getString("defaultValue"),
        inputType = inputType,
        waitForPositiveButton = true,
        allowEmpty = true
      ) { _, charSequence -> onClick(Dialog.BUTTON_POSITIVE, arguments, actionCallback, charSequence) }
    }
    materialDialog.show()
    presentedDialog = WeakReference(materialDialog)
  }


  @ReactMethod
  fun alertWithArgs(options: ReadableMap, actionCallback: Callback) {
    val args = Bundle()
    if (options.hasKey("theme")) {
      args.putString(AlertFragment.ARG_THEME, options.getString("theme"))
    }
    if (options.hasKey(KEY_TITLE)) {
      args.putString(AlertFragment.ARG_TITLE, options.getString(KEY_TITLE))
    }
    if (options.hasKey("type")) {
      args.putString(AlertFragment.ARG_TYPE, options.getString("type"))
    }
    if (options.hasKey(KEY_MESSAGE)) {
      args.putString(AlertFragment.ARG_MESSAGE, options.getString(KEY_MESSAGE))
    }
    if (options.hasKey("defaultValue")) {
      args.putString(AlertFragment.ARG_DEFAULT_VALUE, options.getString("defaultValue"))
    }
    if (options.hasKey("keyboardType")) {
      args.putString(AlertFragment.ARG_KEYBOARD_TYPE, options.getString("keyboardType"))
    }
    val cancelButtonKey = options.getString("cancelButtonKey")
    val destructiveButtonKey = options.getString("destructiveButtonKey")
    if (options.hasKey(KEY_ITEMS)) {
      val items = Objects.requireNonNull(options.getArray(KEY_ITEMS))
      val ints = IntArray(items!!.size())
      Arrays.fill(ints, Int.MIN_VALUE)
      if (cancelButtonKey != null) {
        val value = cancelButtonKey.toInt()
        ints[value] = Dialog.BUTTON_NEUTRAL
      }
      if (destructiveButtonKey != null) {
        val value = destructiveButtonKey.toInt()
        ints[value] = Dialog.BUTTON_NEGATIVE
      }
      for (i in ints.indices) {
        if (ints[i] == Int.MIN_VALUE) ints[i] = Dialog.BUTTON_POSITIVE
      }
      args.putIntArray("buttonIndices", ints)
      for (i in 0 until items.size()) {
        val button = items.getMap(i)
        val hashMap = button!!.toHashMap()
        val values: Collection<Any> = hashMap.values
        val keys: Set<String> = hashMap.keys
        for (key in keys) {
          var title = ""
          for (buttonTitle in values) title = buttonTitle as String
          when (key) {
            cancelButtonKey -> {
              args.putString(AlertFragment.ARG_BUTTON_NEUTRAL, title)
            }
            destructiveButtonKey -> {
              args.putString(AlertFragment.ARG_BUTTON_NEGATIVE, title)
            }
            else -> {
              args.putString(AlertFragment.ARG_BUTTON_POSITIVE, title)
            }
          }
        }
      }
    }
    UiThreadUtil.runOnUiThread { showNewAlert(args, actionCallback) }
  }

  companion object {
    const val REACT_CLASS = "RNAlert"

    /* package */
    const val ACTION_DISMISSED = "dismissed"

    /* package */
    const val KEY_TITLE = "title"

    /* package */
    const val KEY_MESSAGE = "message"


    /* package */
    const val KEY_ITEMS = "buttons"

    /* package */
    const val FRAGMENT_TAG = "com.reactnativealert.AlertViewManager"
  }
}
