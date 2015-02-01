package net.kennux.cubicworld.test;

import junit.framework.TestCase;
import net.kennux.cubicworld.networking.BitReader;
import net.kennux.cubicworld.networking.BitWriter;
import net.kennux.cubicworld.plugins.baseplugin.BasePlugin;
import net.kennux.cubicworld.voxel.VoxelData;
import net.kennux.cubicworld.voxel.VoxelEngine;

import org.junit.Test;

import com.badlogic.gdx.math.Vector3;

public class BitReaderWriterTest extends TestCase
{
	/**
	 * Tests reading / writing all data types
	 */
	@Test
	public void testReadWrite()
	{
		// Init the writer
		BitWriter writer = new BitWriter();

		// Write all data types
		writer.writeBool(true);
		writer.writeBool(false);
		writer.writeByte((byte) 123);
		writer.writeBytes(new byte[] { 12, 34, 56 });
		writer.writeChar('a');
		writer.writeChar('z');
		writer.writeFloat(2.556f);
		writer.writeInt(1337);
		writer.writeShort((short) 1337);
		writer.writeString("KennuX is KinG");
		writer.writeVector3(new Vector3(10, 13, 37));
		writer.writeVoxelData(VoxelData.construct((short) BasePlugin.voxelBedrockId));

		// Create reader
		BitReader reader = new BitReader(writer.getPacket());

		// Test for expected results
		assertTrue(reader.readBoolean());
		assertFalse(reader.readBoolean());
		assertEquals((byte) 123, reader.readByte());
		byte[] data = reader.readBytes();
		assertEquals((byte) 12, data[0]);
		assertEquals((byte) 34, data[1]);
		assertEquals((byte) 56, data[2]);
		assertEquals('a', reader.readChar());
		assertEquals('z', reader.readChar());
		assertEquals(2.556f, reader.readFloat());
		assertEquals(1337, reader.readInt());
		assertEquals((short) 1337, reader.readShort());
		assertEquals("KennuX is KinG", reader.readString());
		assertEquals(new Vector3(10, 13, 37), reader.readVector3());
		assertEquals(VoxelEngine.getVoxelType(BasePlugin.voxelBedrockId), reader.readVoxelData().voxelType);

	}
}
