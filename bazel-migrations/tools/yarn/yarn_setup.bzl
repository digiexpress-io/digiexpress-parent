# The yarn_install rule runs yarn anytime the package.json or yarn.lock file changes.
# It also extracts any Bazel rules distributed in an npm package.
load("@build_bazel_rules_nodejs//:index.bzl", "yarn_install")

def yarn_setup():
    yarn_install(
        name = "npm",  # Name this "npm" so that Bazel Label references look like @npm//package
        package_json = "//ts-parent:package.json",
        quiet = False,
        yarn_lock = "//ts-parent:yarn.lock",
        symlink_node_modules = False,

#        data = [
#            YARN_LABEL,
#            "//:.yarnrc"
#        ],
#        yarn = YARN_LABEL,
    )