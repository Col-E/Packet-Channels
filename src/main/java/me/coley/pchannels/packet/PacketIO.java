package me.coley.pchannels.packet;

import me.coley.pchannels.packet.impl.ClosePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SocketChannel;

/**
 * Common packet IO operations.
 *
 * @author Matt Coley
 */
public class PacketIO
{
    private static final Logger logger = LoggerFactory.getLogger(PacketIO.class);

    /**
     * Occupys the current thread with handling packets incoming from the given channel.
     *
     * @param channel   Channel to read packets from.
     * @param condition Loop condition lookup.
     * @param delegator Delegator with registered {@link PacketHandler}s to handle incoming packets.
     * @throws IOException When reading packets fails.
     */
    public static void handleLoop(ByteChannel channel, PacketLoopCondition condition, PacketHandlerDelegator delegator) throws IOException
    {
        while (condition.shouldContinue())
        {
            Packet packet = PacketFactory.read(channel);
            if (!delegator.handle(channel, packet))
            {
                break;
            }
        }
    }

    /**
     * @param channel Channel to write the packet to.
     * @param packet  Packet to write.
     * @throws IOException When serializing the packet fails, or when the channel cannot be written to.
     */
    public static void write(ByteChannel channel, Packet packet) throws IOException
    {
        ByteBuffer buffer = PacketFactory.write(packet);
        channel.write(buffer);
    }

    /**
     * Writes a {@link ClosePacket} to the channel then closes the channel.
     * If the packet cannot be sent {@link SocketChannel#close()} will still be called.
     *
     * @param channel Channel to close.
     * @throws IOException When the channel cannot be closed.
     */
    public static void close(ByteChannel channel) throws IOException
    {
        try
        {
            write(channel, new ClosePacket());
        } catch (IOException ex)
        {
            logger.error("Could not write close packet to channel: {}", channel, ex);
        }
        channel.close();
    }
}
