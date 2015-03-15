package net.kennux.cubicworld.microbenchmark;

public class FloatingPointBenchmark extends AMicroBenchmark
{
	public static void main(String[] args)
	{
		FloatingPointBenchmark benchmark = new FloatingPointBenchmark();
		benchmark.benchmark();
	}

	public FloatingPointBenchmark()
	{
	}

	@MicroBenchmark(name = "Float operations", iterations = 100000)
	public void floatBenchmark()
	{
		float f = 1.14f;
		float f2 = f * 2.25f;

		float f3 = ((f / f2) * f2) + (f - f2);
	}

	@MicroBenchmark(name = "Double operations", iterations = 100000)
	public void doubleBenchmark()
	{
		double f = 1.14;
		double f2 = f * 2.25;

		double f3 = ((f / f2) * f2) + (f - f2);
	}

	@MicroBenchmark(name = "Integer operations", iterations = 100000)
	public void intBenchmark()
	{
		int i = 1;
		int i2 = i * 2;

		int i3 = ((i / i2) * i2) + (i - i2);
	}

}
