package net.kennux.cubicworld.microbenchmark;

import com.badlogic.gdx.math.Vector3;

import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;
import net.kennux.cubicworld.serialization.Serializer;
import net.kennux.cubicworld.test.SerializerTest;
import net.kennux.cubicworld.test.serializer.SerializerTestClass;

public class SerializationBenchmark extends AMicroBenchmark
{
	public static void main(String[] args)
	{
		SerializationBenchmark benchmark = new SerializationBenchmark();
		benchmark.benchmark();
	}
	
	private SerializerTestClass testClass = new SerializerTestClass();
	private byte[] data;
	
	public SerializationBenchmark()
	{
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
		
		BitWriter writer = new BitWriter();
		testClass.serialize(writer);
		this.data = writer.getPacket();
		
		// Warmup
		writer = new BitWriter();
		BitReader reader = new BitReader(data);
		Serializer.serialize(writer, testClass);
		Serializer.deserialize(reader, SerializerTestClass.class);
	}

	@MicroBenchmark(name = "Procedural serialize", iterations = 10000)
	public void proceduralSerialize()
	{
		BitWriter writer = new BitWriter();
		testClass.serialize(writer);
	}

	@MicroBenchmark(name = "Procedural deserialize", iterations = 10000)
	public void proceduralDeserialize()
	{
		BitReader reader = new BitReader(data);
		SerializerTestClass.deserialize(reader);
	}

	@MicroBenchmark(name = "Annotation-driven serialize", iterations = 10000)
	public void annotationDrivenSerialize()
	{
		BitWriter writer = new BitWriter();
		Serializer.serialize(writer, testClass);
	}

	@MicroBenchmark(name = "Annotation-driven deserialize", iterations = 10000)
	public void annotationDrivenDeserialize()
	{
		BitReader reader = new BitReader(data);
		Serializer.deserialize(reader, SerializerTestClass.class);
	}

}
