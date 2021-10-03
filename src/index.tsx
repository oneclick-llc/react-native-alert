import { KeyboardType, NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-a' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const AlertManager = NativeModules.AlertManager
  ? NativeModules.AlertManager
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export type AlertType = 'default' | 'plain-text' | 'secure-text';

export type AlertButtonStyle = 'default' | 'cancel' | 'destructive';
export type Buttons = Array<{
  text?: string;
  onPress?: () => void;
  style?: AlertButtonStyle;
}>;

type AlertParams = {
  title: string;
  message?: string;
  buttons?: Buttons;
  theme?: 'dark' | 'light' | 'system';
};

type PromptParams = {
  title: string;
  message?: string;
  buttons?: Buttons;
  type?: AlertType;
  theme?: 'dark' | 'light' | 'system';
  defaultValue?: string;
  keyboardType?: KeyboardType;
};

export class Alert {
  static alert(params: AlertParams) {
    Alert.prompt({
      ...params,
      type: 'default',
    });
  }

  static prompt(params: PromptParams): void {
    let callbacks: Record<number, (text: string) => void> = [];
    const buttons: Array<Record<number, string>> = [];
    let cancelButtonKey;
    let destructiveButtonKey;
    if (params.buttons) {
      const paramsButtons = params.buttons;
      paramsButtons.forEach((btn, index) => {
        if (btn.onPress) callbacks[index] = btn.onPress;
        if (btn.style === 'cancel') {
          cancelButtonKey = String(index);
        } else if (btn.style === 'destructive') {
          destructiveButtonKey = String(index);
        }
        if (btn.text || index < paramsButtons.length - 1) {
          const btnDef: Record<number, string> = {};
          btnDef[index] = btn.text || '';
          buttons.push(btnDef);
        }
      });
    }

    AlertManager.alertWithArgs(
      {
        title: params.title || '',
        message: params.message || undefined,
        buttons,
        type: params.type || undefined,
        defaultValue: params.defaultValue,
        cancelButtonKey,
        destructiveButtonKey,
        keyboardType: params.keyboardType,
        theme: params.theme,
      },
      (id: number, value: string) => {
        console.log('--', id, callbacks);
        const cb = callbacks[id];
        cb?.(value);
      }
    );
  }
}
