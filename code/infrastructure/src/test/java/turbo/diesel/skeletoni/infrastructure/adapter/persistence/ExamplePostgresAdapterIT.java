package turbo.diesel.skeletoni.infrastructure.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import turbo.diesel.skeletoni.domain.model.Example;
import turbo.diesel.skeletoni.domain.model.ExampleId;
import turbo.diesel.skeletoni.infrastructure.mapper.ExampleMapperImpl;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ExamplePostgresAdapter.class, ExampleMapperImpl.class})
class ExamplePostgresAdapterIT {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
      .withDatabaseName("skeletoni_test")
      .withUsername("test")
      .withPassword("test")
      .withInitScript("db/migration/V1__init_examples.sql");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.flyway.enabled", () -> "false");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
  }

  @Autowired
  private ExamplePostgresAdapter adapter;

  @Test
  @DisplayName("Should save and retrieve an example by ID")
  void shouldSaveAndFindById() {
    Example example = Example.create("Integration Test Example");

    adapter.save(example);

    Optional<Example> found = adapter.findById(example.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Integration Test Example");
    assertThat(found.get().getId()).isEqualTo(example.getId());
  }

  @Test
  @DisplayName("Should return empty when ID does not exist")
  void shouldReturnEmptyForNonExistentId() {
    Optional<Example> found = adapter.findById(ExampleId.generate());
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should list all saved examples")
  void shouldFindAll() {
    adapter.save(Example.create("First"));
    adapter.save(Example.create("Second"));

    List<Example> all = adapter.findAll();
    assertThat(all).hasSizeGreaterThanOrEqualTo(2);
  }
}
