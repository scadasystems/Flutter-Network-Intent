import 'dart:async';

import 'package:flutter/services.dart';
import 'package:network_intent/src/network_intent_type.dart';

import 'network_intent_platform_interface.dart';

/// An implementation of [NetworkIntentPlatform] that uses method channels.
class MethodChannelNetworkIntent extends NetworkIntentPlatform {
  /// The method channel used to interact with the native platform.
  final methodChannel = const MethodChannel('network_intent');

  final eventChannel = const EventChannel('network_intent_events');

  DiscoveryStarted? _onDiscoveryStarted;
  DiscoveryStopped? _onDiscoveryStopped;
  DiscoveryError? _onDiscoveryError;
  DiscoveryResult? _onDiscoveryResult;

  StreamSubscription<dynamic>? _onListener;

  @override
  Future<bool> initDiscovery() async {
    return await methodChannel.invokeMethod<bool>('initDiscovery') ?? false;
  }

  @override
  Future<bool> enableDiscovery() async {
    return await methodChannel.invokeMethod<bool>('enableDiscovery') ?? false;
  }

  @override
  Future<void> disableDiscovery() async {
    return methodChannel.invokeMethod('disableDiscovery');
  }

  @override
  StreamSubscription<dynamic> onListener({
    DiscoveryStarted? onDiscoveryStarted,
    DiscoveryStopped? onDiscoveryStopped,
    DiscoveryError? onDiscoveryError,
    DiscoveryResult? onDiscoveryResult,
  }) {
    _onDiscoveryStarted = onDiscoveryStarted;
    _onDiscoveryStopped = onDiscoveryStopped;
    _onDiscoveryError = onDiscoveryError;
    _onDiscoveryResult = onDiscoveryResult;

    _onListener = eventChannel.receiveBroadcastStream().listen((event) {
      final type = event['type'];

      switch (type) {
        case 'DISCOVERY_STARTED':
          _onDiscoveryStarted?.call('DISCOVERY_STARTED');
          break;
        case 'DISCOVERY_STOPPED':
          _onDiscoveryStopped?.call('DISCOVERY_STOPPED');
          break;
        case 'DISCOVERY_ERROR':
          final errorMsg = event['error'];
          _onDiscoveryError?.call(errorMsg);
          break;
        case 'DISCOVERY_RESULT':
          final ipAddress = event['address'];
          final result = event['result'];

          _onDiscoveryResult?.call(ipAddress, result);
          break;
        default:
          break;
      }
    });

    return _onListener!;
  }

  @override
  void sendMessage(String message) {
    methodChannel.invokeMethod('sendMessage', {'message': message});
  }

  @override
  Future<void> dispose() async {
    await _onListener?.cancel();
    await disableDiscovery();
    _onListener = null;

    return;
  }
}
