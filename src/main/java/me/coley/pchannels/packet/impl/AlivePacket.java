package me.coley.pchannels.packet.impl;

import me.coley.pchannels.Client;
import me.coley.pchannels.Constants;
import me.coley.pchannels.Server;
import me.coley.pchannels.packet.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * No-op packet used by a {@link Client} to check if a {@link Server} is online.
 * If an exception is thrown when writing the packet then the {@link Server} is closed.
 * <br>
 * This packet needs to exist because {@code SocketChannel} has no way to actually check if the remote
 * connection is dead.
 *
 * @author Matt Coley
 */
public class AlivePacket implements Packet {
	@Override
	public int getId() {
		return Constants.ID_ALIVE;
	}

	@Override
	public int getLength() {
		return 0;
	}

	@Override
	public void read(DataInputStream in) {
		// no-op
	}

	@Override
	public void write(DataOutputStream out) {
		// no-op
	}
}
