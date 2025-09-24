

export * from './version-info'

// Example usage:
/*
import  { VersionInfoApi } from './version-info'
const builder = VersionInfoApi.builder()
  .setLogo('logo_1_great_ones_wisdom')
  .setTheme('purple')
  .setProjectInfo('@dxs-ts/gamut', '2.1.0', new Date())
  .addInternalComponents(['@dxs-ts/core', '@dxs-ts/utils', '@dxs-ts/validation'])
  .addExternalComponents(['typescript', 'vite', 'vitest', 'eslint']);

console.log('🔮 BUILDER PATTERN ASCII ART:');
builder.render();

console.log('\n🔥 RED THEME EXAMPLE:');
VersionInfoApi.builder()
  .setLogo('logo_1_great_ones_wisdom')
  .setTheme('red')
  .setProjectInfo('@dxs-ts/gamut', '2.1.0', new Date())
  .addInternalComponents(['@dxs-ts/core'])
  .addExternalComponents(['react', 'typescript'])
  .render();
*/