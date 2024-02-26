package hellojpa;

import jakarta.persistence.MappedSuperclass;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class BaseEntity {
  private String createdBy;
  private LocalDateTime createdAt;
  private String lastModifiedBy;
  private LocalDateTime lastModifiedAt;
}
