package net.kennux.cubicworld.util;

import net.kennux.cubicworld.voxel.VoxelEngine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * This class contains some static functions for generating commonly used mesh types like block meshes.
 * This can be used for example to create tile entity meshes.
 * 
 * @author KennuX
 *
 */
public class MeshUtil
{
	private static final Vector3[] LEFT_SIDE_VERTICES = new Vector3[] { new Vector3(-0.5f, -0.5f, 0.5f), new Vector3(-0.5f, -0.5f, -0.5f), new Vector3(-0.5f, 0.5f, -0.5f), new Vector3(-0.5f, 0.5f, 0.5f), };

	private static final Vector3[] RIGHT_SIDE_VERTICES = new Vector3[] { new Vector3(0.5f, -0.5f, -0.5f), new Vector3(0.5f, -0.5f, 0.5f), new Vector3(0.5f, 0.5f, 0.5f), new Vector3(0.5f, 0.5f, -0.5f), };

	private static final Vector3[] TOP_SIDE_VERTICES = new Vector3[] { new Vector3(-0.5f, 0.5f, -0.5f), new Vector3(0.5f, 0.5f, -0.5f), new Vector3(0.5f, 0.5f, 0.5f), new Vector3(-0.5f, 0.5f, 0.5f), };

	private static final Vector3[] BOTTOM_SIDE_VERTICES = new Vector3[] { new Vector3(-0.5f, -0.5f, -0.5f), new Vector3(0.5f, -0.5f, -0.5f), new Vector3(0.5f, -0.5f, 0.5f), new Vector3(-0.5f, -0.5f, 0.5f), };

	private static final Vector3[] BACK_SIDE_VERTICES = new Vector3[] { new Vector3(-0.5f, -0.5f, -0.5f), new Vector3(0.5f, -0.5f, -0.5f), new Vector3(0.5f, 0.5f, -0.5f), new Vector3(-0.5f, 0.5f, -0.5f), };

	private static final Vector3[] FRON_SIDE_VERTICES = new Vector3[] { new Vector3(0.5f, -0.5f, 0.5f), new Vector3(-0.5f, -0.5f, 0.5f), new Vector3(-0.5f, 0.5f, 0.5f), new Vector3(0.5f, 0.5f, 0.5f), };

	/**
	 * Builds a block mesh.
	 * 
	 * @param topTexture
	 * @param bottomTexture
	 * @param leftTexture
	 * @param rightTexture
	 * @param frontTexture
	 * @param backTexture
	 * @return
	 */
	public static Mesh buildBlockMesh(int topTexture, int bottomTexture, int leftTexture, int rightTexture, int frontTexture, int backTexture)
	{
		MeshBuilder builder = new MeshBuilder();

		builder.begin(Usage.Position | Usage.TextureCoordinates, GL20.GL_TRIANGLES);

		// Prepare vertex data
		Vector3[][] verticesArray = new Vector3[][] { LEFT_SIDE_VERTICES, RIGHT_SIDE_VERTICES, TOP_SIDE_VERTICES, BOTTOM_SIDE_VERTICES, BACK_SIDE_VERTICES, FRON_SIDE_VERTICES };
		Vector2[][] uvsArray = new Vector2[][] { VoxelEngine.getUvForTexture(leftTexture), VoxelEngine.getUvForTexture(rightTexture), VoxelEngine.getUvForTexture(topTexture), VoxelEngine.getUvForTexture(bottomTexture), VoxelEngine.getUvForTexture(backTexture), VoxelEngine.getUvForTexture(frontTexture) };

		// Build vertex info
		for (int i = 0; i < verticesArray.length; i++)
		{
			Vector3[] vertices = verticesArray[i];
			Vector2[] uvs = uvsArray[i];

			// Build vertex infos
			VertexInfo[] vertexInfos = new VertexInfo[vertices.length];
			for (int j = 0; j < vertexInfos.length; j++)
			{
				vertexInfos[j] = new VertexInfo();
				vertexInfos[j].setPos(vertices[j]);
				vertexInfos[j].setUV(uvs[j]);
			}

			// Add mesh rect
			builder.rect(vertexInfos[0], vertexInfos[1], vertexInfos[2], vertexInfos[3]);
		}

		return builder.end();
	}
}
