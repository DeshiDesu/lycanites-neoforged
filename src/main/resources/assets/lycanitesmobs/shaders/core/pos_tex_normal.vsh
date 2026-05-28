#version 150
in vec3 Position;
in vec2 UV0;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat3 NormalMat;

uniform vec4 ColorModulator;
uniform vec2 UvOffset;

out vec2 vTexCoord;
out vec3 vNormalVS;
out vec4 vColor;
out vec3 vViewDir;

void main() {
    vec4 viewPos = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPos;

    vTexCoord = UV0 + UvOffset;
    vNormalVS = normalize(NormalMat * Normal);
    vColor = ColorModulator;
    vViewDir = normalize(-viewPos.xyz);
}
