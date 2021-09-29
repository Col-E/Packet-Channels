package me.coley.pchannels;

import me.coley.pchannels.packet.PacketHandler;
import me.coley.pchannels.packet.PacketHandlerDelegator;
import me.coley.pchannels.packet.PacketIO;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

/**
 * A basic client setup using NIO {@link SocketChannel}s.
 *
 * @author Matt Coley
 */
public abstract class Client
{
    private final InetAddress address;
    private final int port;
    private SocketChannel channel;

    /**
     * Client connection on localhost using the given port.
     *
     * @param port Port that a {@link Server} is running on.
     * @throws IOException When localhost cannot be resolved.
     */
    public Client(int port) throws IOException
    {
        this(InetAddress.getLocalHost(), port);
    }

    /**
     * Client connection to a given address on the given port.
     *
     * @param address Address of the remote {@link Server}.
     * @param port    Port that a {@link Server} is running on.
     */
    public Client(InetAddress address, int port)
    {
        this.address = address;
        this.port = port;
    }

    /**
     * Starts the channel connection to the server.
     *
     * @throws IOException When {@link SocketChannel#open(SocketAddress)} fails.
     */
    public void start() throws IOException
    {
        channel = SocketChannel.open(new InetSocketAddress(address, port));
    }

    /**
     * Closes the channel connection and tells the server we're leaving.
     *
     * @throws IOException When writing the closure packet fails, or when closing the channel fails.
     */
    public void stop() throws IOException
    {
        PacketIO.close(channel);
    }

    /**
     * @return Address of the server connected to.
     */
    public InetAddress getAddress()
    {
        return address;
    }

    /**
     * @return Port to connect on.
     */
    public int getPort()
    {
        return port;
    }

    /**
     * @return Socket channel connection to a {@link Server}.
     */
    public SocketChannel getChannel()
    {
        return channel;
    }

    /**
     * @param delegator Delegator to register additional {@link PacketHandler}s with.
     */
    protected abstract void setup(PacketHandlerDelegator delegator);
}
