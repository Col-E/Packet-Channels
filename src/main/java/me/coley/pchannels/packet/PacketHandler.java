package me.coley.pchannels.packet;

import java.nio.channels.ByteChannel;

/**
 * A handler for any number of packets of any type.
 *
 * @author Matt Coley
 */
public interface PacketHandler<P extends Packet> {
	/**
	 * @param channel
	 * 		The socket channel the packet originates from.
	 * @param packet
	 * 		Current packet to handle.
	 *
	 * @return {@code true} to continue handling further packets.
	 * {@code false} to abort handling.
	 */
	boolean handlePacket(ByteChannel channel, P packet);
}
