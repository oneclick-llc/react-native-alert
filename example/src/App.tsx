import * as React from 'react';

import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { Alert } from 'react-native-alert';

export default function App() {
  return (
    <View style={styles.container}>
      <TouchableOpacity
        style={{ marginTop: 100 }}
        onPress={() => {
          Alert.alert({
            defaultValue: 'some',
            type: 'secure-text',
            title: 'lorem ipsum dollar',
            theme: 'light',
            keyboardType: 'decimal-pad',
            buttons: [
              {
                style: 'destructive',
                text: 'destructive',
                onPress: () => {
                  console.log('click destructive');
                },
              },
              {
                style: 'cancel',
                text: 'cancel',
                onPress: () => {
                  console.log('click cancel');
                },
              },
              {
                style: 'default',
                text: 'positive',
                onPress: (va) => {
                  console.log('click default', va);
                },
              },
            ],
          });
        }}
      >
        <Text>Press</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
