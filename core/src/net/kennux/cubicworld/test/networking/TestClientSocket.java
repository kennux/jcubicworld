package net.kennux.cubicworld.test.networking;

import java.io.IOException;
import java.net.Socket;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.networking.AClientSocket;
import net.kennux.cubicworld.networking.IPacketModel;
import net.kennux.cubicworld.networking.Protocol;

/**
 * TODO Rewrite this to define complex test cases in the socket, so the test tells the client socket how the data traffic should look like.
 * 
 * @author KennuX
 *
 */
public class TestClientSocket extends AClientSocket
{
	private CubicWorldGame master;

	@SuppressWarnings("rawtypes")
	public Class awaitPacketClass;

	public boolean awaitedPacketGot = false;

	public TestClientSocket(Socket clientSocket, CubicWorldGame master) throws IOException
	{
		super(clientSocket);
		this.master = master;
	}

	public boolean awaitedPacketRecieved()
	{
		boolean ret = this.awaitedPacketGot;
		this.awaitedPacketGot = false;
		return ret;
	}

	public void directSend(IPacketModel model)
	{
		this.sendPacketDirect(model);
	}

	@Override
	protected IPacketModel getPacketInstance(short packetId) throws InstantiationException, IllegalAccessException
	{
		return Protocol.getPacket(packetId);
	}

	public void update()
	{
		super.update();
		while (this.hasPacket())
		{
			// Get next packet model
			IPacketModel packet = this.getPacket();

			if (packet == null)
				break;

			if (this.awaitPacketClass != null)
				if (packet.getClass() == this.awaitPacketClass)
				{
					this.awaitedPacketGot = true;
					this.awaitPacketClass = null;
				}

			if (this.master != null)
				packet.interpretClientSide(this.master);
		}
	}
}
