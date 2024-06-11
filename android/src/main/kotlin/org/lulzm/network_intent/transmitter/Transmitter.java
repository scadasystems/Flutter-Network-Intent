package org.lulzm.network_intent.transmitter;

import android.content.Intent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.lulzm.network_intent.AndroidNetworkIntents;
import org.lulzm.network_intent.discovery.Discovery;

/**
 * Transmitter class for sending {@link Intent}s through network.
 */
public class Transmitter {
    private String multicastAddress;
    private int port;

    /**
     * Creates a new {@link Transmitter} instance that will sent {@link Intent}s to
     * the default multicast address and port.
     */
    public Transmitter() {
        this(
                AndroidNetworkIntents.DEFAULT_MULTICAST_ADDRESS,
                AndroidNetworkIntents.DEFAULT_PORT
        );
    }

    /**
     * Creates a new {@link Transmitter} instance that will sent {@link Intent}s to
     * the default multicast address and the given port port.
     *
     * @param port The destination network port.
     */
    public Transmitter(int port) {
        this(
                AndroidNetworkIntents.DEFAULT_MULTICAST_ADDRESS,
                port
        );
    }

    /**
     * Creates a new {@link Transmitter} instance that will sent {@link Intent}s to
     * the given multicast address and port.
     *
     * @param multicastAddress The destination multicast address, e.g. 225.4.5.6.
     * @param port             The destination network port.
     */
    public Transmitter(String multicastAddress, int port) {
        this.multicastAddress = multicastAddress;
        this.port = port;
    }

    /**
     * Sends an {@link Intent} through the network to any listening {@link Discovery}
     * instance.
     *
     * @param intent The intent to send.
     * @throws TransmitterException if intent could not be transmitted.
     */
    public void transmit(Intent intent) throws TransmitterException {
        try (MulticastSocket socket = createSocket()) {
            transmit(socket, intent);
        } catch (UnknownHostException exception) {
            throw new TransmitterException("Unknown host", exception);
        } catch (SocketException exception) {
            throw new TransmitterException("Can't create DatagramSocket", exception);
        } catch (IOException exception) {
            throw new TransmitterException("IOException during sending intent", exception);
        }
    }

    protected MulticastSocket createSocket() throws IOException {
        return new MulticastSocket();
    }

    /**
     * Actual (private) implementation that serializes the {@link Intent} and sends
     * it as {@link DatagramPacket}. Used to separate the implementation from the
     * error handling code.
     */
    private void transmit(MulticastSocket socket, Intent intent) throws IOException {
        byte[] data = intent.toUri(0).getBytes();

        DatagramPacket packet = new DatagramPacket(
                data,
                data.length,
                InetAddress.getByName(multicastAddress),
                port
        );

        socket.send(packet);
    }
}
