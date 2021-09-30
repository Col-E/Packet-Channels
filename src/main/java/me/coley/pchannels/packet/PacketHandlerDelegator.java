package me.coley.pchannels.packet;

import me.coley.pchannels.Client;
import me.coley.pchannels.Constants;
import me.coley.pchannels.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.ByteChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstracts away common {@link PacketHandler}s between the {@link Client} and {@link Server}.
 *
 * @author Matt Coley
 */
public class PacketHandlerDelegator {
	private static final Logger logger = LoggerFactory.getLogger(PacketHandlerDelegator.class);
	private final Map<Integer, PacketHandler<?>> handlers = new HashMap<>();

	/**
	 * Create the delegator.
	 */
	public PacketHandlerDelegator() {
		register(Constants.ID_ALIVE, ((channel, packet) -> true));
		register(Constants.ID_CLOSE, ((channel, packet) -> false));
	}

	/**
	 * @param id
	 * 		Packet ID.
	 * @param handler
	 * 		Packet handler for the type associated with the ID.
	 * @param <P>
	 * 		Packet type.
	 */
	public <P extends Packet> void register(int id, PacketHandler<P> handler) {
		handlers.put(id, handler);
	}

	/**
	 * @param channel
	 * 		Channel associated with the packet.
	 * @param packet
	 * 		The packet to handle.
	 *
	 * @return {@code true} to continue handling further packets.
	 * {@code false} to stop handling packets.
	 */
	@SuppressWarnings("unchecked")
	public boolean handle(ByteChannel channel, Packet packet) {
		int id = packet.getId();
		PacketHandler<Packet> handler = (PacketHandler<Packet>) handlers.get(id);
		if (handler == null) {
			logger.warn("No handler for packet: {}", packet.getClass().getSimpleName());
			return true;
		}
		return handler.handlePacket(channel, packet);
	}
}
