package net.kennux.cubicworld.microbenchmark;

public class ArrayAccessBenchmark extends AMicroBenchmark
{
	public static void main(String[] args)
	{
		ArrayAccessBenchmark benchmark = new ArrayAccessBenchmark();
		benchmark.benchmark();
	}

	private int[] testData;

	public ArrayAccessBenchmark()
	{
		this.testData = new int[100000];
	}

	@MicroBenchmark(name = "read benchmark", iterations = 10000)
	public void readBenchmark()
	{
		for (int i = 0; i < this.testData.length; i++)
		{
			int j = this.testData[i];
		}
	}

	@MicroBenchmark(name = "write benchmark", iterations = 10000)
	public void writeBenchmark()
	{
		for (int i = 0; i < this.testData.length; i++)
		{
			this.testData[i] = i;
		}
	}

}
