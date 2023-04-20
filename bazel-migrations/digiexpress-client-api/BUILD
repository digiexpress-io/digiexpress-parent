# Load the JUNIT5 for running tests with JUNIT5 runner and based on java file name pattern
load("@contrib_rules_jvm//java:defs.bzl", "JUNIT5_DEPS", "java_test_suite")

java_library(
    visibility = ["//visibility:private"],
    name = "api_dependencies",
    exports = [
      "@maven//:ch_qos_logback_logback_classic",
      "@maven//:com_fasterxml_jackson_datatype_jackson_datatype_guava",
      "@maven//:com_fasterxml_jackson_datatype_jackson_datatype_jdk8",
      "@maven//:com_fasterxml_jackson_datatype_jackson_datatype_jsr310",
      "@maven//:com_fasterxml_jackson_core_jackson_core",

      "@maven//:com_google_code_findbugs_jsr305",
      "@maven//:commons_codec_commons_codec",
      "@maven//:io_dialob_dialob_client_api",
      "@maven//:io_dialob_dialob_form_store_pgsql",
      "@maven//:io_resys_hdes_hdes_client_api",
      "@maven//:io_resys_hdes_hdes_store_thena",
      "@maven//:io_resys_hdes_thena_docdb_pgsql",
      "@maven//:io_smallrye_reactive_mutiny",
      "@maven//:io_thestencil_stencil_client_api",
      "@maven//:jakarta_validation_jakarta_validation_api",
      "@maven//:org_apache_commons_commons_lang3",
      "@maven//:org_ehcache_ehcache",
      "@maven//:org_immutables_gson",
      "@maven//:org_immutables_value",
      "@maven//:org_projectlombok_lombok",
      "@maven//:org_slf4j_slf4j_api",
      "@maven//:com_fasterxml_jackson_core_jackson_annotations",
      "@maven//:com_fasterxml_jackson_core_jackson_databind",
      "@maven//:io_dialob_dialob_api",
      "@maven//:io_dialob_dialob_compiler",
      "@maven//:io_dialob_dialob_rule_parser",
      "@maven//:io_resys_hdes_thena_docdb_api",
      "@maven//:io_smallrye_reactive_smallrye_mutiny_vertx_pg_client",
      "@maven//:io_vertx_vertx_pg_client",
      "@maven//:io_vertx_vertx_sql_client",
      "@maven//:com_google_guava_guava",
      "@maven//:com_google_errorprone_error_prone_annotations"
    ],
)


java_library(
  name = "digiexpress-client-api",
	srcs = glob(["src/main/java/**/*.java"]),
  resources = glob(["src/main/resources/**"]),
  visibility = ["//visibility:public"],
  deps = [
    "//tools/maven:bean_processors",
    ":api_dependencies"
  ],
)

java_library(
  visibility = ["//visibility:private"],
  name = "digiexpress_unit_tests_resources",
  srcs = [],
  resources = glob(["src/test/resources/**/*"]),
)


java_test_suite(
    name = "digiexpress_unit_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    runner = "junit5",
    data = glob(["pom.xml"]),
    test_suffixes = ["Test.java"],
    runtime_deps = JUNIT5_DEPS,
    deps = [
        ":digiexpress_unit_tests_resources",
        ":digiexpress-client-api",
        "//tools/maven:bean_processors",
        "//tools/maven:junit_deps"
    ],
)

