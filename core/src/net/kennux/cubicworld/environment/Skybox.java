package net.kennux.cubicworld.environment;

import net.kennux.cubicworld.util.ShaderLoader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * <pre>
 * Simple skybox implementation.
 * This class renders a skybox constructed from 6 given textures.
 * 
 * TODO Cleaner re-implementation and sun / moon and stars support
 * </pre>
 * 
 * @author KennuX
 *
 */
public class Skybox implements Disposable
{
	/**
	 * <pre>
	 * The pixel maps of the 6 side textures.
	 * 0 = right
	 * 1 = left
	 * 2 = up
	 * 3 = down
	 * 4 = front
	 * 5 = back
	 * </pre>
	 * 
	 */
	protected final Pixmap[] data = new Pixmap[6];

	/**
	 * The skybox shader, loaded by ShaderLoader.loadShader("skybox").
	 */
	protected ShaderProgram shader;

	/**
	 * The uniform location of the world translation matrix.
	 */
	protected int u_worldTrans;

	/**
	 * The skybox quad constructed in the constructor.
	 */
	protected Mesh quad;

	private Matrix4 worldTrans;

	private Matrix4 fakeCam;

	/**
	 * Constructs the skybox from the given filehandles.
	 * 
	 * @param positiveX
	 *            right texture
	 * @param negativeX
	 *            left texture
	 * @param positiveY
	 *            up texture
	 * @param negativeY
	 *            bottom texture
	 * @param positiveZ
	 *            front texture
	 * @param negativeZ
	 *            back texture
	 */
	public Skybox(FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY, FileHandle positiveZ, FileHandle negativeZ)
	{
		this(new Pixmap(positiveX), new Pixmap(negativeX), new Pixmap(positiveY), new Pixmap(negativeY), new Pixmap(positiveZ), new Pixmap(negativeZ));
	}

	/**
	 * Constructs the skybox from the given pixmaps.
	 * 
	 * @param positiveX
	 *            right texture
	 * @param negativeX
	 *            left texture
	 * @param positiveY
	 *            up texture
	 * @param negativeY
	 *            bottom texture
	 * @param positiveZ
	 *            front texture
	 * @param negativeZ
	 *            back texture
	 */
	public Skybox(Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ)
	{
		// Load shader
		this.shader = ShaderLoader.loadShader("skybox");

		data[0] = positiveX;
		data[1] = negativeX;

		data[2] = positiveY;
		data[3] = negativeY;

		data[4] = positiveZ;
		data[5] = negativeZ;

		if (!shader.isCompiled())
			throw new GdxRuntimeException(shader.getLog());

		u_worldTrans = shader.getUniformLocation("u_worldTrans");

		quad = this.createQuad();
		worldTrans = new Matrix4();
		fakeCam = new Matrix4();
		fakeCam.setTranslation(0, 0, -1f);

		// bind cubemap
		Gdx.gl20.glBindTexture(GL20.GL_TEXTURE_CUBE_MAP, 0);
		Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL20.GL_RGB, data[0].getWidth(), data[0].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[0].getPixels());
		Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL20.GL_RGB, data[1].getWidth(), data[1].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[1].getPixels());

		Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL20.GL_RGB, data[2].getWidth(), data[2].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[2].getPixels());
		Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL20.GL_RGB, data[3].getWidth(), data[3].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[3].getPixels());

		Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL20.GL_RGB, data[4].getWidth(), data[4].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[4].getPixels());
		Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL20.GL_RGB, data[5].getWidth(), data[5].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[5].getPixels());

		Gdx.gl20.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR_MIPMAP_LINEAR);
		Gdx.gl20.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);
		Gdx.gl20.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
		Gdx.gl20.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);
	}

	/**
	 * Creates a simple quad mesh.
	 * 
	 * @return
	 */
	public Mesh createQuad()
	{
		Mesh mesh = new Mesh(true, 4, 6, VertexAttribute.Position(), VertexAttribute.ColorUnpacked(), VertexAttribute.TexCoords(0));
		mesh.setVertices(new float[] { -1f, -1f, 0, 1, 1, 1, 1, 0, 1, 1f, -1f, 0, 1, 1, 1, 1, 1, 1, 1f, 1f, 0, 1, 1, 1, 1, 1, 0, -1f, 1f, 0, 1, 1, 1, 1, 0, 0 });
		mesh.setIndices(new short[] { 0, 1, 2, 2, 3, 0 });
		return mesh;
	}

	@Override
	public void dispose()
	{
		shader.dispose();
		quad.dispose();
		for (int i = 0; i < 6; i++)
			data[i].dispose();
	}

	/**
	 * Renders the skybox for the given quaternion camera rotation.
	 * 
	 * @param quaternion
	 *            The rotation of the camera used to render the world.
	 */
	public void render(Quaternion quaternion)
	{
		worldTrans.idt();
		worldTrans.rotate(quaternion);

		shader.begin();
		shader.setUniformMatrix(u_worldTrans, worldTrans.cpy().mul(fakeCam));

		Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE_CUBE_MAP);

		quad.render(shader, GL20.GL_TRIANGLES);
		shader.end();
	}
}
