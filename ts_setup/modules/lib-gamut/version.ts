import { VersionInfoApi } from "@dxs-ts/envir-util";
export const version = '2.0.19';export const build_time = '08/19/2025, 13:53:37';
export const renderVersion = () => VersionInfoApi.builder()
          .setLogo('logo_1_great_ones_wisdom')
          .setTheme('red')
          .setProjectInfo('@dxs-ts/gamut', '2.0.19', '08/19/2025, 13:53:37')
          .addInternalComponents(['@dxs-ts/gamut-theme','@dxs-ts/gamut-shell','@dxs-ts/gamut-routes','@dxs-ts/gamut-primitives','@dxs-ts/gamut-md','@dxs-ts/gamut-intl','@dxs-ts/gamut-form-review','@dxs-ts/gamut-form','@dxs-ts/gamut-api','@dxs-ts/envir-util'])
          .addExternalComponents(['@mui/icons-material@6.5.0','@mui/material@6.5.0','@mui/system@6.5.0','@mui/utils@6.4.9','@tanstack/react-query@5.83.0','@tanstack/react-router@1.130.2','leaflet-geosearch@4.2.0','luxon@3.7.1','numbro@2.5.0','react@18.3.1','react-date-picker@11.0.0','react-intl@6.8.9','react-leaflet@4.2.1','react-markdown@10.1.0','react-time-picker@7.0.0','ts-md5@1.3.1'])
          .render();