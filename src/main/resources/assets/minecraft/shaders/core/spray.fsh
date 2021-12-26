#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord2;
in vec4 normal;
in vec3 clipPosition;
in vec4 viewSpacePosition;
in mat4 inverseViewProjection;

out vec4 fragColor;

void main() {


    fragColor = texture2D(Sampler0, texCoord0);

    //vec4 color = texture(Sampler0, texCoord0) * vertexColor;
    //fragColor = color * ColorModulator;
}
