#version 120

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

// uniform vec2 texcoordOffset;
uniform sampler2D u_texture;

varying vec2 v_texCoords;

void main()
{
	vec2 texCoords = vec2(v_texCoords.x, 1.0 - v_texCoords.y);
	gl_FragColor = texture2D(u_texture, texCoords.xy);
}