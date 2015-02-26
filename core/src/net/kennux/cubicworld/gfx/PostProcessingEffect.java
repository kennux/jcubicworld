package net.kennux.cubicworld.gfx;

import net.kennux.cubicworld.util.ShaderLoader;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * <pre>
 * TODO: This class is not finished yet!
 * 
 * Post-processing effect implementation. Gets the shaders from the assets
 * folder. Structure: assets/shaders/postprocess/[effect name]/vertex.shader
 * assets/shaders/postprocess/[effect name]/fragment.shader
 * 
 * Shader attribute names:
 * 
 * Shader uniform names:
 * 
 * </pre>
 * 
 * @author KennuX
 *
 */
public class PostProcessingEffect
{
	private ShaderProgram shader;

	public PostProcessingEffect(String effectName)
	{
		this.shader = ShaderLoader.loadShader("postprocess/test");
	}

	public ShaderProgram getShader()
	{
		return shader;
	}

	public void setShader(ShaderProgram shader)
	{
		this.shader = shader;
	}
}
