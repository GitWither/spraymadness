#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec2 UV2;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec2 texCoord0;
out vec2 texCoord2;
out vec4 normal;
out vec4 clipPosition;
out vec4 viewSpacePosition;
out mat4 inverseViewProjection;

void main() {
    inverseViewProjection = inverse(ProjMat * ModelViewMat);
    viewSpacePosition = gl_Position;
    gl_Position = ProjMat * inverse(ModelViewMat) * vec4(Position, 1.0);

    clipPosition = gl_Position;
    vertexColor = Color;
    texCoord0 = UV0;
    texCoord2 = UV2;
}
