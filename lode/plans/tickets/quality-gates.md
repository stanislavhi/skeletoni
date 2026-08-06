# Epic SKL-QLTY — Tests & Contract Verification

The mandatory-tests rule in [../../practices.md](../../practices.md) is currently unmet: two tests
exist for the whole codebase. Board: [../backlog.md](../backlog.md).

---

## SKL-18 — Unit tests for application, mapper and filter

**Type** Task · **Priority** P1 · **Estimate** M · **Status** Todo

Untested classes: `ExampleService`, `ExampleControllerDelegateImpl`, `ExampleMapper`,
`CorrelationIdFilter`, `MetricsConfig`, `ApplicationHealthIndicator`. All are cheap to test —
constructor injection everywhere means no Spring context is needed for most of them.

```java
@ExtendWith(MockitoExtension.class)
class ExampleServiceTest {
  @Mock ExampleRepository exampleRepository;
  @InjectMocks ExampleService exampleService;

  @Test void createExamplePersistsAndReturnsAggregate() {
    Example result = exampleService.createExample(new CreateExampleCommand("n"));
    verify(exampleRepository).save(result);
    assertThat(result.getId()).isNotNull();
    assertThat(result.getName()).isEqualTo("n");
  }
}
```

Priority targets and what each must actually prove:

| Class | Must prove |
|---|---|
| `ExampleService` | save is called with the returned aggregate; id generated |
| `ExampleControllerDelegateImpl` | `ExampleId` unwrapped to raw `UUID` in the response |
| `ExampleMapper` (via `ExampleMapperImpl`) | round-trip domain→entity→domain preserves id; nulls handled |
| `CorrelationIdFilter` | header reused when present; generated when blank; **MDC cleared even when the chain throws** |

That last case is the one that matters — a leaked MDC key attaches one request's correlation ID to
an unrelated request on a reused container thread.

**Acceptance**
- [ ] Each listed class has a `*Test` in its own module
- [ ] `CorrelationIdFilter` test covers the exception path, asserting MDC is empty afterwards
- [ ] Mapper test runs against the generated `ExampleMapperImpl`, not a hand-written stub
- [ ] Suite passes via `mvn -B clean verify -f code/pom.xml`

---

## SKL-19 — `@WebMvcTest` for `ExampleController`

**Type** Task · **Priority** P2 · **Estimate** S · **Status** Todo

The controller is completely untested: status codes, JSON shape, and the correlation header are all
unverified. `@WebMvcTest` with a mocked `ExampleControllerDelegate` is exactly the test the delegate
pattern was designed to make possible — see
[../../contract/rest-delegate-pattern.md](../../contract/rest-delegate-pattern.md).

Must cover: `POST` returns **201** (not 200 — `@ResponseStatus(HttpStatus.CREATED)` is easy to
regress), `GET` returns the array shape, validation failures return 400 (SKL-5), and
`X-Correlation-Id` is echoed.

**Acceptance**
- [ ] `@WebMvcTest(ExampleController.class)` with the delegate mocked
- [ ] 201 on create, 200 + array on list, 400 on invalid body
- [ ] Response JSON field names match `openapi.yml` (`id`, `name`, `createdAt`)
- [ ] `X-Correlation-Id` echo asserted

**Blocked by** SKL-5

---

## SKL-20 — Verify controller against `openapi.yml`

**Type** Story · **Priority** P2 · **Estimate** M · **Status** Todo

`ExampleController` is hand-written and `openapi.yml` is hand-maintained. CI runs Spectral, which
only checks the YAML is *well-formed* — it never compares the spec to the code. They can drift
silently, and for a contract-first project that is the most damaging possible class of bug: the
published contract lies.

Two approaches:

- **(a) Generate the controller** from `openapi.yml` with `openapi-generator-maven-plugin` in
  `delegatePattern` mode. The spec becomes executable truth, the hand-written controller disappears,
  and the existing `ExampleControllerDelegate` maps almost exactly onto the generated delegate
  interface — the project is already shaped for this.
- **(b) Conformance test** validating requests/responses against the spec at test time.

**Recommendation: (a).** It eliminates the drift class rather than detecting it, and it is the
reason the delegate indirection exists. Cost: generated sources in the build, and the delegate
interface's shape is then dictated by the generator.

**Acceptance**
- [ ] Approach chosen and recorded here
- [ ] If (a): controller generated at build time, hand-written one deleted, endpoints unchanged
- [ ] Editing `openapi.yml` without updating code fails the build
- [ ] [../../contract/rest-delegate-pattern.md](../../contract/rest-delegate-pattern.md) updated —
      its "Alternative not taken" section becomes the taken path

---

## SKL-21 — Replace CI service containers with Testcontainers

**Type** Task · **Priority** P2 · **Estimate** M · **Status** Todo

`ci.yml` starts Postgres, Mongo, Kafka and Schema Registry as GitHub service containers with **no
health gating**. Maven can begin before they accept connections, producing intermittent red builds
that are not code failures. `KNOWLEDGE-BASE.md` §17 already documents this.

The project already uses Testcontainers correctly in `ExamplePostgresAdapterIT`, which waits for
readiness by construction. Two mechanisms for the same job is one too many.

Also note the CI Kafka advertises only `PLAINTEXT://localhost:9092` while `compose.yml` needs the
dual-listener split — two divergent configurations to keep correct. Testcontainers removes that.

**Acceptance**
- [ ] Service containers removed from `ci.yml`; ITs provision their own
- [ ] Reusable Testcontainers config (`@ServiceConnection` where applicable)
- [ ] Hardcoded `SPRING_DATASOURCE_*` env vars removed from the workflow
- [ ] Ten consecutive CI runs green — flakiness is the metric here
- [ ] [../../build/ci-cd.md](../../build/ci-cd.md) and
      [../../testing/summary.md](../../testing/summary.md) updated

**Blocked by** SKL-10 (prune first, so fewer services need provisioning at all)

---

Related: [../known-gaps.md](../known-gaps.md) entries 11, 12, 13
