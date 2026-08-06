package turbo.diesel.skeletoni.application.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import turbo.diesel.skeletoni.application.command.CreateExampleCommand;
import turbo.diesel.skeletoni.application.port.in.ExampleUseCase;
import turbo.diesel.skeletoni.application.port.out.ExampleRepository;
import turbo.diesel.skeletoni.domain.model.Example;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExampleService implements ExampleUseCase {

  private final ExampleRepository exampleRepository;

  @Override
  public Example createExample(CreateExampleCommand command) {
    log.info("Creating example with name: {}", command.getName());
    Example example = Example.create(command.getName());
    exampleRepository.save(example);
    return example;
  }

  @Override
  public List<Example> listExamples() {
    return exampleRepository.findAll();
  }
}
