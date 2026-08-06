package turbo.diesel.skeletoni.application.port.out;

import java.util.List;
import java.util.Optional;

import turbo.diesel.skeletoni.domain.model.Example;
import turbo.diesel.skeletoni.domain.model.ExampleId;

public interface ExampleRepository {

  void save(Example example);

  Optional<Example> findById(ExampleId id);

  List<Example> findAll();
}
