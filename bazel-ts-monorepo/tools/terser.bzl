"""Wrapper for minifying with terser
"""

load("@npm//@bazel/terser:index.bzl", _terser_minified = "terser_minified")

def terser_minified(name, src):
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
