package me.coley.pchannels.bench;

import me.coley.pchannels.Client;
import me.coley.pchannels.Server;
import me.coley.pchannels.Threads;
import me.coley.pchannels.packet.PacketFactory;
import me.coley.pchannels.packet.PacketHandlerDelegator;
import me.coley.pchannels.packet.PacketIO;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class JmhRunner {
	private static final Random RANDOM = new Random();
	private static final ExecutorService EXECUTOR = Threads.pool();
	private static final int SOCKET_UDP = 6666;
	private static final int SOCKET_TCP = 6667;
	private static final int SOCKET_PCHANNELS = 6668;
	private static final int BUFF_SIZE = 8192;
	private static final byte[] CLIENT_MESSAGE = genData();

	@Benchmark
	public void netUdp(UdpSocket socket) {
		socket.clientAction();
	}

	@Benchmark
	public void netTcp(TcpSocket socket) {
		socket.clientAction();
	}

	@Benchmark
	public void netChannels(PacketChannelsImpl socket) {
		socket.clientAction();
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
				.include(JmhRunner.class.getName() + ".*")
				.mode(Mode.Throughput)
				.timeUnit(TimeUnit.MILLISECONDS)
				.warmupTime(TimeValue.milliseconds(500))
				.warmupIterations(3)
				.measurementTime(TimeValue.milliseconds(500))
				.measurementIterations(5)
				.threads(1)
				.forks(1)
				.shouldFailOnError(true)
				.shouldDoGC(true)
				//.jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
				//.addProfiler(WinPerfAsmProfiler.class)
				.build();
		new Runner(opt).run();
	}

	private static byte[] genData() {
		byte[] data = new byte[BUFF_SIZE];
		RANDOM.nextBytes(data);
		return data;
	}

	static {
		PacketFactory.register(ByteArrayPacket.ID, ByteArrayPacket::new);
	}

	private interface ClientServer {
		void serverStart();

		void clientStart();

		void clientStop();

		void clientAction();
	}

	@State(Scope.Benchmark)
	public static class PacketChannelsImpl implements ClientServer {
		private final Server server;
		private final Client client;
		private volatile boolean running;
		private Future<?> loopThread;
		private int ack;

		public PacketChannelsImpl() {
			try {
				// TODO: Fix this from freezing. The JMH never finishes execution due to a hang somewhere...
				//   - Can use this JMH to optimize and rework PacketChannels design to fit our use case
				//      - Single connection in the common case
				//      - Often to connecting to diff process on same machine, but allowing for remote connection as well
				//      - High throughput ideal
				server = new Server(SOCKET_PCHANNELS) {
					@Override
					protected void setup(PacketHandlerDelegator delegator) {
						// Reply to client with same data
						delegator.register(ByteArrayPacket.ID, ((channel, packet) -> {
							try {
								PacketIO.write(channel, packet);
								return running;
							} catch (IOException ex) {
								throw new IllegalStateException(ex);
							}
						}));
					}
				};
				client = new Client(SOCKET_PCHANNELS) {
					@Override
					protected void setup(PacketHandlerDelegator delegator) {
						// Server will respond with same packet, so we increment ACK here
						delegator.register(ByteArrayPacket.ID, ((channel, packet) -> {
							ack++;
							return running;
						}));
					}
				};
			} catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		}


		@Override
		public void serverStart() {
			EXECUTOR.submit(() -> {
				try {
					server.start();
				} catch (Exception ex) {
					throw new IllegalStateException(ex);
				}
			});
		}

		@Override
		@Setup(Level.Trial)
		public void clientStart() {
			try {
				System.out.println("Start PacketChannels");
				client.start();
			} catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		}

		@Override
		@TearDown(Level.Trial)
		public void clientStop() {
			System.out.println("Stop PacketChannels, acks=" + ack);
			running = false;
			try {
				client.close();
				server.close();
			} catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		}

		@Override
		public void clientAction() {
			try {
				PacketIO.write(client.getChannel(), new ByteArrayPacket(CLIENT_MESSAGE));
			} catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		}
	}

	@State(Scope.Benchmark)
	public static class TcpSocket implements ClientServer {
		private final InetAddress address;
		private final ServerSocket server;
		private Socket client;
		private Future<?> loopThread;
		private boolean running;
		private int ack;

		public TcpSocket() {
			try {
				System.out.println("Setup TCP");
				address = InetAddress.getByName("localhost");
				server = new ServerSocket(SOCKET_TCP);
			} catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		}

		@Override
		public void serverStart() {
			try {
				running = true;
				Socket socket = server.accept();
				byte[] data = new byte[BUFF_SIZE];
				while (running) {
					// Read incoming
					socket.getInputStream().read(data);
					// Reply with same data
					socket.getOutputStream().write(data);
				}
				server.close();
			} catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		}

		@Override
		@Setup(Level.Trial)
		public void clientStart() {
			System.out.println("Start TCP");
			this.loopThread = EXECUTOR.submit(this::serverStart);
			try {
				client = new Socket(address, SOCKET_TCP);
			} catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		}

		@Override
		@TearDown(Level.Trial)
		public void clientStop() {
			System.out.println("Stop TCP, acks=" + ack);
			running = false;
			try {
				client.close();
				server.close();
			} catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
			loopThread.cancel(true);
		}

		@Override
		public void clientAction() {
			try {
				// Send buf
				client.getOutputStream().write(CLIENT_MESSAGE);
				// Get response
				client.getInputStream().read(CLIENT_MESSAGE);
				ack++;
			} catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		}
	}

	@State(Scope.Benchmark)
	public static class UdpSocket implements ClientServer {
		private final InetAddress address;
		private final DatagramSocket server;
		private final DatagramSocket client;
		private Future<?> loopThread;
		private boolean running;
		private int ack;

		public UdpSocket() {
			try {
				System.out.println("Setup UDP");
				address = InetAddress.getByName("localhost");
				server = new DatagramSocket(SOCKET_UDP);
				client = new DatagramSocket();
			} catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		}


		@Override
		public void serverStart() {
			try {
				running = true;
				while (running) {
					// Read incoming
					DatagramPacket packet = new DatagramPacket(CLIENT_MESSAGE, CLIENT_MESSAGE.length);
					server.receive(packet);

					// Reply with same data
					InetAddress address = packet.getAddress();
					int port = packet.getPort();
					packet = new DatagramPacket(CLIENT_MESSAGE, CLIENT_MESSAGE.length, address, port);
					server.send(packet);
				}
				server.close();
			} catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		}

		@Override
		@Setup(Level.Trial)
		public void clientStart() {
			System.out.println("Start UDP");
			this.loopThread = EXECUTOR.submit(this::serverStart);
		}

		@Override
		@TearDown(Level.Trial)
		public void clientStop() {
			System.out.println("Stop UDP, acks=" + ack);
			running = false;
			client.close();
			server.close();
			loopThread.cancel(true);
		}

		@Override
		public void clientAction() {
			try {
				// Send buf
				DatagramPacket packet = new DatagramPacket(CLIENT_MESSAGE, CLIENT_MESSAGE.length, address, SOCKET_UDP);
				client.send(packet);
				// Get response
				packet = new DatagramPacket(CLIENT_MESSAGE, CLIENT_MESSAGE.length);
				client.receive(packet);
				ack++;
			} catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		}
	}
}
