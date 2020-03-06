void main() {
  geomTexCoord = vertTexCoord;
  geomParticleN = vertParticleN;
  gl_Position = vec4(vert*2, 1);
}
