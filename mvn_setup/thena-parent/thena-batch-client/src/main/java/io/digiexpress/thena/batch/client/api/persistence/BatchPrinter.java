package io.digiexpress.thena.batch.client.api.persistence;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ComparisonChain;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.jackson.QuarkusJacksonJsonCodec;


public class BatchPrinter {
  private final BatchDb state;

  public BatchPrinter(BatchDb state) {
    super();
    this.state = state;
  } 
  
  public String print(Tenant repo) {
    
      final var ctx = state.withTenant(repo);
      final var idMasker = new IdMasker(true, new HashMap<String, String>());
  
      StringBuilder result = new StringBuilder().append(System.lineSeparator())
        .append("Batch tenant").append(System.lineSeparator())
        .append("  - id: ").append(repo.getId())
        .append(", rev: ").append(repo.getRev()).append(System.lineSeparator())
        .append("    name: ").append(repo.getName())
        .append(", prefix: ").append(repo.getPrefix())
        .append(", type: ").append(repo.getType()).append(System.lineSeparator());

  
      final var container = ctx.query().findAll().await().atMost(Duration.ofMinutes(5));
  
      
      // Mask dates
      container.getBatches().values().stream().forEach(e -> idMasker.maskDate(e.getCreatedAt()));
      container.getBatchConsumers().values().stream().forEach(e -> {
        idMasker.maskDate(e.getCreatedAt());
        idMasker.maskDate(e.getUpdatedAt().orElse(null));
      });

      container.getRuntimeInstances().values().stream().forEach(e -> idMasker.maskDate(e.getCreatedAt()));
      container.getRuntimeLogs().values().stream().forEach(e -> idMasker.maskDate(e.getCreatedAt()));
      container.getRuntimeMetrics().values().stream().forEach(e -> {
        idMasker.maskDate(e.getCreatedAt());
        idMasker.maskDate(e.getUpdatedAt().orElse(null));
      });
      container.getRuntimeParams().values().stream().forEach(e -> idMasker.maskDate(e.getCreatedAt()));
      container.getRuntimeStepRows().values().stream().forEach(e -> idMasker.maskDate(e.getCreatedAt()));
      container.getRuntimeSteps().values().stream().forEach(e -> idMasker.maskDate(e.getCreatedAt()));
      
      
      result.append("Batches: ").append(System.lineSeparator());      
      container.getBatches().values().stream()
        .sorted((a, b) -> ComparisonChain.start()
            .compare(a.getBatchName(), b.getBatchName())
            .result())
        .forEach(batch -> result
          .append("  - ").append(idMasker.maskId(batch.getId())).append("::").append(batch.getDocType()).append(System.lineSeparator())
          .append("    ").append(writeValueAsString(batch)).append(System.lineSeparator())
        );
      
      result.append("Consumers: ").append(System.lineSeparator());      
      container.getBatchConsumers().values().stream()
        .sorted((a, b) -> ComparisonChain.start()
            .compare(a.getBatchName(), b.getBatchName())
            .compare(a.getAppId(), b.getAppId())
            .compare(a.getConsumerName(), b.getConsumerName())
            .result())
        .forEach(batch -> result
          .append("  - ").append(idMasker.maskId(batch.getId())).append("::").append(batch.getDocType()).append(System.lineSeparator())
          .append("    ").append(writeValueAsString(batch)).append(System.lineSeparator())
        );
      
      result.append("Instances: ").append(System.lineSeparator());      
      container.getRuntimeInstances().values().stream()
        .sorted((a, b) -> ComparisonChain.start()
            .compare(a.getName(), b.getName())
            .compare(a.getCreatedAt(), b.getCreatedAt())
            .result())
        .forEach(batch -> result
          .append("  - ").append(idMasker.maskId(batch.getId())).append("::").append(batch.getDocType()).append(System.lineSeparator())
          .append("    ").append(writeValueAsString(batch)).append(System.lineSeparator())
        );
      
      result.append("Instances Steps: ").append(System.lineSeparator());      
      container.getRuntimeSteps().values().stream()
        .sorted((a, b) -> ComparisonChain.start()
            .compare(a.getName(), b.getName())
            .compare(a.getCreatedAt(), b.getCreatedAt())
            .result())
        .forEach(batch -> result
          .append("  - ").append(idMasker.maskId(batch.getId())).append("::").append(batch.getDocType()).append(System.lineSeparator())
          .append("    ").append(writeValueAsString(batch)).append(System.lineSeparator())
        ); 
          
      result.append("Instances Step Rows: ").append(System.lineSeparator());      
      container.getRuntimeStepRows().values().stream()
        .sorted((a, b) -> ComparisonChain.start()
            .compare(a.getStepId(), b.getStepId())
            .compare(a.getRowNumber(), b.getRowNumber())
            .compare(a.getCreatedAt(), b.getCreatedAt())
            .result())
        .forEach(batch -> result
          .append("  - ").append(idMasker.maskId(batch.getId())).append("::").append(batch.getDocType()).append(System.lineSeparator())
          .append("    ").append(writeValueAsString(batch)).append(System.lineSeparator())
        ); 
      
      result.append("Instances Metrics: ").append(System.lineSeparator());      
      container.getRuntimeMetrics().values().stream()
        .sorted((a, b) -> ComparisonChain.start()
            .compare(a.getRuntimeId(), b.getRuntimeId())
            .compare(a.getStepId().orElse(""), b.getStepId().orElse(""))
            .compare(a.getCreatedAt(), b.getCreatedAt())
            .result())
        .forEach(batch -> result
          .append("  - ").append(idMasker.maskId(batch.getId())).append("::").append(batch.getDocType()).append(System.lineSeparator())
          .append("    ").append(writeValueAsString(batch)).append(System.lineSeparator())
        ); 
      
      result.append("Instances Logs: ").append(System.lineSeparator());      
      container.getRuntimeLogs().values().stream()
        .sorted((a, b) -> ComparisonChain.start()
            .compare(a.getRuntimeId(), b.getRuntimeId())
            .compare(a.getStepId().orElse(""), b.getStepId().orElse(""))
            .compare(a.getCreatedAt(), b.getCreatedAt())
            .result())
        .forEach(batch -> result
          .append("  - ").append(idMasker.maskId(batch.getId())).append("::").append(batch.getDocType()).append(System.lineSeparator())
          .append("    ").append(writeValueAsString(batch)).append(System.lineSeparator())
        ); 
      
      
      result.append("Instances Params: ").append(System.lineSeparator());      
      container.getRuntimeParams().values().stream()
        .sorted((a, b) -> ComparisonChain.start()
            .compare(a.getRuntimeId(), b.getRuntimeId())
            .compare(a.getStepId().orElse(""), b.getStepId().orElse(""))
            .compare(a.getRowId().orElse(""), b.getRowId().orElse(""))
            .compare(a.getCreatedAt(), b.getCreatedAt())
            .result())
        .forEach(batch -> result
          .append("  - ").append(idMasker.maskId(batch.getId())).append("::").append(batch.getDocType()).append(System.lineSeparator())
          .append("    ").append(writeValueAsString(batch)).append(System.lineSeparator())
        ); 
      
      return result.toString();

  }


  private String writeValueAsString(Object value) {
    try {
      return QuarkusJacksonJsonCodec.mapper().writeValueAsString(value);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  private static class IdMasker {
    private final boolean isStatic; 
    private final Map<String, String> collector;
    private final Map<String, String> wipes = new HashMap<>();
    private final Map<String, String> replacements;
    
    public IdMasker(boolean isStatic, Map<String, String> collector) {
      super();
      this.isStatic = isStatic;
      this.collector = collector;
      this.replacements = collector != null ? collector : new HashMap<>();
    }

    
    public String maskId(String id) {
      if(!isStatic) {
        return id;
      }
      if(id == null) {
        return null;
      }
      
      if(replacements.containsKey(id)) {
        return replacements.get(id);
      }
      final var next = String.valueOf(replacements.size() + 1);
      replacements.put(id, next);
      return next;
    };

   
    
    public String maskDate(OffsetDateTime input) {
      if(input == null) {
        return null;
      }
      try {
        final var id = QuarkusJacksonJsonCodec.mapper().writeValueAsString(input);
        if(!isStatic) {
          return id.toString();
        }

        if(replacements.containsKey(id)) {
          return replacements.get(id);
        }

        final var next = "\"OffsetDateTime.now()\"";
        replacements.put(id, next);
        return next;
      } catch(Exception e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    };  
  }
}
