package turbo.diesel.skeletoni.application.port.in;

import java.util.List;

import turbo.diesel.skeletoni.application.command.CreateExampleCommand;
import turbo.diesel.skeletoni.domain.model.Example;

public interface ExampleUseCase {

  Example createExample(CreateExampleCommand command);

  List<Example> listExamples();
}
