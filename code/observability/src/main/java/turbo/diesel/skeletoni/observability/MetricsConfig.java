package turbo.diesel.skeletoni.observability;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Centralised Micrometer configuration.
 * Adds common tags to every metric emitted by the application.
 */
@Configuration
public class MetricsConfig {

  @Bean
  public MeterRegistryCustomizer<MeterRegistry> commonTags(Environment environment) {
    return registry -> registry.config()
        .commonTags(
            "application", environment.getProperty("spring.application.name", "skeletoni"),
            "environment", environment.getProperty("spring.profiles.active", "default")
        );
  }
}
