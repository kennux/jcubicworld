package net.kennux.cubicworld.microbenchmark;

import java.io.IOException;
import java.util.Random;
import java.util.zip.DataFormatException;

import net.kennux.cubicworld.serialization.BitWriter;
import net.kennux.cubicworld.util.CompressionUtils;
import net.kennux.cubicworld.voxel.VoxelData;
import net.kennux.cubicworld.voxel.VoxelEngine;

import org.iq80.snappy.Snappy;
/*
 * import net.jpountz.lz4.LZ4Compressor;
 * import net.jpountz.lz4.LZ4Factory;
 * import net.jpountz.lz4.LZ4FastDecompressor;
 * import net.jpountz.lz4.LZ4SafeDecompressor;
 */

public class CompressionBenchmark extends AMicroBenchmark
{
	/**
	 * Returns a pseudo-random number between min and max, inclusive.
	 * The difference between min and max can be at most <code>Integer.MAX_VALUE - 1</code>.
	 *
	 * @param min
	 *            Minimum value
	 * @param max
	 *            Maximum value. Must be greater than min.
	 * @return Integer between min and max, inclusive.
	 * @see java.util.Random#nextInt(int)
	 */
	public static int randInt(int min, int max)
	{

		// NOTE: Usually this should be a field rather than a method
		// variable so that it is not re-seeded every call.
		Random rand = new Random();

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}

	public static void main(String[] args) throws IOException
	{
		CompressionBenchmark benchmark = new CompressionBenchmark();
		benchmark.benchmark();
	}

	private byte[] testData;
	private byte[] compressed;
	private byte[] compressedSnappy;

	/*
	 * private byte[] compressedlz4;
	 * private byte[] compressedlz4High;
	 * 
	 * private LZ4Compressor lz4Compressor;
	 * private LZ4Compressor lz4Compressor2;
	 * private LZ4FastDecompressor lz4Decompressor;
	 */

	public CompressionBenchmark() throws IOException
	{
		VoxelEngine.initialize(128, 128);
		VoxelEngine.registerType("Test 1");
		VoxelEngine.registerType("Test 2");
		VoxelEngine.registerType("Test 3");
		VoxelEngine.registerType("Test 4");

		// Init lz4
		/*
		 * LZ4Factory lz4Factory = LZ4Factory.fastestInstance();
		 * this.lz4Compressor = lz4Factory.fastCompressor();
		 * this.lz4Decompressor = lz4Factory.fastDecompressor();
		 * this.lz4Compressor2 = lz4Factory.highCompressor();
		 */

		BitWriter writer = new BitWriter();
		int typeCount = 4;

		for (int x = 0; x < 16; x++)
			for (int y = 0; y < 16; y++)
				for (int z = 0; z < 16; z++)
				{
					int randomInt = randInt(0, typeCount - 1);
					writer.writeVoxelData(VoxelData.construct((short) randomInt));
				}
		this.testData = writer.getPacket();
		this.compressed = CompressionUtils.compress(this.testData);
		/*
		 * int maxlz4size = this.lz4Compressor.maxCompressedLength(this.testData.length);
		 * this.compressedlz4 = new byte[maxlz4size];
		 * int realCompressedLength = this.lz4Compressor.compress(this.testData, this.compressedlz4);
		 * 
		 * int maxlz4sizehigh = this.lz4Compressor2.maxCompressedLength(this.testData.length);
		 * this.compressedlz4High = new byte[maxlz4sizehigh];
		 * int realCompressedLengthHigh = this.lz4Compressor2.compress(this.testData, this.compressedlz4High);
		 */

		this.compressedSnappy = Snappy.compress(this.testData);

		System.out.println("Deflate compressed size: " + this.compressed.length);
		/*
		 * System.out.println("LZ4 fast compressed size: " + realCompressedLength);
		 * System.out.println("LZ4 high compressed size: " + realCompressedLengthHigh);
		 */
		System.out.println("Snappy compressed size: " + this.compressedSnappy.length);
		System.out.println("Uncompressed size: " + this.testData.length);

	}

	@MicroBenchmark(name = "deflate compress", iterations = 1000)
	public void deflateCompress() throws IOException
	{
		byte[] compressed = CompressionUtils.compress(this.testData);
	}

	@MicroBenchmark(name = "deflate decompress", iterations = 1000)
	public void deflateDecompress() throws IOException, DataFormatException
	{
		byte[] decompressed = CompressionUtils.decompress(this.compressed);
	}

	@MicroBenchmark(name = "snappy compress", iterations = 1000)
	public void snappyCompress()
	{
		byte[] compressed = Snappy.compress(this.testData);
	}

	@MicroBenchmark(name = "snappy decompress", iterations = 1000)
	public void snappyDecompress()
	{
		byte[] decompressed = Snappy.uncompress(this.compressedSnappy, 0, this.compressedSnappy.length);
	}

	/*
	 * @MicroBenchmark(name = "LZ4 fast compress", iterations = 1000)
	 * public void lz4FastCompress()
	 * {
	 * int maxlz4size = this.lz4Compressor.maxCompressedLength(this.testData.length);
	 * byte[] c = new byte[maxlz4size];
	 * int realCompressedLength = this.lz4Compressor.compress(this.testData, c);
	 * }
	 * 
	 * @MicroBenchmark(name = "LZ4 high compress", iterations = 1000)
	 * public void lz4HighCompress()
	 * {
	 * int maxlz4sizehigh = this.lz4Compressor2.maxCompressedLength(this.testData.length);
	 * byte[] c = new byte[maxlz4sizehigh];
	 * int realCompressedLengthHigh = this.lz4Compressor2.compress(this.testData, c);
	 * }
	 * 
	 * @MicroBenchmark(name = "LZ4 fast decompress", iterations = 1000)
	 * public void lz4FastDecompress()
	 * {
	 * byte[] data = new byte[this.testData.length];
	 * this.lz4Decompressor.decompress(this.compressedlz4, data);
	 * }
	 */
}
