package turbo.diesel.skeletoni.domain.model;

import java.time.OffsetDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class Example {

  private final ExampleId id;

  private final String name;

  private final OffsetDateTime createdAt;

  public static Example create(String name) {
    return Example.builder()
        .id(ExampleId.generate())
        .name(name)
        .createdAt(OffsetDateTime.now())
        .build();
  }
}
