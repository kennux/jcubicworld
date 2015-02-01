package net.kennux.cubicworld.serialization;

/**
 * This interface gets used for implementing serializers.
 * 
 * @author KennuX
 */
public interface ISerializer
{
	/**
	 * The serialize method.
	 * @param object
	 * @return 
	 * @return
	 */
	public void serialize(BitWriter writer, Object object);
	
	/**
	 * The deserialize method.
	 * @param data
	 * @return
	 */
	public Object deserialize(BitReader reader);
}
