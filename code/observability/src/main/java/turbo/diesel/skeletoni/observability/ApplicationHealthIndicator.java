package turbo.diesel.skeletoni.observability;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for application-level readiness checks.
 * Extend this with real dependency checks (e.g. Kafka connectivity) as needed.
 */
@Component
public class ApplicationHealthIndicator implements HealthIndicator {

  @Override
  public Health health() {
    return Health.up()
        .withDetail("service", "skeletoni")
        .withDetail("status", "operational")
        .build();
  }
}
