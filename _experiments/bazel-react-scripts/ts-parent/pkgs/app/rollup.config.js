import resolve from "@rollup/plugin-node-resolve";
import commonjs from '@rollup/plugin-commonjs';

import peerDepsExternal from 'rollup-plugin-peer-deps-external';

import image from '@rollup/plugin-image';
import replace from '@rollup/plugin-replace';

// To handle css files
import postcss from "rollup-plugin-postcss";
/** @type {import("rollup").RollupOptions} */
const config = [{
  input: "src/index.js",
  external: [/\.css$/], // telling rollup anything that is .css aren't part of type exports
  plugins: [
    replace({
      'process.env.NODE_ENV': JSON.stringify('development')
    }),
    peerDepsExternal(),
    resolve({
      jsnext: true,
      main: true,
      browser: true,
    }),

    commonjs(),
    postcss(),
    image(),

/*    svg({
      // process SVG to DOM Node or String. Default: false
      stringify: false
    }),
*/    
  ],
},
];

export default config;
