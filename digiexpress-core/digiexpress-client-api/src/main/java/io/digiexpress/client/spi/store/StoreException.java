package io.digiexpress.client.spi.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.digiexpress.client.api.ServiceStore.StoreEntity;
import io.digiexpress.client.api.ServiceStore.StoreExceptionMsg;



public class StoreException extends RuntimeException {

  private static final long serialVersionUID = 7058468238867536222L;

  private final String code;
  private final Optional<StoreEntity> target;
  private final List<StoreExceptionMsg> messages = new ArrayList<>();
  
  public StoreException(Exception e, String code, StoreEntity target) {
    super(e.getMessage(), e);
    this.code = code;
    this.target = Optional.ofNullable(target);
  }

  public StoreException(Exception e, String code, StoreEntity target, StoreExceptionMsg ... msg) {
    super(e.getMessage(), e);
    this.code = code;
    this.target = Optional.ofNullable(target);
    this.messages.addAll(Arrays.asList(msg));
  }
    
  public StoreException(String code, StoreEntity target) {
    super();
    this.code = code;
    this.target = Optional.ofNullable(target);
  }

  public StoreException(String code, StoreEntity target, StoreExceptionMsg ... msg) {
    super(formatMessages(code, target, msg));
    this.code = code;
    this.target = Optional.ofNullable(target);
    this.messages.addAll(Arrays.asList(msg));
  }
  
  private static String formatMessages(String code, StoreEntity target, StoreExceptionMsg ... msg) {
    final var builder = new StringBuilder()
        .append(System.lineSeparator())
        .append("Store operation failed with:").append(System.lineSeparator())
        .append("  - code: ").append("'" + code + "'").append(System.lineSeparator());
    
    
    if(target != null) {
      builder.append("  - entity id: ").append("'" + target.getId() + "'").append(System.lineSeparator());
    }
    
    
    for(final var m : msg) {
      builder
        .append("  - msg id: '").append(m.getId()).append("'").append(System.lineSeparator())
        .append("  - msg value: '").append(m.getValue()).append("'").append(System.lineSeparator())
        .append("  - msg additional info: ").append(System.lineSeparator());
      
      for(final var arg : m.getArgs()) {
        final var nested = Arrays.asList(arg.trim()
            .split(System.lineSeparator())).stream()
            .map(n -> n.trim())
            .filter(n -> !n.isEmpty())
            .map(n -> {
              if(n.startsWith("-")) {
                return n.substring(1).trim();
              }
              return n;
            })
            .collect(Collectors.toList());
        
        if(!nested.isEmpty()) {
          builder.append("    - ").append(nested.get(0)).append(System.lineSeparator());
        } 
        
        for(int index = 1; index < nested.size(); index++) {
          builder.append("      - ").append(nested.get(index)).append(System.lineSeparator());
        }
      }
    }
    
    return builder.toString();
  }
  
  
  
  public String getCode() {
    return code;
  }
  public Optional<StoreEntity> getTarget() {
    return target;
  }
  public List<StoreExceptionMsg> getMessages() {
    return messages;
  }
}
