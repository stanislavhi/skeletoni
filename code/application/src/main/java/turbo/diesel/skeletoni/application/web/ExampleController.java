package turbo.diesel.skeletoni.application.web;

import java.util.List;
import java.util.stream.Collectors;

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
import turbo.diesel.skeletoni.application.command.CreateExampleCommand;
import turbo.diesel.skeletoni.application.port.in.ExampleUseCase;
import turbo.diesel.skeletoni.contract.dto.CreateExampleRequest;
import turbo.diesel.skeletoni.contract.dto.ExampleResponse;
import turbo.diesel.skeletoni.domain.model.Example;

@RestController
@RequestMapping("/api/v1/examples")
@RequiredArgsConstructor
@Tag(name = "Example", description = "Example API")
public class ExampleController {

  private final ExampleUseCase exampleUseCase;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a new example")
  public ExampleResponse createExample(@RequestBody CreateExampleRequest request) {
    CreateExampleCommand command = new CreateExampleCommand(request.getName());
    Example example = exampleUseCase.createExample(command);
    return mapToResponse(example);
  }

  @GetMapping
  @Operation(summary = "List examples")
  public List<ExampleResponse> listExamples() {
    return exampleUseCase.listExamples().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  private ExampleResponse mapToResponse(Example example) {
    return ExampleResponse.builder()
        .id(example.getId().value())
        .name(example.getName())
        .createdAt(example.getCreatedAt())
        .build();
  }
}
