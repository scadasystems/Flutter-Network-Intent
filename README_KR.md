
# NetworkIntent

이 라이브러리를 사용하여 네트워크 기반의 디바이스 발견 및 통신을 관리할 수 있습니다.

## 지원 가능
- [x] Android
- [ ] iOS
- [ ] Web
- [ ] Windows
- [ ] Linux
- [ ] MacOS

## 기능

1. **디스커버리 초기화**
   - `initDiscovery()`: 디스커버리를 초기화합니다. 성공적으로 초기화되면 `true`를 반환합니다.

2.  **이벤트 리스너**
   - `onListener()`: 다양한 디바이스 발견 이벤트에 대한 리스너를 설정합니다.
     - `onDiscoveryStarted`: 발견이 시작될 때 호출됩니다.
     - `onDiscoveryStopped`: 발견이 중지될 때 호출됩니다.
     - `onDiscoveryError`: 오류가 발생했을 때 호출됩니다.
     - `onDiscoveryResult`: 디바이스 발견 결과가 있을 때 호출됩니다.

3. **디스커버리 활성화**
   - `enableDiscovery()`: 디스커버리 시작합니다. 성공적으로 시작되면 `true`를 반환합니다.

4. **디스커버리 비활성화**
   - `disableDiscovery()`: 디바이스 발견을 중지합니다.

5. **메시지 전송**
   - `sendMessage(String message)`: 문자열 메시지를 전송합니다.

6. **DISPOSE**
   - `dispose()`: 사용한 자원을 해제합니다.

## 예제 코드
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