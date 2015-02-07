varying vec2 v_texCoords;
varying float v_LightCol;

uniform sampler2D r_textureAtlas;
uniform int m_baseLightLevel;

#define MAX_LIGHT_LEVEL 15

void main()
{
	int lightLevel = m_baseLightLevel;
	vec4 fragColor = texture2D(r_textureAtlas, v_texCoords);
	
	// Alpha testing
	if(fragColor.a < 0.1) { discard; }
	
	fragColor.rgb = fragColor.rgb * vec3(v_LightCol,v_LightCol,v_LightCol);
    gl_FragColor = fragColor;
}