# Packet Channels

A simple client/server library utilizing packet structures over `java.nio.channels`.

## Usage

Create an extension of the `Client` and `Server` classes. Call `start()` on the server then on a client.

By default, the only supported packets are a simple alive check (`AlivePacket`), 
and a connection close notification (`ClosePacket`). 

To register more packet types:

```java
PacketFactory.register(PACKET_ID, CustomPacket::new);
```

And to add custom behavior in response to a packet, both the client and server follow the same semantics:

```java
// In the client or server implementations
@Override
protected void setup(PacketHandlerDelegator delegator) {
    delegator.register(PACKET_ID, this::handleCustom);
}

private boolean handleCustom(ByteChannel channel, CustomPacket packet) {
    // ... do something with 'packet'
    return true;
}
```