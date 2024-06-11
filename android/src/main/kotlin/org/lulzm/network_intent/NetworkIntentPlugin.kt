package org.lulzm.network_intent

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.MulticastLock
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.StreamHandler
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.lulzm.network_intent.discovery.Discovery
import org.lulzm.network_intent.discovery.DiscoveryListener
import org.lulzm.network_intent.transmitter.Transmitter
import java.net.InetAddress


class NetworkIntentPlugin : FlutterPlugin, MethodCallHandler, StreamHandler, DiscoveryListener {
    private lateinit var context: Context
    private lateinit var channel: MethodChannel
    private lateinit var eventChannel: EventChannel
    private var eventSink: EventChannel.EventSink? = null

    private var discovery: Discovery? = null
    private var transmitter: Transmitter? = null

    private var discoveryStarted = false

    private var INTENT_KEY = "NETWORK_INTENT_MESSAGE"

    private val eventHandler: Handler = Handler(Looper.getMainLooper())
    private val discoveryHandler: Handler = Handler(Looper.getMainLooper())

    private lateinit var wifiManager: WifiManager
    private lateinit var wifiLock: WifiManager.WifiLock
    private lateinit var multicastLock: MulticastLock

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "network_intent")
        channel.setMethodCallHandler(this)
        eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "network_intent_events")
        eventChannel.setStreamHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "initDiscovery" -> startDiscovery(call, result)
            "enableDiscovery" -> enableDiscovery(result)
            "disableDiscovery" -> disableDiscovery(result)
            "sendMessage" -> sendMessage(call, result)
            else -> result.notImplemented()
        }
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        eventSink = events
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
    }

    private fun startDiscovery(call: MethodCall, result: Result) {
        wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiLock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            wifiManager.createWifiLock(WifiManager.WIFI_INTERFACE_TYPE_AP, "network_intent")
        } else {
            wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "network_intent")
        }
        multicastLock = wifiManager.createMulticastLock("network_intent")
        acquireWifiLock()
        acquireMulticastLock()

        val address = call.argument<String>("address")
        val port = call.argument<Int>("port")
        val intentName = call.argument<String>("intentName")

        if (!address.isNullOrEmpty()) {
            AndroidNetworkIntents.DEFAULT_MULTICAST_ADDRESS = address
        }

        if (port != null) {
            AndroidNetworkIntents.DEFAULT_PORT = port
        }

        if (!intentName.isNullOrEmpty()) {
            INTENT_KEY = intentName
        }

        if (!discoveryStarted) {
            discovery = Discovery(
                AndroidNetworkIntents.DEFAULT_MULTICAST_ADDRESS,
                AndroidNetworkIntents.DEFAULT_PORT
            )
            transmitter = Transmitter(
                AndroidNetworkIntents.DEFAULT_MULTICAST_ADDRESS,
                AndroidNetworkIntents.DEFAULT_PORT
            )
            discoveryStarted = true
        }

        result.success(discoveryStarted)
    }

    private fun enableDiscovery(result: Result) {
        try {
            discovery?.enable(this)
            discoveryStarted = true
            result.success(true)
        } catch (e: Exception) {
            discovery?.disable()
            discoveryStarted = false
            result.error("DiscoveryException", e.message, null)
        }
    }

    private fun disableDiscovery(result: Result) {
        if (discoveryStarted) {
            discovery?.disable()
        }

        result.success(null)
    }

    private fun sendMessage(call: MethodCall, result: Result) {
        if (!call.hasArgument("message")) {
            result.error("Missing argument", "message argument is required", null)
            return
        }

        val message = call.argument<String>("message") ?: "nothing"

        val intent = Intent()
        intent.putExtra(INTENT_KEY, message)

        CoroutineScope(Dispatchers.IO).launch {
            transmitter?.transmit(intent)
        }

        result.success(null)
    }

    override fun onDiscoveryStarted() {
        eventHandler.post {
            eventSink?.success(
                mapOf(
                    "type" to NetworkIntentType.DISCOVERY_STARTED.name,
                    "result" to "Discovery started",
                )
            )
        }
    }

    override fun onDiscoveryStopped() {
        eventHandler.post {
            eventSink?.success(
                mapOf(
                    "type" to NetworkIntentType.DISCOVERY_STOPPED.name,
                    "result" to "Discovery stopped",
                )
            )
        }
    }

    override fun onDiscoveryError(exception: Exception?) {
        eventHandler.post {
            eventSink?.success(
                mapOf(
                    "type" to NetworkIntentType.DISCOVERY_ERROR.name,
                    "error" to exception?.message,
                )
            )
        }
    }

    override fun onIntentDiscovered(address: InetAddress?, intent: Intent?) {
        val hasExtra = intent?.hasExtra(INTENT_KEY) ?: false

        Log.i("NetworkIntentPlugin", "onIntentDiscovered: $address, $hasExtra")

        if (!hasExtra) return

        val result = intent?.getStringExtra(INTENT_KEY)

        discoveryHandler.post {
            eventSink?.success(
                mapOf(
                    "type" to NetworkIntentType.DISCOVERY_RESULT.name,
                    "address" to address?.hostAddress,
                    "result" to result,
                )
            )
        }
    }

    private fun acquireMulticastLock() {
        Log.i("MulticastLock", "isHeld Before: ${multicastLock.isHeld}")

        if (!multicastLock.isHeld) {
            multicastLock.acquire()
        }

        Log.i("MulticastLock", "isHeld After: ${multicastLock.isHeld}")
    }

    private fun releaseMulticastLock() {
        if (multicastLock.isHeld) {
            multicastLock.release()
        }
    }

    private fun acquireWifiLock() {
        Log.i("WifiLock", "isHeld Before: ${wifiLock.isHeld}")

        if (!wifiLock.isHeld) {
            wifiLock.acquire()
        }

        Log.i("WifiLock", "isHeld After: ${wifiLock.isHeld}")
    }

    private fun releaseWifiLock() {
        if (wifiLock.isHeld) {
            wifiLock.release()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        eventHandler.removeCallbacksAndMessages(null)
        discoveryHandler.removeCallbacksAndMessages(null)
        channel.setMethodCallHandler(null)
        eventChannel.setStreamHandler(null)
        eventSink = null
        releaseWifiLock()
        releaseMulticastLock()
    }
}
