# This is how we configure annotation processors for immutables.
# Visibility is private because we are going to expose annotation processors as one fixed set as a java library
java_plugin(
    visibility = ["//visibility:private"], #only visible inside this file
    name = "immutables_plugin_2",
    generates_api = True,
    processor_class = "org.immutables.processor.ProxyProcessor",
    deps = ["@maven//:org_immutables_value"],
)

# This is how we configure annotation processors for lombok.
# Visibility is private because we are going to expose annotation processors as one fixed set as a java library
java_plugin(
    visibility = ["//visibility:private"],  #only visible inside this file
    name = "lombok_plugin",
    generates_api = True,
    processor_class = "lombok.launch.AnnotationProcessorHider$AnnotationProcessor",
    deps = ["@maven//:org_projectlombok_lombok"],
)

# This is a set of our annotation processors that we are going to be using in java projects
# in desired java library include this as a dependency: "//tools/maven:bean_processors"
java_library(
    visibility = ["//visibility:public"],
    name = "bean_processors",
    exported_plugins = [
      ":lombok_plugin",
      ":immutables_plugin_2",
    ],
    neverlink = 1,
    exports = [
      "@maven//:org_projectlombok_lombok",
      "@maven//:org_immutables_value",
    ],
    tags = ["maven:compile_only"]
)

load("//tools/maven:maven_test_deps.bzl", "MAVEN_TEST_DEPS")
java_library(
    visibility = ["//visibility:public"],
    name = "junit_deps",
    exports = MAVEN_TEST_DEPS,
)