package turbo.diesel.skeletoni.domain.event;

import java.time.OffsetDateTime;

import turbo.diesel.skeletoni.domain.model.ExampleId;

public record ExampleCreatedEvent(
    ExampleId id,
    String name,
    OffsetDateTime occurredAt
) {

}
