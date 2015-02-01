package net.kennux.cubicworld.voxel;

import com.badlogic.gdx.math.Vector2;

/**
 * <pre>
 * The voxel rendering state holds textures for all voxel sides and their uv coordinates.
 * It is possible to attach multiple VoxelRenderStates to one voxel type with different state ids.
 * </pre>
 * 
 * @author KennuX
 *
 */
public class VoxelRenderState
{
	public int topTexture, bottomTexture, leftTexture, rightTexture, frontTexture, backTexture;
	public Vector2[] topUv, bottomUv, leftUv, rightUv, frontUv, backUv;

	public VoxelRenderState(int topTexture, int bottomTexture, int leftTexture, int rightTexture, int frontTexture, int backTexture)
	{
		this.topTexture = topTexture;
		this.bottomTexture = bottomTexture;
		this.leftTexture = leftTexture;
		this.rightTexture = rightTexture;
		this.frontTexture = frontTexture;
		this.backTexture = backTexture;
	}

	/**
	 * Gets uvs for the given faces.
	 * 
	 * @param face
	 * @return
	 */
	public Vector2[] getUvsForFace(VoxelFace face)
	{
		switch (face)
		{
			case FRONT:
				return this.frontUv;

			case BACK:
				return this.backUv;

			case LEFT:
				return this.leftUv;

			case RIGHT:
				return this.rightUv;

			case TOP:
				return this.topUv;

			case BOTTOM:
				return this.bottomUv;
		}

		return null;
	}

	/**
	 * Initializes the UV-Coordinates for this voxel type. Gets called by the
	 * voxelengine after the bootstrap was successfully executed.
	 */
	public void initializeUvs()
	{
		this.topUv = VoxelEngine.getUvForTexture(topTexture);
		this.bottomUv = VoxelEngine.getUvForTexture(bottomTexture);
		this.leftUv = VoxelEngine.getUvForTexture(leftTexture);
		this.rightUv = VoxelEngine.getUvForTexture(rightTexture);
		this.frontUv = VoxelEngine.getUvForTexture(frontTexture);
		this.backUv = VoxelEngine.getUvForTexture(backTexture);
	}

	/**
	 * Sets the texture for a given face.
	 * 
	 * @param face
	 * @param textureId
	 */
	public VoxelRenderState setTexture(VoxelFace face, int textureId)
	{
		switch (face)
		{
			case FRONT:
				this.frontTexture = textureId;
				break;

			case BACK:
				this.backTexture = textureId;
				break;

			case LEFT:
				this.leftTexture = textureId;
				break;

			case RIGHT:
				this.rightTexture = textureId;
				break;

			case TOP:
				this.topTexture = textureId;
				break;

			case BOTTOM:
				this.bottomTexture = textureId;
				break;
		}

		return this;
	}
}
