load("@build_bazel_rules_nodejs//:index.bzl", "js_library", "pkg_npm")
load("//tools/yarn:index.bzl", "ts_project")

SRCS = glob(
    ["src/index.tsx"] + ["src/**/*.ts"] + ["src/**/*.tsx"],
    exclude = ["src/**/*.spec.ts"],
)

# Compile to JS
ts_project(
    name = "src",
    srcs = SRCS,
    tsconfig = "tsconfig.json",
)

# Export library to be consumed
js_library(
    name = "lib",
    package_name = "@digiexpress/digiexpress-composer-ui",
    srcs = ["package.json"],
    visibility = ["//:__subpackages__"],
    deps = [":src"],
)

# Create NPM Package
pkg_npm(
    name = "pkg",
    package_name = "@digiexpress/digiexpress-composer-ui",
    srcs = ["package.json"],
    tgz = "digiexpress-composer-ui.tgz",
    deps = [":src"],
)