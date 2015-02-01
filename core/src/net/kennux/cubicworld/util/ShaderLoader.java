package net.kennux.cubicworld.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Simple & easy shader loading utility class.
 * 
 * @author KennuX
 *
 */
public class ShaderLoader
{
	/**
	 * Loads a new shader. Files get load from:
	 * assets/shaders/[shader_name]/vertex.shader
	 * assets/shaders/[shader_name]/fragment.shader
	 * 
	 * @param name
	 * @return
	 */
	public static ShaderProgram loadShader(String name)
	{
		ShaderProgram shader = new ShaderProgram(Gdx.files.internal("shaders/" + name + "/vertex.shader").readString(), Gdx.files.internal("shaders/" + name + "/fragment.shader").readString());

		if (!shader.isCompiled())
		{
			System.out.println("Couldn't compile shader: " + name);
			System.out.println("Log: " + shader.getLog());

			System.exit(-1);
		}

		return shader;
	}
}
