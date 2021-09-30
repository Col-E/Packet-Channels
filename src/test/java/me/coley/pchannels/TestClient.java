package me.coley.pchannels;

import me.coley.pchannels.packet.PacketHandlerDelegator;
import me.coley.pchannels.packet.PacketIO;
import me.coley.pchannels.packet.impl.ChatPacket;

import java.io.IOException;

public class TestClient extends Client {
	public TestClient(int port) throws IOException {
		super(port);
	}

	@Override
	protected void setup(PacketHandlerDelegator delegator) {

	}

	public static void main(String[] a) {
		try {
			// TODO: Once we start doing feedback from the server, make sure to use "AlivePacket" to check if the server is alive
			// every now and then.
			Client client = new TestClient(Constants.PORT);
			client.start();
			Thread.sleep(100);
			PacketIO.write(client.getChannel(), new ChatPacket("Hello 1"));
			Thread.sleep(500);
			PacketIO.write(client.getChannel(), new ChatPacket("Hello 2"));
			Thread.sleep(1500);
			PacketIO.write(client.getChannel(), new ChatPacket("Hello 3"));
			client.stop();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
