package turbo.diesel.skeletoni.domain.model;

import java.util.UUID;

public record ExampleId(UUID value) {

  public static ExampleId generate() {
    return new ExampleId(UUID.randomUUID());
  }

  public static ExampleId fromString(String value) {
    return new ExampleId(UUID.fromString(value));
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
