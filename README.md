# react-native-alert

## Getting started
Main goal of this library is that support prompt for android and looks more beautiful on android.


###### package.json
`"react-native-alert": "sergeymild/react-native-alert"`

`$ yarn`

## Usage
```typescript
import {Alert} from 'react-native-alert';

export type AlertType = 'default' | 'plain-text' | 'secure-text';

type DefaultButton = { text: string; onPress: () => void; style: 'default' };
type InputButton = {
  text: string;
  onPress: (value: string) => void;
  style: 'default';
};
type CancelButton = { text: string; onPress: () => void; style: 'cancel' };
type DestructiveButton = {
  text: string;
  onPress: () => void;
  style: 'destructive';
};

type AlertParams = {
  title: string;
  message?: string;
  buttons?: Array<CancelButton | DefaultButton | DestructiveButton>;
  theme?: 'dark' | 'light' | 'system';
};

type PromptParams = {
  title: string;
  message?: string;
  buttons?: Array<InputButton | CancelButton>;
  type?: AlertType;
  theme?: 'dark' | 'light' | 'system';
  defaultValue?: string;
  keyboardType?: KeyboardType;
};

Alert.alert(AlertParams);
Alert.prompt(PromptParams);
```
