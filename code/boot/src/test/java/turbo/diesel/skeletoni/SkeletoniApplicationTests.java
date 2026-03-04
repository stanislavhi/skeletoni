package turbo.diesel.skeletoni;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SkeletoniApplicationTests {

  @Test
  @DisplayName("Context should load successfully")
  void contextLoads(ApplicationContext context) {
    assertThat(context).isNotNull();
    assertThat(context.containsBean("skeletoniApplication")).isTrue();
  }

}
