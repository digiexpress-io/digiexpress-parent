import pluginNodeResolve from "@rollup/plugin-node-resolve";

/** @type {import("rollup").RollupOptions} */
const config = {
    output: {
        format: "commonjs",
    },
    plugins: [
        pluginNodeResolve(), //
    ],
};

export default config;
