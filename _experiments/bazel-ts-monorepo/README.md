# bazel-ts-monorepo

This repo demonstrates the basic usage of [Bazel](https://bazel.build/) with TypeScript.

## Getting started

Prerequisites: Node.js, Yarn

```sh
$ yarn install  # install bazel & other dependencies
$ yarn build    # build everything
$ yarn test     # test everything
$ yarn start    # run the app
```

## Libraries used

-   [TypeScript](https://www.typescriptlang.org/) for transpiling
-   [Rollup](https://rollupjs.org/) for bundling
-   [Terser](https://terser.org/) for minifying
-   [Jest](https://jestjs.io/) for testing

## Project structure

```
├── pkgs/
│   ├── app/
│   │       (Console application, depends on `common`)
│   └── common/
│           (Utility library)
├── tools/
│           (Starlark marcros, config files for tools)
├── package.json
│           (Nodejs manifest file, list project & build tool dependencies)
└── WORKSPACE.bazel
            (Bazel workspace file)
```
