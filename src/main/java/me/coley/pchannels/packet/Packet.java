package me.coley.pchannels.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Basic packet outline.
 *
 * @author Matt Coley
 */
public interface Packet
{
    /**
     * @return Packet identifier.
     */
    int getId();

    /**
     * @return Length of content to be created by {@link #write(DataOutputStream)}.
     */
    int getLength();

    /**
     * @param in Data stream to read from.
     * @throws IOException When reading from the stream fails.
     */
    void read(DataInputStream in) throws IOException;

    /**
     * @param out Data stream to write to.
     * @throws IOException When writing to the stream fails.
     */
    void write(DataOutputStream out) throws IOException;
}
