package net.kennux.cubicworld.voxel.handlers.mesh;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.BufferUtils;

public class VoxelMesh
{
	private int vboId;
	private ByteBuffer dataBuffer;
	private VertexAttribute[] attributes;
	
	public VoxelMesh(int maximumVertices, VertexAttribute... attributes)
	{
		this.dataBuffer = BufferUtils.newByteBuffer(maximumVertices * 4 *1);
		this.initVbo();
	}
	
	private void initVbo()
	{
		IntBuffer vboIdBuffer = BufferUtils.newIntBuffer(1);
		Gdx.gl20.glGenBuffers(1, vboIdBuffer);
		this.vboId = vboIdBuffer.get(0);
	}
	
	private void bind(ShaderProgram shader)
	{
		
	}
	
	private float[] setVertices()
	{
	}
}
