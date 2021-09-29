package me.coley.pchannels.packet.impl;

import me.coley.pchannels.Constants;
import me.coley.pchannels.packet.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Packet used to indicate a connection should be closed.
 *
 * @author Matt Coley
 */
public class ClosePacket implements Packet
{
    @Override
    public int getId()
    {
        return Constants.ID_CLOSE;
    }

    @Override
    public int getLength()
    {
        return 0;
    }

    @Override
    public void read(DataInputStream in)
    {
        // no-op
    }

    @Override
    public void write(DataOutputStream out)
    {
        // no-op
    }
}
