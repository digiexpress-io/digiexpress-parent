package io.digiexpress.eveli.client.persistence.entities;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Entity
@Table(name="task_process_event")
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class EventEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "target_id", nullable = false)
  private String targetId;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private EventStatus status;
  
  @Column(name = "created_date", nullable = false)
  private ZonedDateTime created;
  
  @Column(name = "updated_date")
  private ZonedDateTime updated;

  @Column(name = "event_type")
  @Enumerated(EnumType.STRING)
  private EvenType eventType;
  
  @Column(name = "event_body", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private String eventBody;
  
  @Column(name = "error_text")
  private String errorText;
  
  public enum EventStatus {
    DONE, FAILED, NEW
  }
  
  public enum EvenType {
    TASK_EVENT, 
  }
  
  @PrePersist
  void prePersist() {
    updated = ZonedDateTime.now(ZoneId.of("UTC"));
    if (id == null) {
      created = updated;
    }
    if (status == null) {
      status = EventStatus.NEW;
    }
  }

}
