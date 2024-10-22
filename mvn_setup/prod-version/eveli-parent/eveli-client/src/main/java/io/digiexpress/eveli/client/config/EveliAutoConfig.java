package io.digiexpress.eveli.client.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.digiexpress.eveli.client.persistence.entities.TaskEntity;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;



@Configuration
@EnableTransactionManagement
@EntityScan( basePackageClasses = { TaskEntity.class })
@EnableJpaRepositories(basePackageClasses = { TaskRepository.class })
public class EveliAutoConfig {

}
