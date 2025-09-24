import { VersionInfoApi } from "@dxs-ts/envir-util";
export const version = '2.0.23';export const build_time = '09/24/2025, 14:17:20';
export const renderVersion = () => VersionInfoApi.builder()
          .setLogo('logo_1_great_ones_wisdom')
          .setTheme('red')
          .setProjectInfo('@dxs-ts/gamut', '2.0.23', '09/24/2025, 14:17:20')
          .addInternalComponents(['@dxs-ts/envir-util','@dxs-ts/gamut-api','@dxs-ts/gamut-md','@dxs-ts/gamut-primitives','@dxs-ts/gamut-form-review','@dxs-ts/gamut-form','@dxs-ts/gamut-shell','@dxs-ts/gamut-intl','@dxs-ts/gamut-routes','@dxs-ts/gamut-theme'])
          .addExternalComponents(['@mui/icons-material@6.5.0','@mui/material@6.5.0','@mui/system@6.5.0','@mui/utils@6.4.9','@tanstack/react-query@5.83.0','@tanstack/react-router@1.130.2','leaflet-geosearch@4.2.0','luxon@3.7.1','numbro@2.5.0','react@18.3.1','react-date-picker@11.0.0','react-intl@6.8.9','react-leaflet@4.2.1','react-markdown@10.1.0','react-time-picker@7.0.0','ts-md5@1.3.1'])
          .render();