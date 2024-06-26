package org.lulzm.network_intent.discovery;

import android.content.Intent;

import org.lulzm.network_intent.AndroidNetworkIntents;

public class Discovery {
    private String multicastAddress;
    private int port;

    private DiscoveryListener listener;
    private DiscoveryThread thread;

    /**
     * Create a new {@link Discovery} instance that will listen to the default
     * multicast address and port for incoming {@link Intent}s.
     */
    public Discovery() {
        this(
                AndroidNetworkIntents.DEFAULT_MULTICAST_ADDRESS,
                AndroidNetworkIntents.DEFAULT_PORT
        );
    }

    /**
     * Create a new {@link Discovery} instance that will listen to the default
     * multicast address and the given port for incoming {@link Intent}s.
     *
     * @param port The network port to listen to.
     */
    public Discovery(int port) {
        this(
                AndroidNetworkIntents.DEFAULT_MULTICAST_ADDRESS,
                port
        );
    }

    /**
     * Create a new {@link Discovery} instance that will listen to the given
     * multicast address and port for incoming {@link Intent}s.
     *
     * @param multicastAddress The multicast address to listen to, e.g. 225.4.5.6.
     * @param port The port to listen to.
     */
    public Discovery(String multicastAddress, int port) {
        this.multicastAddress = multicastAddress;
        this.port = port;
    }

    /**
     * Set the {@link DiscoveryListener} instance that will be notified about
     * incoming {@link Intent}s.
     *
     * @param listener The {@link DiscoveryListener} that will be notified about
     *                 incoming {@link Intent}s.
     */
    public void setDiscoveryListener(DiscoveryListener listener) {
        this.listener = listener;
    }

    /**
     * Enables the {@link Discovery} so that it will monitor the network for
     * {@link Intent}s and notify the given {@link DiscoveryListener} instance.
     *
     * This is a shortcut for:
     * <code>
     * discovery.setDiscoveryListener(listener);
     * discovery.enable();
     * </code>
     *
     * @param listener The {@link DiscoveryListener} that will be notified about
     *                 incoming {@link Intent}s.
     * @throws DiscoveryException if discovery could not be enabled.
     */
    public void enable(DiscoveryListener listener) throws DiscoveryException {
        setDiscoveryListener(listener);
        enable();
    }

    /**
     * Enables the {@link Discovery} so that it will monitor the network for
     * {@link Intent}s and notify the set {@link DiscoveryListener} instance.
     *
     */
    public void enable() {
        if (listener == null) {
            throw new IllegalStateException("No listener set");
        }

        if (thread == null) {
            thread = createDiscoveryThread();
            thread.start();
        } else {
            throw new IllegalAccessError("Discovery already started");
        }
    }

    protected DiscoveryThread createDiscoveryThread() {
        return new DiscoveryThread(multicastAddress, port, listener);
    }

    /**
     * Disables the {@link Discovery}.
     *
     * @throws IllegalAccessError if this {@link Discovery} is not running
     */
    public void disable() {
        if (thread != null) {
            thread.stopDiscovery();
            thread = null;
        } else {
            throw new IllegalAccessError("Discovery not running");
        }
    }
}
