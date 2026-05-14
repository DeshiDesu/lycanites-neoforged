#version 150
uniform sampler2D Sampler0;
uniform sampler2D Sampler2;

uniform vec2 OverlayUv;
uniform vec2 LightUv;

in vec2 vTexCoord;
in vec3 vNormalVS;
in vec4 vColor;
in vec3 vViewDir;
in float vertexDistance;

out vec4 fragColor;

uniform float AlphaCutoff;

uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

uniform vec3 BaseColor;
uniform float BaseRange;

uniform vec3 VariantColor;
uniform float VariantAmount;

uniform vec3 SunDirectionVS;
uniform vec3 SpecularColor;
uniform float SpecularIntensity;
uniform float SpecularPower;

uniform float FearRedGlow;

void main() {
    vec4 tex = texture(Sampler0, vTexCoord);
    if (AlphaCutoff > 0.0 && tex.a <= AlphaCutoff) {
        discard;
    }

    // Variant recolor band
    vec3 base = tex.rgb;
    float dist = distance(base, BaseColor);
    float influence = BaseRange > 0.0 ? clamp(1.0 - dist / BaseRange, 0.0, 1.0) : 0.0;
    float amount = VariantAmount * influence;
    vec3 recolored = mix(base, VariantColor, amount);

    // Single lightmap sample at combined block+sky UV — vanilla approach.
    // Sampler2 is the 16x16 lightmap; block light = U axis, sky light = V axis.
    const float TEX = 1.0 / 16.0;
    vec2 uvCoord = clamp(LightUv / 256.0, vec2(0.5 * TEX), vec2(15.5 * TEX));
    vec3 lightCol = texture(Sampler2, uvCoord).rgb;

    // Baked range compression (lightRangeScale = 0.95): 0.5 + (x - 0.5) * 0.95
    lightCol = vec3(0.025) + lightCol * 0.95;

    // Matte lightmap-based lighting (no directional sun shading).
    // SunDirectionVS, SpecularColor, SpecularIntensity, SpecularPower, vNormalVS,
    // and vViewDir are still available as uniforms/varyings for resource pack
    // shader overrides that want to implement custom material lighting.

    float minBrightness = 0.15;
    lightCol = max(lightCol, vec3(minBrightness));

    vec3 lit = recolored * lightCol;

    // Fear red glow: red-dominant pixels bypass lighting and become emissive.
    // Detects texels where red channel significantly exceeds green and blue,
    // making creature eyes/markings glow through the fear darkness.
    if (FearRedGlow > 0.0) {
        float redExcess = recolored.r - max(recolored.g, recolored.b);
        float isRed = smoothstep(0.12, 0.35, redExcess) * smoothstep(0.25, 0.45, recolored.r);
        vec3 emissive = recolored * 1.8;
        lit = mix(lit, emissive, isRed * FearRedGlow);
    }

    vec3 finalRgb = lit;

    // Damage red flash: OverlayUv.y == 0 means full red, 10 means no red
    float hurtIntensity = clamp(1.0 - OverlayUv.y / 10.0, 0.0, 1.0);
    if (hurtIntensity > 0.0) {
        finalRgb = mix(finalRgb, vec3(1.0, 0.0, 0.0), hurtIntensity * 0.3);
    }

    // Baked highlight rolloff (highlightCompression = 0.35).
    finalRgb = finalRgb / (1.0 + finalRgb * 0.35);

    vec4 preFogColor = vec4(finalRgb, tex.a) * vColor;

    // Fog: blend toward FogColor based on vertex distance.
    // Matches vanilla linear_fog(): smoothstep between FogStart and FogEnd,
    // scaled by FogColor.a (0 = no fog active, 1 = full fog).
    // Covers all fog types: distance, underwater, lava, blindness, void, powder snow, etc.
    if (FogColor.a > 0.0) {
        float fogFactor = vertexDistance <= FogStart ? 0.0
                        : vertexDistance >= FogEnd   ? 1.0
                        : smoothstep(FogStart, FogEnd, vertexDistance);
        fragColor = vec4(mix(preFogColor.rgb, FogColor.rgb, fogFactor * FogColor.a), preFogColor.a);
    } else {
        fragColor = preFogColor;
    }
}
