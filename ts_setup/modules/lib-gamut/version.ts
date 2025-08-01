import { VersionInfoApi } from "@dxs-ts/envir-util";

export const version = '1.0.247';export const build_time = '10/07/2025 07:06:54';


export function renderVersion() {
  VersionInfoApi.builder()
    .setLogo('logo_1_great_ones_wisdom')
    .setTheme('red')
    .setProjectInfo('@dxs-ts/gamut', version, new Date())
    .addInternalComponents(['@dxs-ts/core'])
    .addExternalComponents(['react', 'typescript'])
    .render();
}