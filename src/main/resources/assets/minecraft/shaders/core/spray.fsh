#version 150
#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 normal;

out vec4 fragColor;

void clip(vec2 texCoord0) {
    if (texCoord0.x < 0.0 || texCoord0.y < 0.0 || texCoord0.x > 1.0 || texCoord0.y > 1.0) {
        discard;
    }
}

void main() {
    clip(texCoord0);

    vec4 color = texture(Sampler0, clamp(texCoord0, 0.0, 1.0));
    color *= vertexColor * ColorModulator;

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
