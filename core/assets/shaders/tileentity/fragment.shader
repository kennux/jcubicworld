uniform sampler2D r_textureAtlas;

varying float v_light;
varying vec2 v_uv;

void main()
{
	vec4 fragColor = texture2D(r_textureAtlas, v_uv);
	gl_FragColor = fragColor * vec4(v_light, v_light, v_light, 1.0);
}