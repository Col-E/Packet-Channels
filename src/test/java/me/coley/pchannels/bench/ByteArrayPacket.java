package me.coley.pchannels.bench;

import me.coley.pchannels.packet.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ByteArrayPacket implements Packet {
	public static final int ID = 93;
	private byte[] array;

	public ByteArrayPacket() {
		this(null);
	}

	public ByteArrayPacket(byte[] array) {
		this.array = array;
	}

	@Override
	public int getId() {
		return ID;
	}

	@Override
	public int getLength() {
		return 2 + array.length;
	}

	@Override
	public void read(DataInputStream in) throws IOException {
		array = new byte[in.readUnsignedShort()];
		in.read(array);
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeShort(array.length);
		out.write(array);
	}
}
