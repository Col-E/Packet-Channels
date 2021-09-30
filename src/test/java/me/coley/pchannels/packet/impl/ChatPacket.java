package me.coley.pchannels.packet.impl;

import me.coley.pchannels.TestConstants;
import me.coley.pchannels.packet.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChatPacket implements Packet {
	private String message;

	public ChatPacket() {
		this(null);
	}

	public ChatPacket(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public int getId() {
		return TestConstants.ID_CHAT;
	}

	@Override
	public int getLength() {
		// UTF length short + UTF content length
		return 2 + message.length();
	}

	@Override
	public void read(DataInputStream in) throws IOException {
		message = in.readUTF();
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeUTF(message);
	}
}
