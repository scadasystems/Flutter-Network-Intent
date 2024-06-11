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
  final _networkIntentPlugin = NetworkIntent();

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
    final isInit = await _networkIntentPlugin.initDiscovery();

    _setMsg('isInit: $isInit');

    onListener ??= _networkIntentPlugin.onListener(
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
      await _networkIntentPlugin.enableDiscovery();
    } catch (e) {
      _setMsg('enableDiscovery error: $e');
    }

    _networkIntentPlugin.sendMessage('테스트');
  }

  void _stop() async {
    await onListener?.cancel();
    onListener = null;
    await _networkIntentPlugin.dispose();
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
