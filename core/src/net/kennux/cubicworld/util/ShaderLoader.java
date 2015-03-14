package net.kennux.cubicworld.util;

import java.util.HashMap;

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
	private static HashMap<String, ShaderProgram> shaderCache = new HashMap<String, ShaderProgram>();
	
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
		// Check if shader already cached
		if (shaderCache.containsKey(name))
			return shaderCache.get(name);
		
		ShaderProgram shader = new ShaderProgram(Gdx.files.internal("shaders/" + name + "/vertex.shader").readString(), Gdx.files.internal("shaders/" + name + "/fragment.shader").readString());

		if (!shader.isCompiled())
		{
			System.out.println("Couldn't compile shader: " + name);
			System.out.println("Log: " + shader.getLog());

			System.exit(-1);
		}
		
		// Add to cache
		shaderCache.put(name, shader);

		return shader;
	}
}
