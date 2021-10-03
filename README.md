# react-native-alert

## Getting started
Main goal of this library is that support prompt for android and looks more beautiful on android.


###### package.json
`"react-native-alert": "sergeymild/react-native-alert"`

`$ yarn`

## Usage
```typescript
import {Alert} from 'react-native-alert';

type AlertType = 'default' | 'plain-text' | 'secure-text';
type AlertButtonStyle = 'default' | 'cancel' | 'destructive';
type Buttons = Array<{
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

Alert.alert(AlertParams);
Alert.prompt(PromptParams);
```
