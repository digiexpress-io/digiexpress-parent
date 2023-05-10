"""Wrapper for compiling with typescript
"""
load("@rules_nodejs//nodejs/private:ts_project.bzl", _ts_project_lib = "ts_project")
load("@npm//@bazel/typescript:index.bzl", _ts_project = "ts_project")

def ts_project(name, srcs, tsconfig, deps = []):
    """A macro around the typescript ts_project rule

    Args:
        name: name
        srcs: srcs
        tsconfig: tsconfig
        deps: deps
    """
    all_deps = ["@npm//tslib"]
    all_deps.extend(deps)

    _ts_project(
        name = name,
        srcs = srcs,

        allow_js = True,
        declaration = True,
        declaration_map = True,
        tsconfig = tsconfig,
        deps = all_deps,
    )
