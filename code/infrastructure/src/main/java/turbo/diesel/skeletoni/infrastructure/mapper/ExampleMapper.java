package turbo.diesel.skeletoni.infrastructure.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import turbo.diesel.skeletoni.domain.model.Example;
import turbo.diesel.skeletoni.domain.model.ExampleId;
import turbo.diesel.skeletoni.infrastructure.entity.ExampleJpaEntity;

@Mapper(componentModel = "spring")
public interface ExampleMapper {

  @Mapping(target = "id", expression = "java(entity.getId() != null ? new ExampleId(entity.getId()) : null)")
  Example toDomain(ExampleJpaEntity entity);

  @Mapping(target = "id", expression = "java(domain.getId() != null ? domain.getId().value() : null)")
  ExampleJpaEntity toEntity(Example domain);

  default UUID map(ExampleId value) {
    return value != null ? value.value() : null;
  }

  default ExampleId map(UUID value) {
    return value != null ? new ExampleId(value) : null;
  }
}
