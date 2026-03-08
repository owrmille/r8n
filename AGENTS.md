We're creating a private review platform. Focusing on security and privacy to satisfy cautious users, German legal entities, and those who can be reviewed (public persons like politicians) or those who own something that can be reviewed (cafe owners, goods and services suppliers). Keeping the codebase clean, structured and tested.

# General
Code should be clean and well-performing. Readability and maintainability before performance. Clear naming, comments if the code is complex or non-obvious.

When in doubt, ask the developer. If you're not sure whether a specific solution would work, say it outright, keep the developer informed. Before implementing anything, check the repository whether ready solutions/approaches/whole components exist already; we want to make the project uniform and the components reusable.

Use consistent compatible external dependency versions.

When suggesting new features, respect existing tests and immediately suggest unit/integration/architectural tests to cover them.

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