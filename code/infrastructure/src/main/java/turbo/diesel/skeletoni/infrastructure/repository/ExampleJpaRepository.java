package turbo.diesel.skeletoni.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import turbo.diesel.skeletoni.infrastructure.entity.ExampleJpaEntity;

public interface ExampleJpaRepository extends JpaRepository<ExampleJpaEntity, UUID> {

}
