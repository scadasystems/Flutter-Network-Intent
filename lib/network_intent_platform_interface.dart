import 'dart:async';

import 'package:network_intent/src/network_intent_type.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'network_intent_method_channel.dart';

abstract class NetworkIntentPlatform extends PlatformInterface {
  NetworkIntentPlatform() : super(token: _token);

  static final Object _token = Object();

  static NetworkIntentPlatform _instance = MethodChannelNetworkIntent();

  static NetworkIntentPlatform get instance => _instance;

  static set instance(NetworkIntentPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  /// Init Discovery
  Future<bool> initDiscovery() {
    throw UnimplementedError('startDiscovery() has not been implemented.');
  }

  /// Enable Discovery
  Future<bool> enableDiscovery() {
    throw UnimplementedError('enableDiscovery() has not been implemented.');
  }

  /// Disable Discovery
  Future<void> disableDiscovery() {
    throw UnimplementedError('disableDiscovery() has not been implemented.');
  }

  /// On Event Listener
  StreamSubscription<dynamic> onListener({
    DiscoveryStarted? onDiscoveryStarted,
    DiscoveryStopped? onDiscoveryStopped,
    DiscoveryError? onDiscoveryError,
    DiscoveryResult? onDiscoveryResult,
  }) {
    throw UnimplementedError('onListener() has not been implemented.');
  }

  void sendMessage(String message) {
    throw UnimplementedError('sendMessage() has not been implemented.');
  }

  Future<void> dispose() {
    throw UnimplementedError('dispose() has not been implemented.');
  }
}
