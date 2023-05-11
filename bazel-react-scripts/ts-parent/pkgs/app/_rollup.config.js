import pluginNodeResolve from "@rollup/plugin-node-resolve";
import serve from "rollup-plugin-serve";
import livereload from "rollup-plugin-livereload";
import babel from '@rollup/plugin-babel';
import { nodeResolve } from '@rollup/plugin-node-resolve';
import commonjs from '@rollup/plugin-commonjs';
import replace from '@rollup/plugin-replace';
import css from "rollup-plugin-import-css";

/** @type {import("rollup").RollupOptions} */
const config = {
    output: {
        format: "commonjs",
    },
    external: [/\.css$/],
    plugins: [

        pluginNodeResolve(), //
        nodeResolve({
          extensions: [".js",".css"],
        }),
        replace({
          'process.env.NODE_ENV': JSON.stringify( 'development' )
        }),
        babel({
          presets: ["./bazel-out/k8-fastbuild/bin/ts-parent/node_modules/@babel/preset-react"],
        }),
        css(),
        commonjs(),
        serve({
          open: true,
          verbose: true,
          contentBase: ["", "public"],
          host: "localhost",
          port: 3000,
        }),
        livereload({ watch: "dist" }),
    ]
};

export default config;
