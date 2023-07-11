package io.digiexpress.spring.composer.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(name = "digiexpress.db.pg.enabled", havingValue = "true")
public class PgConfig {
  

}
