package net.kennux.cubicworld.microbenchmark;

import java.lang.reflect.Field;

import com.badlogic.gdx.math.Vector3;

import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;
import net.kennux.cubicworld.serialization.Serializer;
import net.kennux.cubicworld.test.SerializerTest;
import net.kennux.cubicworld.test.serializer.SerializerTestClass;

public class ReflectFieldAccess extends AMicroBenchmark
{
	public static void main(String[] args)
	{
		ReflectFieldAccess benchmark = new ReflectFieldAccess();
		benchmark.benchmark();
	}
	
	private SerializerTestClass testClass = new SerializerTestClass();
	private Field testField;
	
	public ReflectFieldAccess()
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
		
		try
		{
			this.testField = this.testClass.getClass().getDeclaredField("b");
		}
		catch (NoSuchFieldException | SecurityException e)
		{
			
		}
	}

	@MicroBenchmark(name = "Reflect field access", iterations = 10000)
	public void reflectFieldAccess()
	{
		try
		{
			this.testField.get(this.testClass);
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
