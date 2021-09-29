package me.coley.pchannels.packet;

import me.coley.pchannels.Constants;
import me.coley.pchannels.packet.impl.AlivePacket;
import me.coley.pchannels.packet.impl.ClosePacket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Packet IO handler for reading and writing {@link Packet} content.
 *
 * @author Matt Coley
 */
public class PacketFactory
{
    private static final ByteBuffer HEADER_BUFFER = ByteBuffer.allocate(Constants.HEADER_BUFFER_SIZE);
    private static final Map<Integer, Supplier<Packet>> SUPPLIERS = new HashMap<>();

    static
    {
        register(Constants.ID_ALIVE, AlivePacket::new);
        register(Constants.ID_CLOSE, ClosePacket::new);
    }

    /**
     * @return Current registered packet suppliers.
     * Key values are {@link Packet#getId()}.
     */
    public static Map<Integer, Supplier<Packet>> getPacketSuppliers()
    {
        return SUPPLIERS;
    }

    /**
     * Registers a packet supplier to the given packet ID.
     *
     * @param id       Packet identifier.
     * @param supplier Supplier to create new instances of the packet <i>(For reading)</i>.
     */
    public static void register(int id, Supplier<Packet> supplier)
    {
        if (SUPPLIERS.containsKey(id))
        {
            throw new IllegalArgumentException("The packet ID " + id + " is already registered!");
        }
        SUPPLIERS.put(id, supplier);
    }

    /**
     * @param channel Channel to read remaining data from.
     * @return Packet from the given information.
     * @throws IOException When a packet's deserialization fails.
     */
    public static Packet read(ByteChannel channel) throws IOException
    {
        int id;
        int size;
        synchronized (HEADER_BUFFER)
        {
            // Fill the buffer and
            channel.read(HEADER_BUFFER);
            HEADER_BUFFER.position(0);
            // Get packet information and handle the rest of the data
            id = HEADER_BUFFER.get();
            size = HEADER_BUFFER.getInt();
            // Reset buffer for next call
            HEADER_BUFFER.clear();
        }
        // Read remaining data into buffer
        ByteBuffer buffer = ByteBuffer.allocate(size);
        if (size > 0)
        {
            channel.read(buffer);
        }
        byte[] data = buffer.array();
        // Create and read packet data
        Packet packet = fromId(id);
        if (packet.getId() != id)
        {
            throw new IllegalStateException("Packet registered to " + id + " has different id " + packet.getId());
        }
        packet.read(new DataInputStream(new ByteArrayInputStream(data)));
        return packet;
    }

    /**
     * @param packet Packet with data to write.
     * @return Buffer containing packet's data.
     * @throws IOException When a packet's serialization fails.
     */
    public static ByteBuffer write(Packet packet) throws IOException
    {
        // Create expanding byte[] using streams
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        // Write header info
        dos.writeByte(packet.getId());
        dos.writeInt(packet.getLength());
        // Write packet data
        packet.write(dos);
        // Wrap in buffer
        byte[] raw = baos.toByteArray();
        return ByteBuffer.wrap(raw);
    }

    /**
     * @param id Some key value mapping to a {@link Packet#getId()}.
     * @return New packet instance of associated type.
     */
    private static Packet fromId(int id)
    {
        Supplier<Packet> packetSupplier = SUPPLIERS.get(id);
        if (packetSupplier == null)
        {
            throw new IllegalStateException("Unknown packet id: " + id);
        }
        return packetSupplier.get();
    }
}
