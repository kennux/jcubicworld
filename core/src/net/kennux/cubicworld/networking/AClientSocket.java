package net.kennux.cubicworld.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;

import net.kennux.cubicworld.util.ConsoleHelper;

/**
 * <pre>
 * Abstract client socket implementation. The client socket will run in it's own
 * thread and read data.
 * It will enquene the data in a FIFO stack, so an implementation of this class
 * can read it in an update() method on the main thread.
 * 
 * All functions of this implementation are thread-safe.
 * </pre>
 * 
 * @author KennuX
 *
 */
public abstract class AClientSocket implements Runnable
{
	/**
	 * The client socket instance.
	 */
	protected Socket clientSocket;

	/**
	 * The socket's input stream retrieved by getInputStream().
	 */
	protected InputStream inputStream;

	/**
	 * The socket's output stream retrieved by getOutputStream().
	 */
	protected OutputStream outputStream;
	protected Object outputLockObject = new Object();

	/**
	 * The packets recieved in the socket thread. If you access this object you
	 * need to lock the lockobject.
	 */
	protected LinkedList<IPacketModel> packetsRecieved; // FIFO stack-like
	protected Object packetsRecievedLockObject = new Object();

	/**
	 * The packets sended by calling sendPacket().
	 * If you access this object you need to lock the lockobject.
	 */
	protected LinkedList<IPacketModel> packetsToSend; // FIFO stack-like
	protected Object packetsToSendLockObject = new Object();

	/**
	 * The thread which runs this run() method.
	 */
	private Thread socketReadThread;

	/**
	 * The thread which handles packet sending.
	 */
	private Thread socketWriterThread;

	/**
	 * The bytes recieved from downstream this tick.
	 */
	private int bytesDownstream;
	/**
	 * The bytes sent to upstream this tick.
	 */
	private int bytesUpstream;

	private final int recieveBufferSize = 1024 * 1024 * 1; // 1mb
	private final int sendBufferSize = 1024 * 1024 * 1; // 1mb
	private final int socketTimeout = 6000 * 1000; // 60 seconds

	/**
	 * This constructor has to be overloaded in your own implementation!
	 * 
	 * @throws IOException
	 */
	public AClientSocket(Socket clientSocket) throws IOException
	{
		this.clientSocket = clientSocket;
		this.clientSocket.setSoTimeout(socketTimeout);

		// Set buffers
		this.clientSocket.setReceiveBufferSize(recieveBufferSize);
		this.clientSocket.setSendBufferSize(sendBufferSize); // 1 mb buffer

		// Get stream
		this.outputStream = this.clientSocket.getOutputStream();
		this.inputStream = this.clientSocket.getInputStream();

		this.packetsRecieved = new LinkedList<IPacketModel>();
		this.packetsToSend = new LinkedList<IPacketModel>();

		this.socketReadThread = new Thread(this);
		this.socketReadThread.setName("Socket read thread: " + this.getClass().getSimpleName() + " - " + this.clientSocket.getRemoteSocketAddress());
		this.socketReadThread.start();

		final AClientSocket cSocket = this;
		this.socketWriterThread = new Thread(new Runnable()
		{
			public void run()
			{
				while (true)
				{
					LinkedList<IPacketModel> packetsSelected = new LinkedList<IPacketModel>();
					synchronized (cSocket.packetsToSendLockObject)
					{
						if (cSocket.packetsToSend.size() > 0)
						{
							// Select packets from packet stack.
							while (cSocket.packetsToSend.size() > 0)
							{
								packetsSelected.push(cSocket.packetsToSend.pop());
							}
						}
					}

					// Send out all packets
					if (packetsSelected.size() > 0)
					{
						for (IPacketModel packetModel : packetsSelected)
						{
							cSocket.sendPacketDirect(packetModel);
						}

						packetsSelected.clear();
					}

					try
					{
						Thread.sleep(10);
					}
					catch (InterruptedException e)
					{ /* Not expected to happen */
					}
				}
			}
		});
		this.socketWriterThread.setName("Socket write thread: " + this.getClass().getSimpleName() + " - " + this.clientSocket.getRemoteSocketAddress());
		this.socketWriterThread.start();
	}

	/**
	 * <pre>
	 * Closes this socket and it's streams.
	 * Gets called if the player normally disconnected or there was an error
	 * while reading.
	 * After the close() was called, the server update thread's cleanup routine
	 * will remove this instance from the clients array.
	 * </pre>
	 */
	@SuppressWarnings("deprecation")
	public void close()
	{
		ConsoleHelper.writeLog("INFO", "Client connection dropped " + this.clientSocket, "ClientSocket");

		try
		{
			if (this.clientSocket != null)
				this.clientSocket.close();
			if (this.inputStream != null)
				this.inputStream.close();
			if (this.outputStream != null)
				this.outputStream.close();
			if (this.socketReadThread != null)
				this.socketReadThread.stop();

			this.clientSocket = null;
		}
		catch (Exception e)
		{
		}
	}

	/**
	 * @return the bytesDownstream
	 */
	public int getBytesDownstream()
	{
		return bytesDownstream;
	}

	/**
	 * @return the bytesUpstream
	 */
	public int getBytesUpstream()
	{
		return bytesUpstream;
	}

	/**
	 * Returns the next packet on the stack if there is one available. Returns
	 * null if there no packet.
	 * 
	 * @return
	 */
	protected IPacketModel getPacket()
	{
		synchronized (this.packetsRecievedLockObject)
		{
			if (this.hasPacket())
				return this.packetsRecieved.removeFirst();
			else
				return null;
		}
	}

	/**
	 * <pre>
	 * Performs a getClientPacket or getServerPacket call to resolve the packet
	 * id to a IPacketModel istance.
	 * 
	 * You have to implement this function.
	 * On the server it will return the client packet with packet id packetId.
	 * On the client it will return server packet instance.
	 * 
	 * This gets used in run() to enquene packet instances.
	 * </pre>
	 * 
	 * @param packetId
	 * @return
	 */
	protected abstract IPacketModel getPacketInstance(short packetId) throws InstantiationException, IllegalAccessException;

	/**
	 * Returns true if there is a packet on the current packet stack.
	 * 
	 * @return
	 */
	protected boolean hasPacket()
	{
		synchronized (this.packetsRecievedLockObject)
		{
			return this.packetsRecieved.size() > 0;
		}
	}

	/**
	 * Returns if this client is alive.
	 * 
	 * Code: this.clientSocket != null
	 * 
	 * @return
	 */
	public boolean isAlive()
	{
		return this.clientSocket != null; // && !this.clientSocket.isClosed();
	}

	/**
	 * Reads a byte array from the socket of the given size. If the socket
	 * read() method runs out of data this function will call it again till the
	 * data is completely read. Will return null if the socket closed.
	 * 
	 * @param size
	 * @return
	 * @throws IOException
	 */
	private byte[] readReliable(int size) throws IOException
	{
		// Init data array and need data flag
		byte[] data = new byte[size];
		int dataRead = 0;

		while (dataRead < size)
		{
			// Read data this step
			int readThisStep = this.inputStream.read(data, dataRead, size - dataRead);

			if (readThisStep == -1)
				return null;

			dataRead += readThisStep;
		}

		return data;
	}

	/**
	 * Socket thread function. Reads data from the socket in blocking mode.
	 * If data arrived it will get added to the packets stack which can get
	 * interpreted by using hasPacket() and getPacket().
	 */
	@Override
	public void run()
	{
		// Still alive?
		while (this.isAlive())
		{
			try
			{
				// Read header
				byte[] header = this.readReliable(6);
				if (header == null)
				{
					this.close();
					return;
				}

				// Read packet header values
				short packetId = (short) (header[1] & 0xFF | (header[0] & 0xFF) << 8);

				int packetLength = header[5] & 0xFF | (header[4] & 0xFF) << 8 | header[3] & 0xFF << 16 | (header[2] & 0xFF) << 24;

				byte[] data = this.readReliable(packetLength);
				if (data == null)
				{
					this.close();
					return;
				}

				this.setBytesDownstream(this.getBytesDownstream() + packetLength + 6); // 6 = header size

				BitReader reader = new BitReader(data);

				// Get packet from protocol
				IPacketModel packetModel = null;
				try
				{
					packetModel = this.getPacketInstance(packetId);
				}
				catch (Exception e)
				{
					ConsoleHelper.writeLog("error", "Packet instantiation for packet id " + packetId + " failed!", "ServerClient");
				}

				if (packetModel != null)
				{
					// Read & add to stack
					packetModel.readPacket(reader);

					synchronized (this.packetsRecievedLockObject)
					{
						this.packetsRecieved.add(packetModel);
					}
				}
				else
				{
					ConsoleHelper.writeLog("ERROR", "Got packet with unknown model. PacketId: " + packetId + ", PacketLength: " + packetLength, "ServerClient");
				}
			}
			catch (Exception e)
			{
				ConsoleHelper.writeLog("error", "Exception in client socket thread!\r\nClosing Connection!", "AClientSocket");
				ConsoleHelper.logError(e);
				e.printStackTrace();
				this.close();
			}
		}
	}

	/**
	 * Adds the given packet to the packets to send stack.
	 * 
	 * @param packet
	 * @throws IOException
	 */
	public void sendPacket(IPacketModel packet)
	{
		synchronized (this.packetsToSendLockObject)
		{
			this.packetsToSend.push(packet);
		}
	}

	/**
	 * Sends a packet to this client immediately!
	 * 
	 * @param packet
	 * @throws IOException
	 */
	protected void sendPacketDirect(IPacketModel packet)
	{
		BitWriter headerBuilder = new BitWriter();
		BitWriter dataBuilder = new BitWriter();

		// Write header

		packet.writePacket(dataBuilder);

		headerBuilder.writeShort(packet.getPacketId());
		headerBuilder.writeInt(dataBuilder.getLength());

		this.setBytesUpstream(this.getBytesUpstream() + dataBuilder.getLength() + 6); // 6 = header size

		// System.out.println("write packet: " + packet + "|"+this);
		// Write to socket
		try
		{
			synchronized (this.outputLockObject)
			{
				this.outputStream.write(headerBuilder.getPacket());
				this.outputStream.write(dataBuilder.getPacket());
				this.outputStream.flush();
			}
		}
		catch (IOException e)
		{
			this.close();
		}
	}

	/**
	 * @param bytesDownstream
	 *            the bytesDownstream to set
	 */
	private void setBytesDownstream(int bytesDownstream)
	{
		this.bytesDownstream = bytesDownstream;
	}

	/**
	 * @param bytesUpstream
	 *            the bytesUpstream to set
	 */
	private void setBytesUpstream(int bytesUpstream)
	{
		this.bytesUpstream = bytesUpstream;
	}

	/**
	 * Call this every tick / frame. It will reset the bytes counter.
	 * 
	 * @params playerPosition The Player position used for chunk requesting.
	 * @throws IOException
	 */
	public void update()
	{
		// Reset counters
		this.setBytesDownstream(0);
		this.setBytesUpstream(0);
	}
}
