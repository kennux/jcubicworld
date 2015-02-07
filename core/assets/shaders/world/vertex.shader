attribute vec4 v_Position;
attribute vec2 v_Uv;
attribute float v_Light;

uniform mat4 m_cameraProj;

varying vec2 v_texCoords;
varying float v_LightCol;

void main()
{
    v_texCoords = v_Uv;
    v_LightCol = v_Light;
    gl_Position = m_cameraProj * v_Position;
}