package me.coley.pchannels;

import me.coley.pchannels.packet.Packet;
import me.coley.pchannels.packet.PacketHandler;
import me.coley.pchannels.packet.PacketHandlerDelegator;
import me.coley.pchannels.packet.PacketIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A basic client setup using NIO {@link SocketChannel}s.
 *
 * @author Matt Coley
 */
public abstract class Client {
	private static final Logger logger = LoggerFactory.getLogger(Client.class);
	private final ExecutorService executorService = Executors.newWorkStealingPool();
	private final PacketHandlerDelegator delegator = new PacketHandlerDelegator();
	private final InetAddress address;
	private final int port;
	private SocketChannel channel;
	private Future<?> handlerThread;


	/**
	 * Client connection on localhost using the given port.
	 *
	 * @param port
	 * 		Port that a {@link Server} is running on.
	 *
	 * @throws IOException
	 * 		When localhost cannot be resolved.
	 */
	public Client(int port) throws IOException {
		this(InetAddress.getLocalHost(), port);
	}

	/**
	 * Client connection to a given address on the given port.
	 *
	 * @param address
	 * 		Address of the remote {@link Server}.
	 * @param port
	 * 		Port that a {@link Server} is running on.
	 */
	public Client(InetAddress address, int port) {
		this.address = address;
		this.port = port;
		setup(delegator);
	}

	/**
	 * Starts the channel connection to the server.
	 *
	 * @throws IOException
	 * 		When {@link SocketChannel#open(SocketAddress)} fails.
	 */
	public void start() throws IOException {
		channel = SocketChannel.open(new InetSocketAddress(address, port));
		handlerThread = executorService.submit(() -> {
			logger.debug("Connected: {}", address);
			try {
				PacketIO.handleLoop(channel, channel::isConnected, delegator);
			} catch (Throwable t) {
				logger.error("Error: {}", address, t);
			}
			logger.debug("Disconnect: {}", address);
		});
	}

	/**
	 * Closes the channel connection and tells the server we're leaving.
	 *
	 * @throws IOException
	 * 		When writing the closure packet fails, or when closing the channel fails.
	 */
	public void stop() throws IOException {
		// Stop the handling thread (stops reading new items)
		handlerThread.cancel(true);
		// Send a closure notice and leave
		PacketIO.close(channel);
		// The interrupt handler causes the channel to close.
		executorService.shutdownNow();
	}

	/**
	 * Writes the given packet to the {@link #getChannel() remote server's socket channel}.
	 *
	 * @param packet
	 * 		Packet to write to the server.
	 *
	 * @throws IOException
	 * 		When the packet cannot be written.
	 */
	public void write(Packet packet) throws IOException {
		PacketIO.write(getChannel(), packet);
	}

	/**
	 * @return Address of the server connected to.
	 */
	public InetAddress getAddress() {
		return address;
	}

	/**
	 * @return Port to connect on.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return Socket channel connection to a remote {@link Server}.
	 */
	public SocketChannel getChannel() {
		return channel;
	}

	/**
	 * @param delegator
	 * 		Delegator to register additional {@link PacketHandler}s with.
	 */
	protected abstract void setup(PacketHandlerDelegator delegator);
}
