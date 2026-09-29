# Java and Spring Boot Guidelines

Reusable engineering guidelines for Java 21 and Spring Boot. Apply them when writing or changing application code.

## Language and framework

- Use Java 21.
- Follow standard Spring Boot conventions for project layout, configuration, and component stereotypes.

## Layering

- Keep controllers focused on HTTP and request handling: map requests, call the appropriate service, and return responses.
- Keep business logic outside controllers.
- Separate API, business/domain logic, and persistence concerns so each layer has a single responsibility.

## Dependency injection

- Use constructor-based dependency injection.
- Prefer immutable dependencies (`final` fields) injected through the constructor.

## Validation and errors

- Validate incoming requests at the backend boundary before business logic runs.
- Use centralized, meaningful exception and error handling so clients receive consistent error responses.

## Configuration

- Keep configuration externalized. Do not hardcode environment-specific values in code.

## Quality

- Write maintainable and testable code: small units, clear boundaries, and dependencies that can be substituted in tests.

## Safety

- Never commit secrets or credentials.
- Never commit or push anything unless asked specifically.
