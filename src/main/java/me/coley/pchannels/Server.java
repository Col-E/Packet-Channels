package me.coley.pchannels;

import me.coley.pchannels.packet.PacketHandler;
import me.coley.pchannels.packet.PacketHandlerDelegator;
import me.coley.pchannels.packet.PacketIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A basic server setup using NIO {@link SocketChannel}s.
 *
 * @author Matt Coley
 */
public abstract class Server {
	private static final Logger logger = LoggerFactory.getLogger(Server.class);
	private final ServerSocketChannel socket;
	private final ExecutorService executorService = Executors.newWorkStealingPool();
	private final Collection<SocketChannel> activeClients = new CopyOnWriteArraySet<>();
	private final PacketHandlerDelegator delegator = new PacketHandlerDelegator();
	private boolean accepting = true;

	/**
	 * @throws IOException
	 * 		When a {@link ServerSocketChannel} cannot be opened and bound to the default port.
	 */
	public Server() throws IOException {
		this(Constants.PORT);
	}

	/**
	 * @param port
	 * 		Port to run the server on.
	 *
	 * @throws IOException
	 * 		When a {@link ServerSocketChannel} cannot be opened and bound to the  port.
	 */
	public Server(int port) throws IOException {
		socket = ServerSocketChannel.open();
		socket.bind(new InetSocketAddress(port));
		setup(delegator);
	}

	/**
	 * Start the server. This will occupy the current thread with waiting on new client connections.
	 * To stop receiving new clients call {@link #stop()}.
	 *
	 * @throws IOException
	 * 		When {@link ServerSocketChannel#accept()} fails.
	 */
	public void start() throws IOException {
		while (accepting) {
			SocketChannel client = socket.accept();
			executorService.submit(() -> handle(client));
		}
	}

	/**
	 * Close all connections and stop the server.
	 *
	 * @throws IOException
	 * 		When clients cannot be notified of server closure.
	 */
	public void stop() throws IOException {
		// Notify all clients that the server is closing.
		for (SocketChannel channel : activeClients) {
			PacketIO.close(channel);
		}
		// Stop receiving new clients
		accepting = false;
		// Kill all client threads
		executorService.shutdownNow();
	}

	/**
	 * Handle a connection to a given client channel.
	 *
	 * @param channel
	 * 		Client channel connection.
	 */
	private void handle(SocketChannel channel) {
		SocketAddress address = null;
		try {
			address = channel.getRemoteAddress();
		} catch (IOException ex) {
			logger.error("Cannot get address from channel connection?", ex);
			return;
		}
		try {
			logger.debug("Connected: {}", address);
			activeClients.add(channel);
			PacketIO.handleLoop(channel, channel::isConnected, delegator);
		} catch (Throwable t) {
			logger.error("Error: {}", address, t);
		}
		logger.debug("Disconnect: {}", address);
		activeClients.remove(channel);
	}

	/**
	 * @return Server socket.
	 */
	public ServerSocketChannel getSocket() {
		return socket;
	}

	/**
	 * @return Channel connections to active {@link Client}s.
	 */
	public Collection<SocketChannel> getActiveClients() {
		return activeClients;
	}

	/**
	 * @return {@code true} when the server is open to receiving new channel connections.
	 */
	public boolean isAccepting() {
		return accepting;
	}

	/**
	 * @param delegator
	 * 		Delegator to register additional {@link PacketHandler}s with.
	 */
	protected abstract void setup(PacketHandlerDelegator delegator);
}
