package turbo.diesel.skeletoni.infrastructure.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "examples")
@Getter
@Setter
public class ExampleJpaEntity {

  @Id
  private UUID id;

  private String name;

  private OffsetDateTime createdAt;
}
