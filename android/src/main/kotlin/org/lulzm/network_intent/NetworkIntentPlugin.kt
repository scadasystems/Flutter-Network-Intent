package org.lulzm.network_intent

import android.content.Context
import android.content.Intent
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


    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "network_intent")
        channel.setMethodCallHandler(this)
        eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "network_intent_events")
        eventChannel.setStreamHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "initDiscovery" -> startDiscovery(result)
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

    private fun startDiscovery(result: Result) {
        if (!discoveryStarted) {
            discovery = Discovery()
            discovery?.setDisoveryListener(this)
            transmitter = Transmitter()
            discoveryStarted = true
        }

        result.success(discoveryStarted)
    }

    private fun enableDiscovery(result: Result) {
        try {
            discovery?.enable()
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

        val message = call.argument<String>("message")

        Log.i("LulzM", "message: $message")

        val intent = Intent()
        intent.putExtra(INTENT_KEY, message)

        Thread {
            transmitter?.transmit(intent)
        }.start()

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
        if (intent?.hasExtra(INTENT_KEY) == false) {
            return
        }

        val result = intent?.getStringExtra(INTENT_KEY)

        eventHandler.post {
            eventSink?.success(
                mapOf(
                    "type" to NetworkIntentType.DISCOVERY_RESULT.name,
                    "address" to address?.hostAddress,
                    "result" to result,
                )
            )
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        eventChannel.setStreamHandler(null)
        eventSink = null
    }
}
