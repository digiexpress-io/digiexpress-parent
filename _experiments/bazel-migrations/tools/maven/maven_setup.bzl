# Setup maven dependencies.
# import a constant that defines all of our project dependencies
load("//tools/maven:maven_deps.bzl", "MAVEN_DEPS")

# This is the tricky part, on initial glance this is straight forward, big difference between maven is that we create a lock file.
# maven_install_json - (generated do not modify) this is our lock file, that defines every dependency we have, if it's not here then it does not exist.
# artifacts - this is the list of artifacts from what we generate the lock file.
# How to add a new dependency? add it into the constant list in file: `//:maven_deps.bzl`
# How to update the lock file? just run the following bazel command: `bazel run @unpinned_maven//:pin`
# import maven_install rule, we will need this to download all the dependencies for our java projects
load("@rules_jvm_external//:defs.bzl", "maven_install")

def maven_setup():
    maven_install(
        maven_install_json = "//tools/maven:maven_install.json",
        artifacts = [] + MAVEN_DEPS,
        # dialob defines this dependency, but such dependency we exclude this and define the actual one:
        # "org.hibernate.validator:hibernate-validator:6.2.5.Final",
        excluded_artifacts = ["org.hibernate:hibernate-validator"],
        repositories = [
          "https://maven.google.com",
          "https://repo1.maven.org/maven2",
        ],
        version_conflict_policy = 'pinned',
        fetch_sources = True,
        strict_visibility = True,
        generate_compat_repositories = False,
    )

