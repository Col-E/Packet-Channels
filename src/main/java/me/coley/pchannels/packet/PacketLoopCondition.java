package me.coley.pchannels.packet;

/**
 * Wrapper for what would otherwise be a {@code Supplier<Boolean>}.
 * Allows further abstraction in {@link PacketIO} by not relying on implementation specific channel methods.
 *
 * @author Matt Coley
 */
public interface PacketLoopCondition {
	/**
	 * The intended usage will be to wrap some channel implementation's state getter.
	 * For example:
	 * <pre>
	 *     SocketChannel chanel = ...
	 *     PacketLoopCondition condition = channel::isConnected;
	 * </pre>
	 *
	 * @return {@code true} to continue reading packet data from a channel.
	 * {@code false} to stop reading data.
	 */
	boolean shouldContinue();
}
