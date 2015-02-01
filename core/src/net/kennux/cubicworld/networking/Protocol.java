package net.kennux.cubicworld.networking;

import java.util.HashMap;

/**
 * <pre>
 * Protocol information holder.
 * Register packets in your bootstrap.
 * 
 * It differs between client and server packets.
 * It can get used to create instances of the packet models.
 * </pre>
 * 
 * @author KennuX
 *
 */
public class Protocol
{
	/**
	 * Adds a client -> server packet model to the protocol.
	 * 
	 * @param model
	 */
	public static void addPacket(APacketModel model)
	{
		// Add packet to list
		if (!packets.containsValue(model.getClass()))
		{
			packets.put(packetCounter, model.getClass());
			packetClassNameToIdMap.put(model.getClass().getName(), new Short(packetCounter));
			packetCounter++;
		}
	}

	/**
	 * Instantiates a new packet with the given packet id. Returns null if the
	 * packet was not found.
	 * 
	 * @param packetId
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static IPacketModel getPacket(short packetId) throws InstantiationException, IllegalAccessException
	{
		if (packets.containsKey(new Short(packetId)))
		{
			return (IPacketModel) packets.get(new Short(packetId)).newInstance();
		}

		return null;
	}

	/**
	 * Returns the packet id of the given packet model.
	 * Returns -1 if there is no such packet registered.
	 * 
	 * @param model
	 * @return
	 */
	public static short getPacketId(APacketModel model)
	{
		// Get model class name
		String className = model.getClass().getName();

		if (packetClassNameToIdMap.containsKey(className))
		{
			return packetClassNameToIdMap.get(className).shortValue();
		}

		return -1;
	}

	/**
	 * Client -> Server packets.
	 */
	@SuppressWarnings("rawtypes")
	private static HashMap<Short, Class> packets = new HashMap<Short, Class>();

	/**
	 * Maps the server packet's class names to an id map.
	 * This hashmap contains server packets and client packets.
	 * The key is the class name with it's package path.
	 */
	private static HashMap<String, Short> packetClassNameToIdMap = new HashMap<String, Short>();

	private static short packetCounter;
}
