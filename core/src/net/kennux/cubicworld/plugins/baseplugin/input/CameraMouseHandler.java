package net.kennux.cubicworld.plugins.baseplugin.input;

import net.kennux.cubicworld.CubicWorld;
import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.gui.hud.BlockSelectorGui;
import net.kennux.cubicworld.input.IMouseInputHandler;
import net.kennux.cubicworld.networking.packet.ClientVoxelUpdate;
import net.kennux.cubicworld.voxel.RaycastHit;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class CameraMouseHandler implements IMouseInputHandler
{
	private int lastMouseX = 0;
	private int lastMouseY = 0;

	/**
	 * How much deg will the camera rotate per pixel movement?
	 */
	private final float degPerPixel = 0.75f;

	@Override
	public void mouseButtonPressed(int buttonId)
	{
		CubicWorldGame cubicWorld = CubicWorld.getClient();
		RaycastHit hitInfo = null;

		switch (buttonId)
		{
			case 0:
				hitInfo = cubicWorld.currentBlockHit;

				if (hitInfo != null)
				{
					// Initialize remove voxel packet
					ClientVoxelUpdate removeVoxel = new ClientVoxelUpdate();
					removeVoxel.x = (int) hitInfo.hitVoxelPosition.x;
					removeVoxel.y = (int) hitInfo.hitVoxelPosition.y;
					removeVoxel.z = (int) hitInfo.hitVoxelPosition.z;
					removeVoxel.data = null;

					CubicWorld.getClient().client.sendPacket(removeVoxel);
				}

				break;
			case 1:
				// Get current block hit
				hitInfo = cubicWorld.currentBlockHit;

				// If the player is looking at a voxel
				if (hitInfo != null)
				{
					// Calculate the position of the new voxel.
					Vector3 blockPos = hitInfo.hitVoxelPosition;

					switch (hitInfo.hitFace)
					{
						case LEFT:
							blockPos.x--;
							break;
						case RIGHT:
							blockPos.x++;
							break;
						case TOP:
							blockPos.y++;
							break;
						case BOTTOM:
							blockPos.y--;
							break;
						case BACK:
							blockPos.z--;
							break;
						case FRONT:
							blockPos.z++;
							break;
					}

					// Initialize remove voxel packet
					ClientVoxelUpdate removeVoxel = new ClientVoxelUpdate();
					removeVoxel.x = (int) hitInfo.hitVoxelPosition.x;
					removeVoxel.y = (int) hitInfo.hitVoxelPosition.y;
					removeVoxel.z = (int) hitInfo.hitVoxelPosition.z;
					removeVoxel.data = BlockSelectorGui.instance.constructNewCurrentSelected();

					CubicWorld.getClient().client.sendPacket(removeVoxel);
				}
				break;
		}
	}

	@Override
	public void mouseButtonReleased(int buttonId)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(Vector2 difference, Vector2 absolute)
	{
		PerspectiveCamera camera = CubicWorld.getClient().cam;

		Vector3 left = new Vector3(camera.direction);
		left.crs(camera.up).nor();

		int differenceX = 0;
		int differenceY = 0;
		int screenX = (int) absolute.x;
		int screenY = (int) absolute.y;

		if (this.lastMouseX != -1)
		{
			// Init
			// Moved?
			differenceX = (int) (-1.0f * ((float) screenX - (float) this.lastMouseX) * this.degPerPixel);
			differenceY = (int) (-1.0f * ((float) screenY - (float) this.lastMouseY) * this.degPerPixel);
		}

		this.lastMouseY = (int) absolute.y;
		this.lastMouseX = (int) absolute.x;

		camera.rotate(Vector3.Y, differenceX);
		camera.rotate(left, differenceY);

		camera.up.set(0, 1, 0);
		camera.update(true);
	}

}
