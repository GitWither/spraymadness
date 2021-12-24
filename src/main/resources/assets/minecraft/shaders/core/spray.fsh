#version 150

uniform sampler2D DiffuseDepthSampler;
uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord2;
in vec4 normal;
in vec4 clipPosition;
in vec4 viewSpacePosition;
in mat4 inverseViewProjection;

out vec4 fragColor;

vec3 world_position_from_depth(vec2 screen_pos, float ndc_depth)
{
    // Remap depth to [-1.0, 1.0] range.
    float depth = ndc_depth * 2.0 - 1.0;

    // // Create NDC position.
    vec4 ndc_pos = vec4(screen_pos, depth, 1.0);

    // Transform back into world position.
    vec4 world_pos = inverseViewProjection * ndc_pos;

    // Undo projection.
    world_pos = world_pos / world_pos.w;

    return world_pos.xyz;
}

void main() {
    vec2 screen_pos = clipPosition.xy / clipPosition.w;
    vec2 tex_coords = screen_pos * 0.5 + 0.5;

    float depth     = texture(DiffuseDepthSampler, tex_coords).x;
    vec3  world_pos = world_position_from_depth(screen_pos, depth);

    vec4 ndc_pos = viewSpacePosition * vec4(world_pos, 1.0);
    ndc_pos.xyz /= ndc_pos.w;

    ndc_pos.xy *= (16 / 9.0);

    if (ndc_pos.x < -1.0 || ndc_pos.x > 1.0 || ndc_pos.y < -1.0 || ndc_pos.y > 1.0)
    discard;

    vec2 decal_tex_coord = ndc_pos.xy * 0.5 + 0.5;
    decal_tex_coord.x    = 1.0 - decal_tex_coord.x;

    vec4 albedo = texture(Sampler0, decal_tex_coord);

    if (albedo.a < 0.1)
    discard;


    fragColor = albedo;


    //vec4 color = texture(Sampler0, texCoord0) * vertexColor;
    //fragColor = color * ColorModulator;
}
