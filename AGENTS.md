We're creating a private review platform. Focusing on security and privacy to satisfy cautious users, German legal entities, and those who can be reviewed (public persons like politicians) or those who own something that can be reviewed (cafe owners, goods and services suppliers). Keeping the codebase clean, structured and tested.

# General
- Code should be clean and well-performing. Readability and maintainability before performance. Clear naming, comments if the code is complex or non-obvious.
- Make the smallest change that fully solves the requested problem.
- When trade-offs exist, prioritize in this order:
1. Correctness
2. Security and privacy
3. Maintainability and readability
4. Consistency with existing architecture
5. Performance optimizations
- Use consistent compatible external dependency versions.
- Makefile as an entrypoint for developers.

# Communication
- When in doubt, ask the developer. If you're not sure whether a specific solution would work, say it outright, keep the developer informed.
- If requirements are ambiguous, ask a clarifying question before making structural or irreversible changes.
- If proceeding with assumptions, state them explicitly.
- Do not invent domain rules, validation rules, or API behavior without saying so.
- When proposing multiple valid approaches, recommend one and explain why briefly.
- When making suggestions, include a short rationale and note trade-offs if relevant.
- When changing code, mention risks, follow-up work, and tests to add or run.
- If the change touches security, privacy, data contracts, or persistence, call that out explicitly.
- Prefer concise answers, but be thorough when the change is architectural or risky.

# Preserving architecture
- Before implementing anything, check the repository whether ready solutions/approaches/whole components exist already; we want to make the project uniform and the components reusable.
- Prefer extending existing patterns over introducing new ones.
- Before suggesting a new abstraction, verify whether a similar abstraction already exists in the repository.
- Do not refactor unrelated code unless it directly improves correctness, security, or maintainability of the requested change.
- If a broader refactor would help, suggest it separately instead of mixing it into the main change.
- Preserve backward compatibility unless the task explicitly allows breaking changes.
- Prefer existing internal utilities, components, conventions, and shared modules before adding new dependencies.
- Add external dependencies only when clearly justified by substantial value.
- For any new dependency, consider maintenance cost, security surface, bundle/runtime impact, and ecosystem fit.
- Avoid duplicate libraries that solve the same problem.

# Privacy
- Treat privacy and data minimization as default design constraints.
- Collect, store, expose, and log only data that is necessary for the feature.
- Avoid exposing internal identifiers, sensitive metadata, moderation data, or implementation details in public APIs.
- Redact or avoid sensitive values in logs, exceptions, monitoring output, and test fixtures.
- Prefer designs that support auditability, moderation, traceability of administrative actions, and reversible enforcement actions.
- Call out any feature that may affect privacy, legal risk, abuse prevention, impersonation, defamation risk, or moderation workload.
- Flag places where authorization, rate limiting, anti-abuse protections, or input validation are likely required.
- Prefer explicit validation over implicit trust for client-provided data.

# Backend
- Java 21, Spring boot 4.0.2, Junit 5, Testcontainers, use most modern but stable features
- dependency management is in libs.versions.toml, then in convention plugins wherever applicable
- keep the structure microserviced, well-isolated
- gateway is reactive, all data services are not
- each microservice is split into several layers:
  - a technology-agnostic API package with just DTOs and API interfaces as contracts for external consumers (frontend and external API)
  - a feign client integration package for other services within the project being consumers, with DTOs and APIs for internal usage (not exposed to frontend and external API users)
  - service itself, also layered:
    - controller layer, getting DTOs from layers under it and providing them outside. components annotated RestController, suffixed Controller.
    - facade layer, converting model classes into DTOs and possibly fetching data from other services to enrich the DTOs. For example, for a service that stores opinions, it doesn't make sense to store usernames. So users will be stored in a separate microservice, the username field will not be present in opinions-sv model layer classes, but will be fetched and added in the facade. Components annotated Service, suffixed Facade.
    - service layer, applying business logic and orchestrating the service components within the microservice. For example, for opinions-sv fetching linked opinions for a certain opinion is an internal task, handled on a service layer. Components annotated Service, suffixed Service.
    - JPA repository and JPA entity layer, entities having Persistence suffixes, having schema, table name, column nullability explicitly defined.
- separate postgresql database node in docker with separate schemas for different microservices
- keeping kafka and kubernetes in mind, suggest when the time comes to integrate them

## API
- Keep API contracts explicit, stable, and versionable.
- Do not leak persistence models into external or internal API contracts.
- Use DTOs intentionally; avoid exposing fields “just in case.”
- Document nullability, optionality, pagination, filtering, and error semantics clearly.
- When changing a contract, mention downstream consumers that may be affected.
- Prefer additive API changes over breaking changes where possible.

## Persistence
- Be explicit about transactional boundaries.
- Avoid accidental N+1 queries and inefficient loading patterns.
- Prefer schema changes that are forward-compatible and migration-friendly.
- When changing persistence models, consider indexes, constraints, nullability, and data migration implications.
- Keep domain logic out of controllers and repositories.
- Do not use entities as cross-layer transport objects.

# Error handling
- Handle errors intentionally; do not swallow exceptions silently.
- Return consistent error responses and avoid leaking internal implementation details.
- Differentiate validation errors, authorization errors, missing resources, conflicts, and unexpected failures.
- Prefer fail-fast validation at boundaries.
- Include observability hooks where operationally useful, without exposing sensitive information.

# Testing
- When suggesting new features, respect existing tests and immediately suggest unit/integration/architectural tests to cover them.
- Every non-trivial change should include or suggest tests at the appropriate level: unit, integration, contract, or end-to-end.
- Test behavior, not implementation details.
- Cover happy path, edge cases, failure cases, and authorization/privacy-sensitive paths.
- For bug fixes, prefer adding a regression test that would fail before the fix.
- Do not remove or weaken tests to make unrelated changes pass.
- Keep test data realistic but synthetic and non-sensitive.

# Frontend
- Prefer strong typing and avoid unnecessary any.
- Keep components focused; extract reusable logic into composables/utilities when appropriate.
- Separate presentation concerns from business/data-fetching concerns where practical.
- Handle loading, error, empty, and success states explicitly.
- Respect accessibility, keyboard usability, and semantic HTML in UI changes.
- Do not expose sensitive backend details, internal IDs, or moderation-only state in the UI unless required.

# Misc
- Do not introduce placeholder TODO logic as if it were complete.
- Do not bypass validation, authorization, or domain rules for convenience.
- Do not mix unrelated formatting/refactoring changes into functional edits.
- Do not duplicate logic that should live in a shared abstraction.
- Do not guess library APIs or framework behavior when uncertain; verify first.
- Do not expose admin-only or internal fields in DTOs, logs, or UI output.
