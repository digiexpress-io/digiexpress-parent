import { VersionInfoApi } from "@dxs-ts/envir-util";
export const version = '2.0.15';export const build_time = '08/18/2025, 08:19:38';
export const renderVersion = () => VersionInfoApi.builder()
          .setLogo('logo_1_great_ones_wisdom')
          .setTheme('red')
          .setProjectInfo('@dxs-ts/eveli-ide', '2.0.15', '08/18/2025, 08:19:38')
          .addInternalComponents(['@dxs-ts/gamut-api','@dxs-ts/gamut-theme','@dxs-ts/gamut-form','@dxs-ts/gamut-md','@dxs-ts/gamut-shell','@dxs-ts/gamut-form-review','@dxs-ts/gamut-intl','@dxs-ts/envir-fetch','@dxs-ts/envir-util','@dxs-ts/wrench-api','@dxs-ts/wrench-routes','@dxs-ts/stencil-api','@dxs-ts/stencil-routes','@dxs-ts/eveli-api','@dxs-ts/eveli-primitives','@dxs-ts/eveli-intl','@dxs-ts/eveli-routes'])
          .addExternalComponents(['@material-table/core@6.4.4','@monaco-editor/react@4.7.0','@mui/icons-material@6.5.0','@mui/material@6.5.0','@mui/system@6.5.0','@mui/x-tree-view@7.29.1','@tanstack/react-query@5.83.0','@tanstack/react-router@1.130.2','@tanstack/react-table@8.21.3','@uiw/react-md-editor@4.0.8','@xyflow/react@12.8.2','diff2html@3.4.52','elkjs/lib/elk.bundled.js@0.9.3','file-saver@2.0.5','js-file-download@0.4.12','leaflet-geosearch@4.2.0','luxon@3.7.1','moment@2.30.1','monaco-editor@0.52.2','notistack@3.0.2','numbro@2.5.0','react@18.3.1','react-date-picker@11.0.0','react-intl@6.8.9','react-leaflet@4.2.1','react-markdown@10.1.0','react-time-picker@7.0.0','recharts@2.15.4','ts-md5@1.3.1','yaml@2.8.1'])
          .render();