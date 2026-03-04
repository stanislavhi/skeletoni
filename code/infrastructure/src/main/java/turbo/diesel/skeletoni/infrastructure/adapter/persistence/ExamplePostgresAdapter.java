package turbo.diesel.skeletoni.infrastructure.adapter.persistence;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import turbo.diesel.skeletoni.application.port.out.ExampleRepository;
import turbo.diesel.skeletoni.domain.model.Example;
import turbo.diesel.skeletoni.domain.model.ExampleId;
import turbo.diesel.skeletoni.infrastructure.entity.ExampleJpaEntity;
import turbo.diesel.skeletoni.infrastructure.mapper.ExampleMapper;
import turbo.diesel.skeletoni.infrastructure.repository.ExampleJpaRepository;

@Component
@RequiredArgsConstructor
public class ExamplePostgresAdapter implements ExampleRepository {

  private final ExampleJpaRepository repository;

  private final ExampleMapper mapper;

  @Override
  public void save(Example example) {
    ExampleJpaEntity entity = mapper.toEntity(example);
    repository.save(entity);
  }

  @Override
  public Optional<Example> findById(ExampleId id) {
    return repository.findById(id.value())
        .map(mapper::toDomain);
  }

  @Override
  public List<Example> findAll() {
    return repository.findAll().stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }
}
