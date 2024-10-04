# Bazel setup
Currently contains only typescript 


## Requires
* bazelisk - https://github.com/bazelbuild/bazelisk
* pnpm - https://pnpm.io/installation


# Building
* `bazel clean --expunge` - wipe any bazel cache
* `bazel build ...` - builds all projects 
* `bazel query //typescript-projects/packages/... union //typescript-projects/apps/...` - query for build jobs


# Running applications
* `bazel-setup/typescript-projects`  
* `pnpm install`
* `pnpm start`