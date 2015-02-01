package net.kennux.cubicworld.test;

import junit.framework.TestCase;
import net.kennux.cubicworld.plugins.baseplugin.BasePlugin;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;
import net.kennux.cubicworld.serialization.SerializationTypes;
import net.kennux.cubicworld.serialization.Serializer;
import net.kennux.cubicworld.serialization.annotations.SerializerField;
import net.kennux.cubicworld.test.serializer.SerializerTestClass;
import net.kennux.cubicworld.voxel.VoxelData;
import net.kennux.cubicworld.voxel.VoxelEngine;

import org.junit.Test;

import com.badlogic.gdx.math.Vector3;

public class SerializerTest extends TestCase
{
	/**
	 * Tests reading / writing all data types
	 */
	@Test
	public void testSerializer()
	{
		SerializerTestClass testClass = new SerializerTestClass();
		testClass.b = (byte)123;
		testClass.bA = new byte[] { 12, 32, 45 };
		testClass.bool = true;
		testClass.c = 'a';
		testClass.f = 2.5776f;
		testClass.i = 1337;
		testClass.l = 13371l;
		testClass.s = (short) 133;
		testClass.str = "LEET";
		testClass.v3 = new Vector3(10, 13, 37);
		
		// Serialize
		BitWriter writer = new BitWriter();
		Serializer.serialize(writer, testClass);
		
		// Deserialize
		BitReader reader = new BitReader(writer.getPacket());
		SerializerTestClass testClass2 = Serializer.deserialize(reader, testClass.getClass());
		
		// Perform assertions
		assertEquals(testClass.b, testClass2.b);
		
		for (int i = 0; i < testClass2.bA.length; i++)
			assertEquals(testClass.bA[i], testClass2.bA[i]);
		
		assertEquals(testClass.bool, testClass2.bool);
		assertEquals(testClass.c, testClass2.c);
		assertEquals(testClass.f, testClass2.f);
		assertEquals(testClass.i, testClass2.i);
		assertEquals(testClass.l, testClass2.l);
		assertEquals(testClass.s, testClass2.s);
		assertEquals(testClass.str, testClass2.str);
		assertEquals(testClass.v3.x, testClass2.v3.x);
		assertEquals(testClass.v3.y, testClass2.v3.y);
		assertEquals(testClass.v3.z, testClass2.v3.z);
	}
}
