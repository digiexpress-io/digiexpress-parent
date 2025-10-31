package io.resys.thena.processor.model;

import java.util.List;

import javax.lang.model.element.Element;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegistryMetamodel {
  Element element;
  String name;                    // "Grim"
  String packageName;             // "io.resys.thena.grim.spi.sql"
  String tableClassName;
  String registryClassName;
  String internalTenantQueryClassName;
  String transactionContainerClassName;
  String transactionSaveClassName;
  String worldName;
  @Builder.Default
  List<String> nonTenantTables = List.of();  // ["process", "process_id_seq"]
}