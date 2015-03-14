attribute vec4 a_position;
attribute vec2 a_texCoord0;

uniform mat4 m_cameraProj;
uniform mat4 m_transform;

uniform float m_light;

varying float v_light;
varying vec2 v_uv;

void main()
{
	v_uv = a_texCoord0;
	v_light = m_light;
    gl_Position = m_cameraProj * m_transform * a_position;
}