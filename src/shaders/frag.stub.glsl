#version 410

// default uniforms from GGGV

// futher sources are set as tex1, tex2, tex3
uniform sampler2D tex0;
uniform sampler2D tex1;
uniform sampler2D tex2;
uniform sampler2D tex3;
uniform sampler2D tex4;
uniform sampler2D tex5;
uniform sampler2D tex6;
uniform sampler2D tex7;
uniform sampler2D tex8;
uniform sampler2D tex9;
uniform sampler2D tex10;
uniform sampler2D tex11;
uniform sampler2D tex12;
uniform sampler2D tex13;
uniform sampler2D tex14;
uniform sampler2D tex15;
uniform sampler2D tex16;
uniform sampler2D tex17;
uniform sampler2D tex18;
uniform sampler2D tex19;
uniform float hasTex0;
uniform float hasTex1;
uniform float hasTex2;
uniform float hasTex3;
uniform float hasTex4;
uniform float hasTex5;
uniform float hasTex6;
uniform float hasTex7;
uniform float hasTex8;
uniform float hasTex9;
uniform float hasTex10;
uniform float hasTex11;
uniform float hasTex12;
uniform float hasTex13;
uniform float hasTex14;
uniform float hasTex15;
uniform float hasTex16;
uniform float hasTex17;
uniform float hasTex18;
uniform float hasTex19;

// result of the last render of this shader
uniform sampler2D lastFrame;

// only set when tex* is a FFsource
uniform float tex0Width;
uniform float tex0Height;

// seconds since application start.
// monotonically increasing, will never be 0
uniform float time;
// time the last frame took to render
uniform float renderTime;
// number of frames in the last second
uniform float fps;
// output window size
uniform float windowHeight;
uniform float windowWidth;
// cursor position
uniform float cursorX;
uniform float cursorY;

// texCoord
in vec2 fragTexCoord;
in vec2 screenCoord;
in float particleN;

ivec2 iftc = ivec2(fragTexCoord * vec2(windowWidth, windowHeight));

// output pixel color
layout(location = 0) out vec4 outputColor;

#define PI 3.1415926535897932384626433832795

vec2 screenToPolar(vec2 screenCoords) {
  screenCoords -= vec2(0.5, 0.5);
  screenCoords *= vec2(windowWidth, windowHeight);
  return vec2(length(screenCoords), atan(screenCoords.y, screenCoords.x));
}

vec2 polarToScreen(vec2 polarCoords) {
  return vec2(0.5, 0.5) + vec2(polarCoords.x * cos(polarCoords.y),
                               polarCoords.x * sin(polarCoords.y)) /
                              vec2(windowWidth, windowHeight);
}

vec2 cToP(vec2 coord) {
  return vec2(length(coord), atan(coord.y, coord.x));
}

vec2 pToC(vec2 coord) {
  return vec2(cos(coord.y) * coord.x, sin(coord.y) * coord.x);
}

float noise1(vec2 st) {
  return fract(sin(dot(st.xy, vec2(12.9898, 78.233)) * 43758.5453123));
}

vec2 noise2(vec2 st) {
  return fract(sin(st.xy * vec2(12.9898, 78.233)) * 43758.5453123);
}
vec3 noise3(vec2 st, float t) {
  return fract(sin(vec3(st.xy, 12.3721 * st.x + 2736.1272 * st.y + t) *
                   vec3(12.9898, 78.233, 81.27655)) *
               43758.5453123);
}
/*
vec2 noise2(vec2 coord, float seed) {
  return fract(1264.2135 * sin((coord + seed) * 17237.28348));
}*/

vec2 ftc = fragTexCoord;

vec2 v4ToV2(vec4 v) {
  vec2 high_bits = v.rg;
  vec2 low_bits = v.ba;
  vec2 c = high_bits * 255. + low_bits;
  c /= 255.;
  c -= 0.5;
  c *= 16;
  return c;
}

vec4 v2ToV4(vec2 v) {
  v /= 16;
  v += 0.5;
  v *= 255.;
  vec2 low_bits = fract(v);
  vec2 high_bits = round(v - low_bits) / 255.;
  return vec4(high_bits, low_bits);
}

vec3 rgb2hsv(vec3 c) {
  vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
  vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
  vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

  float d = q.x - min(q.w, q.y);
  float e = 1.0e-10;
  return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c) {
  vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
  vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
  return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}
