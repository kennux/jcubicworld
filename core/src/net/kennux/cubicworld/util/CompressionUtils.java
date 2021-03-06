package net.kennux.cubicworld.util;

import java.io.IOException;
import java.util.zip.DataFormatException;

import org.iq80.snappy.Snappy;

/**
 * Deflate code copied from: http://qupera.blogspot.de/2013/02/howto-compress-and-uncompress-java-byte.html
 * 
 * @author kennux
 *
 */
public class CompressionUtils
{
	/**
	 * Compresses the given bytes of data.
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public static byte[] compress(byte[] data) throws IOException
	{
		/*
		 * Deflater deflater = new Deflater();
		 * deflater.setInput(data);
		 * 
		 * ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		 * 
		 * deflater.finish();
		 * byte[] buffer = new byte[1024];
		 * while (!deflater.finished())
		 * {
		 * int count = deflater.deflate(buffer); // returns the generated code... index
		 * outputStream.write(buffer, 0, count);
		 * }
		 * outputStream.close();
		 * byte[] output = outputStream.toByteArray();
		 * 
		 * deflater.end();
		 * 
		 * return output;
		 */
		return Snappy.compress(data);
	}

	/**
	 * Decompresses the given bytes of data.
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 * @throws DataFormatException
	 */
	public static byte[] decompress(byte[] data) throws IOException, DataFormatException
	{
		/*
		 * Inflater inflater = new Inflater();
		 * inflater.setInput(data);
		 * 
		 * ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		 * byte[] buffer = new byte[1024];
		 * while (!inflater.finished())
		 * {
		 * int count = inflater.inflate(buffer);
		 * outputStream.write(buffer, 0, count);
		 * }
		 * outputStream.close();
		 * byte[] output = outputStream.toByteArray();
		 * 
		 * inflater.end();
		 * 
		 * return output;
		 */
		return Snappy.uncompress(data, 0, data.length);
	}
}