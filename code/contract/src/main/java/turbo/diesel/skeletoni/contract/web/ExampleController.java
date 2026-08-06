package turbo.diesel.skeletoni.contract.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import turbo.diesel.skeletoni.contract.dto.CreateExampleRequest;
import turbo.diesel.skeletoni.contract.dto.ExampleResponse;

@RestController
@RequestMapping("/api/v1/examples")
@RequiredArgsConstructor
@Tag(name = "Example", description = "Example API")
public class ExampleController {

  private final ExampleControllerDelegate delegate;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a new example")
  public ExampleResponse createExample(@RequestBody CreateExampleRequest request) {
    return delegate.createExample(request);
  }

  @GetMapping
  @Operation(summary = "List examples")
  public List<ExampleResponse> listExamples() {
    return delegate.listExamples();
  }
}
