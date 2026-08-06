package turbo.diesel.skeletoni.contract.web;

import java.util.List;

import turbo.diesel.skeletoni.contract.dto.CreateExampleRequest;
import turbo.diesel.skeletoni.contract.dto.ExampleResponse;

/**
 * Delegate interface for ExampleController.
 * Implemented in the application layer to decouple contract from domain.
 */
public interface ExampleControllerDelegate {

  ExampleResponse createExample(CreateExampleRequest request);

  List<ExampleResponse> listExamples();
}
