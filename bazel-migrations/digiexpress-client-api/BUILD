package(default_visibility = ["//visibility:public"])


java_plugin(
    name = "immutables_plugin_2",
    generates_api = True,
    processor_class = "org.immutables.processor.ProxyProcessor",
    deps = ["@maven//:org_immutables_value"],
)

java_plugin(
    name = "lombok_plugin",
    generates_api = True,
    processor_class = "lombok.launch.AnnotationProcessorHider$AnnotationProcessor",
    deps = ["@maven//:org_projectlombok_lombok"],
)


java_library(
    name = "bean_processors",
    exported_plugins = [
      ":lombok_plugin",
      ":immutables_plugin_2",
    ],
    visibility = ["//visibility:public"],
    exports = [
      "@maven//:org_projectlombok_lombok",
      "@maven//:org_immutables_value",
    ],
    tags = ["maven:compile_only"]
)


java_library(
    name = "digiexpress-client-api",
	srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    visibility = ["//visibility:public"],
    deps = [
        ":bean_processors",
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
