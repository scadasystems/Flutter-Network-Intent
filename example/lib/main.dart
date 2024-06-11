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

  final _textController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _start();
  }

  @override
  void dispose() {
    _textController.dispose();
    _stop();
    super.dispose();
  }

  void _start() async {
    final isInit = await _networkIntentPlugin.initDiscovery(
      port: 5776,
    );

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
      final enable = await _networkIntentPlugin.enableDiscovery();
      _setMsg('enableDiscovery: $enable');
    } catch (e) {
      _setMsg('enableDiscovery error: $e');
    }
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
        body: Padding(
          padding: const EdgeInsets.all(12),
          child: Column(
            children: [
              TextField(
                controller: _textController,
                decoration: InputDecoration(
                  border: const OutlineInputBorder(),
                  labelText: '메시지 보내기',
                  suffix: IconButton(
                    onPressed: () {
                      final text = _textController.text;

                      _networkIntentPlugin.sendMessage(text);
                    },
                    icon: const Icon(Icons.send),
                  ),
                ),
              ),
              const SizedBox(height: 20),
              Expanded(
                child: SingleChildScrollView(
                  child: Text(
                    _stringBuffer.toString(),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
