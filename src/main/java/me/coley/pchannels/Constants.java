package me.coley.pchannels;

/**
 * Constants.
 *
 * @author Matt Coley
 */
public class Constants {
	/**
	 * Default port.
	 */
	public static final int PORT = 7777;
	/**
	 * 1 byte for ID.
	 * <br>
	 * 4 bytes for size.
	 */
	public static final int HEADER_BUFFER_SIZE = 5;

	// ==========================
	// === PACKET IDENTIFIERS ===
	// ==========================

	/**
	 * ID for alive checks.
	 */
	public static final int ID_ALIVE = 0;
	/**
	 * ID for closing connections.
	 */
	public static final int ID_CLOSE = 1;
}
