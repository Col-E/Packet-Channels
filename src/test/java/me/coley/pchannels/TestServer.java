package me.coley.pchannels;

import me.coley.pchannels.packet.PacketFactory;
import me.coley.pchannels.packet.PacketHandlerDelegator;
import me.coley.pchannels.packet.PacketIO;
import me.coley.pchannels.packet.impl.ChatPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ByteChannel;

public class TestServer extends Server {
	private static final Logger logger = LoggerFactory.getLogger(TestServer.class);

	public TestServer() throws IOException {
		super();
	}

	@Override
	protected void setup(PacketHandlerDelegator delegator) {
		delegator.register(TestConstants.ID_CHAT, this::handleChat);
	}

	private boolean handleChat(ByteChannel channel, ChatPacket packet) {
		String msg = packet.getMessage();
		logger.info("<" + channel + "> " + msg);
		if (msg.contains("ping")) {
			try {
				PacketIO.write(channel, new ChatPacket("pong!"));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return true;
	}

	static {
		PacketFactory.register(TestConstants.ID_CHAT, ChatPacket::new);
	}

	public static void main(String[] args) {
		try {
			Server server = new TestServer();
			server.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
