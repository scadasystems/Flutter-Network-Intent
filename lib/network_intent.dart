import 'dart:async';

import 'package:network_intent/src/network_intent_type.dart';

import 'network_intent_platform_interface.dart';

class NetworkIntent {
  /// Init Discovery
  /// [return]: true if success, false if fail
  Future<bool> initDiscovery({
    String address = '225.4.5.6',
    int port = 5775,
    String intentName = 'NETWORK_INTENT_MESSAGE',
  }) {
    return NetworkIntentPlatform.instance.initDiscovery(address: address, port: port, intentName: intentName);
  }

  /// Enable Discovery
  /// [return]: true if success
  Future<bool> enableDiscovery() {
    return NetworkIntentPlatform.instance.enableDiscovery();
  }

  /// Disable Discovery
  Future<void> disableDiscovery() {
    return NetworkIntentPlatform.instance.disableDiscovery();
  }

  /// Discovery Listener
  /// - [onDiscoveryStarted] : Start Discovery Event
  /// - [onDiscoveryStopped] : Stop Discovery Event
  /// - [onDiscoveryError] : Discovery Error Event
  ///   - [return]: Error message
  /// - [onDiscoveryResult] : Discovery Result Event
  ///   - [return]:
  ///     - [address]: Device IP Address
  ///     - [result]: Device message
  StreamSubscription<dynamic> onListener({
    DiscoveryStarted? onDiscoveryStarted,
    DiscoveryStopped? onDiscoveryStopped,
    DiscoveryError? onDiscoveryError,
    DiscoveryResult? onDiscoveryResult,
  }) {
    return NetworkIntentPlatform.instance.onListener(
      onDiscoveryStarted: onDiscoveryStarted,
      onDiscoveryStopped: onDiscoveryStopped,
      onDiscoveryError: onDiscoveryError,
      onDiscoveryResult: onDiscoveryResult,
    );
  }

  /// Send Message
  /// - Only support string
  Future<void> sendMessage(String message) {
    return NetworkIntentPlatform.instance.sendMessage(message);
  }

  /// Dispose
  Future<void> dispose() {
    return NetworkIntentPlatform.instance.dispose();
  }
}
