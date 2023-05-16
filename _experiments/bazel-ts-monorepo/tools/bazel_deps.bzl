"""Third-party dependencies fetched by Bazel.
Unlike WORKSPACE, the content of this file is unordered.
Keep them separate to make the WORKSPACE file more maintainable.
"""

# Install the nodejs "bootstrap" package
# This provides the basic tools for running and packaging nodejs programs in Bazel

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

def fetch_build_bazel_rules_nodejs():
    http_archive(
        name = "build_bazel_rules_nodejs",
        sha256 = "f10a3a12894fc3c9bf578ee5a5691769f6805c4be84359681a785a0c12e8d2b6",
        urls = ["https://github.com/bazelbuild/rules_nodejs/releases/download/5.5.3/rules_nodejs-5.5.3.tar.gz"],
    )
