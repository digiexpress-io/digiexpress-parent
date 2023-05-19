"""Tools used internal for this repository.
"""

load("jest.bzl", _jest_test = "jest_test")
load("terser.bzl", _terser_minified = "terser_minified")
load("typescript.bzl", _ts_project = "ts_project")

jest_test = _jest_test
ts_project = _ts_project
terser_minified = _terser_minified
