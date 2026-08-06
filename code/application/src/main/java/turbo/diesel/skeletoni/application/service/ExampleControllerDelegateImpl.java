package turbo.diesel.skeletoni.application.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import turbo.diesel.skeletoni.application.command.CreateExampleCommand;
import turbo.diesel.skeletoni.application.port.in.ExampleUseCase;
import turbo.diesel.skeletoni.contract.dto.CreateExampleRequest;
import turbo.diesel.skeletoni.contract.dto.ExampleResponse;
import turbo.diesel.skeletoni.contract.web.ExampleControllerDelegate;
import turbo.diesel.skeletoni.domain.model.Example;

@Component
@RequiredArgsConstructor
public class ExampleControllerDelegateImpl implements ExampleControllerDelegate {

  private final ExampleUseCase exampleUseCase;

  @Override
  public ExampleResponse createExample(CreateExampleRequest request) {
    CreateExampleCommand command = new CreateExampleCommand(request.getName());
    Example example = exampleUseCase.createExample(command);
    return mapToResponse(example);
  }

  @Override
  public List<ExampleResponse> listExamples() {
    return exampleUseCase.listExamples().stream()
        .map(this::mapToResponse)
        .toList();
  }

  private ExampleResponse mapToResponse(Example example) {
    return ExampleResponse.builder()
        .id(example.getId().value())
        .name(example.getName())
        .createdAt(example.getCreatedAt())
        .build();
  }
}
