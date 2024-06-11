
# NetworkIntent

This library allows you to manage network-based device discovery and communication.

## Support
- [x] Android
- [ ] iOS
- [ ] Web
- [ ] Windows
- [ ] Linux
- [ ] MacOS

## Features

1. **Initialize Discovery**
   - `initDiscovery()`: Initializes discovery. Returns `true` if successfully initialized.

2. **Event Listener**
   - `onListener()`: Sets up listeners for various device discovery events.
     - `onDiscoveryStarted`: Called when discovery starts.
     - `onDiscoveryStopped`: Called when discovery stops.
     - `onDiscoveryError`: Called when an error occurs.
     - `onDiscoveryResult`: Called when a device discovery result is available.

3. **Enable Discovery**
   - `enableDiscovery()`: Starts discovery. Returns `true` if successfully started.

4. **Disable Discovery**
   - `disableDiscovery()`: Stops device discovery.

5. **Send Message**
   - `sendMessage(String message)`: Sends a string message.

6. **Dispose**
   - `dispose()`: Releases used resources.


## Example
<details>
<summary>See code</summary>
<div markdown="1">

```dart
import 'dart:async';

import 'package:flutter/material.dart';
import 'package:network_intent/network_intent.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _networkIntent = NetworkIntent();

  StreamSubscription<dynamic>? onListener;

  final StringBuffer _stringBuffer = StringBuffer();

  @override
  void initState() {
    super.initState();
    _start();
  }

  @override
  void dispose() {
    _stop();
    super.dispose();
  }

  void _start() async {
    final isInit = await _networkIntent.initDiscovery();

    _setMsg('isInit: $isInit');

    onListener ??= _networkIntent.onListener(
      onDiscoveryStarted: (result) {
        _setMsg('onDiscoveryStarted: $result');
      },
      onDiscoveryStopped: (result) {
        _setMsg('onDiscoveryStopped: $result');
      },
      onDiscoveryError: (error) {
        _setMsg('onDiscoveryError: $error');
      },
      onDiscoveryResult: (address, result) {
        _setMsg('onDiscoveryResult: $address, $result');
      },
    );

    try {
      await _networkIntent.enableDiscovery();
    } catch (e) {
      _setMsg('enableDiscovery error: $e');
    }

    _networkIntent.sendMessage('테스트');
  }

  void _stop() async {
    await _networkIntent.dispose();
  }

  void _setMsg(String text) {
    setState(() {
      _stringBuffer.writeln(text);
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('NetworkIntent example app'),
        ),
        body: Center(
          child: Text(
            _stringBuffer.toString(),
          ),
        ),
      ),
    );
  }
}
```

</div>
</details>
