package io.digiexpress.spring.composer.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(name = "dialob.db.file.enabled", havingValue = "true")
public class FileConfig {
  

}
