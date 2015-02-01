package net.kennux.cubicworld.test;

import java.io.IOException;
import java.net.Socket;

import junit.framework.TestCase;
import net.kennux.cubicworld.networking.packet.ClientLogin;
import net.kennux.cubicworld.networking.packet.ServerPlayerSpawn;
import net.kennux.cubicworld.test.networking.TestClientSocket;

import org.junit.Test;

/**
 * Tests the server connection and login protocol.
 * 
 * @author KennuX
 *
 */
public class NetworkingProtocolTest extends TestCase
{
	@Test
	public void testConnection()
	{
		// Try connect & disconnect 10 times
		for (int i = 0; i < 10; i++)
		{
			Socket clientSocket = null;
			TestClientSocket testClientSocket = null;
			boolean success = true;

			// Test connection
			try
			{
				clientSocket = new Socket("127.0.0.1", 1337);
				testClientSocket = new TestClientSocket(clientSocket, null);
			}
			catch (IOException e)
			{
				success = false;
			}

			// Was connection successfull?
			assertTrue(success && clientSocket.isConnected());

			// Wait for some time to let the server accept the connection
			try
			{
				Thread.sleep(100);
			}
			catch (Exception e)
			{
			}

			assertTrue(Tests.serverInstance.clients[0] != null && Tests.serverInstance.clients[0].isAlive());

			// Send login packet
			ClientLogin loginPacket = new ClientLogin();
			loginPacket.username = "TEST" + i;
			testClientSocket.awaitPacketClass = ServerPlayerSpawn.class;
			testClientSocket.directSend(loginPacket);

			// Wait for the server to accept the login
			try
			{
				Thread.sleep(1000);
			}
			catch (Exception e)
			{
			}

			testClientSocket.update();

			// Await spawn packet
			assertTrue(testClientSocket.awaitedPacketRecieved());

			// Check if login was successfull

			assertTrue(Tests.serverInstance.clients[0] != null && Tests.serverInstance.clients[0].playerEntity != null && Tests.serverInstance.clients[0].playerEntity.getEntityName().equals("TEST" + i));

			// Close socket
			testClientSocket.close();

			// Wait for some time to let the server drop the connection
			try
			{
				Thread.sleep(100);
			}
			catch (Exception e)
			{
			}

			// Check if connection got dropped
			assertTrue(Tests.serverInstance.clients[0] == null || !Tests.serverInstance.clients[0].isAlive());
		}
	}
}
