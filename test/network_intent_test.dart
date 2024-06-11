import 'dart:async';

import 'package:flutter_test/flutter_test.dart';
import 'package:network_intent/network_intent_method_channel.dart';
import 'package:network_intent/network_intent_platform_interface.dart';
import 'package:network_intent/src/network_intent_type.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockNetworkIntentPlatform with MockPlatformInterfaceMixin implements NetworkIntentPlatform {
  @override
  Future<void> disableDiscovery() {
    throw UnimplementedError();
  }

  @override
  Future<bool> enableDiscovery() {
    throw UnimplementedError();
  }

  @override
  Future<void> dispose() {
    throw UnimplementedError();
  }

  @override
  StreamSubscription onListener(
      {DiscoveryStarted? onDiscoveryStarted,
      DiscoveryStopped? onDiscoveryStopped,
      DiscoveryError? onDiscoveryError,
      DiscoveryResult? onDiscoveryResult}) {
    throw UnimplementedError();
  }

  @override
  Future<bool> initDiscovery(
      {String address = '225.4.5.6', int port = 5775, String intentName = 'NETWORK_INTENT_MESSAGE'}) {
    throw UnimplementedError();
  }

  @override
  Future<void> sendMessage(String message) {
    throw UnimplementedError();
  }
}

void main() {
  final NetworkIntentPlatform initialPlatform = NetworkIntentPlatform.instance;

  test('$MethodChannelNetworkIntent is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelNetworkIntent>());
  });

  // test('getPlatformVersion', () async {
  //   NetworkIntent networkIntentPlugin = NetworkIntent();
  //   MockNetworkIntentPlatform fakePlatform = MockNetworkIntentPlatform();
  //   NetworkIntentPlatform.instance = fakePlatform;

  //   expect(await networkIntentPlugin.getPlatformVersion(), '42');
  // });
}
