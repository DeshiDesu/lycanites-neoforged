#version 150
uniform sampler2D Sampler0;

uniform vec2 OverlayUv;
uniform vec2 LightUv;

in vec2 vTexCoord;
in vec3 vNormalVS;
in vec4 vColor;
in vec3 vViewDir;

out vec4 fragColor;

uniform float AlphaCutoff;

uniform vec3 BaseColor;
uniform float BaseRange;

uniform vec3 VariantColor;
uniform float VariantAmount;

uniform vec3 SunDirectionVS;
uniform vec3 SpecularColor;
uniform float SpecularIntensity;
uniform float SpecularPower;

void main() {
    vec4 tex = texture(Sampler0, vTexCoord);
    if (AlphaCutoff > 0.0 && tex.a <= AlphaCutoff) {
        discard;
    }

    vec3 N = normalize(vNormalVS);
    vec3 L = normalize(vec3(0.0, 0.0, 1.0));
    float ndotl = max(dot(N, L), 0.0);

    vec3 base = tex.rgb;

    float dist = distance(base, BaseColor);
    float influence = BaseRange > 0.0 ? clamp(1.0 - dist / BaseRange, 0.0, 1.0) : 0.0;

    float amount = VariantAmount * influence;
    vec3 recolored = mix(base, VariantColor, amount);

    float block = LightUv.x / 240.0;
    float sky = LightUv.y / 240.0;
    float lightFactor = clamp(max(block, sky), 0.0, 1.0);

    vec3 lit = mix(recolored * 0.5, recolored, ndotl) * lightFactor;

    vec3 sunL = normalize(SunDirectionVS);
    vec3 V = normalize(vViewDir);
    vec3 H = normalize(sunL + V);
    float ndoth = max(dot(N, H), 0.0);
    float spec = pow(ndoth, SpecularPower) * SpecularIntensity;
    vec3 specular = SpecularColor * spec * lightFactor;

    vec3 finalRgb = lit + specular;

    fragColor = vec4(finalRgb, tex.a) * vColor;
}
