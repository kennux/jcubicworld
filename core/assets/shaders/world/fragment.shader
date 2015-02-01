varying vec2 v_texCoords;
varying vec4 v_LightCol;

uniform sampler2D r_textureAtlas;
uniform int m_baseLightLevel;

#define MAX_LIGHT_LEVEL 15

void main()
{
	int lightLevel = m_baseLightLevel;
	vec4 fragColor = texture2D(r_textureAtlas, v_texCoords);
	
	// Alpha testing
	if(fragColor.a < 0.1) { discard; }
	
	fragColor.rgb = fragColor.rgb * v_LightCol.rgb;
    gl_FragColor = fragColor;
}