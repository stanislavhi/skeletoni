package turbo.diesel.skeletoni.contract.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExampleResponse {

  private UUID id;

  private String name;

  private OffsetDateTime createdAt;
}
