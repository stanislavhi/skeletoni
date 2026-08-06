package turbo.diesel.skeletoni.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j configuration.
 * Circuit breaker and retry instances are configured via application.yml
 * under the {@code resilience4j.circuitbreaker} and {@code resilience4j.retry} keys.
 * This class serves as the wiring point and can be extended with
 * programmatic customisations if needed.
 */
@Slf4j
@Configuration
public class ResilienceConfig {

  public ResilienceConfig(CircuitBreakerRegistry circuitBreakerRegistry,
                          RetryRegistry retryRegistry) {
    circuitBreakerRegistry.getEventPublisher()
        .onEntryAdded(event -> {
          var cb = event.getAddedEntry();
          log.info("CircuitBreaker '{}' registered", cb.getName());
          cb.getEventPublisher()
              .onStateTransition(e -> log.warn("CircuitBreaker '{}' state transition: {}",
                  cb.getName(), e.getStateTransition()));
        });

    retryRegistry.getEventPublisher()
        .onEntryAdded(event -> log.info("Retry '{}' registered", event.getAddedEntry().getName()));
  }
}
