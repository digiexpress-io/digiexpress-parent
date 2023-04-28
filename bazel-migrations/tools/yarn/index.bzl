"""Tools used internal for this repository.
"""


load("@npm//@bazel/terser:index.bzl", _ts_project = "ts_project")
load("@npm//@bazel/terser:index.bzl", _terser_minified = "terser_minified")

def _ts_project(name, srcs, tsconfig, deps = []):
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
        declaration = True,
        declaration_map = True,
        tsconfig = tsconfig,
        deps = all_deps,
    )


def _terser_minified(name, src):
    """A macro around the terser terser_minified rule
    Args:
        name: name
        src: src
    """
    _terser_minified(
        name = name,
        src = src,
        config_file = "//tools:terser_config.json",
        sourcemap = False,
    )
ts_project = _ts_project
terser_minified = _terser_minified