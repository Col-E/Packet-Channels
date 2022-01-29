package me.coley.pchannels;

import me.coley.pchannels.packet.PacketFactory;
import me.coley.pchannels.packet.PacketHandlerDelegator;
import me.coley.pchannels.packet.impl.ChatPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ByteChannel;

public class TestClient extends Client {
	private static final Logger logger = LoggerFactory.getLogger(TestClient.class);

	public TestClient(int port) throws IOException {
		super(port);
	}

	@Override
	protected void setup(PacketHandlerDelegator delegator) {
		delegator.register(TestConstants.ID_CHAT, this::handleChat);
	}

	private boolean handleChat(ByteChannel channel, ChatPacket packet) {
		String msg = packet.getMessage();
		logger.info("<" + channel + "> " + msg);
		return true;
	}

	static {
		PacketFactory.register(TestConstants.ID_CHAT, ChatPacket::new);
	}

	public static void main(String[] a) {
		try {
			Client client = new TestClient(Constants.PORT);
			client.start();
			client.write(new ChatPacket("ping"));
			logger.info("ping");
			Thread.sleep(1000);
			client.write(new ChatPacket("goodbye"));
			logger.info("goodbye");
			Thread.sleep(1000);
			client.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
