# Jimi - Java Implementation of Moonshot Intelligence

Jimi is a Java implementation of Kimi CLI, providing a powerful CLI agent for software development tasks and terminal operations.

## Overview

This project is a complete Java rewrite of the Python-based Kimi CLI, using Java 17, Maven, and the Spring ecosystem while maintaining functional parity with the original implementation.

## Technology Stack

- **Java 17**: Modern Java language features
- **Maven**: Build and dependency management
- **Spring Boot 3.x**: Application framework and dependency injection
- **Project Reactor**: Reactive programming for async operations
- **Picocli**: Command-line argument parsing
- **JLine 3**: Interactive terminal capabilities
- **Jackson**: JSON/YAML processing
- **SLF4J + Logback**: Logging

## Project Structure

```
jimi/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/io/leavesfly/jimi/
│   │   │   ├── JimiApplication.java
│   │   │   ├── cli/
│   │   │   ├── config/
│   │   │   ├── session/
│   │   │   ├── soul/
│   │   │   ├── llm/
│   │   │   ├── tools/
│   │   │   ├── ui/
│   │   │   ├── wire/
│   │   │   ├── exception/
│   │   │   └── util/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── agents/
│   │       └── logback-spring.xml
│   └── test/
│       └── java/io/leavesfly/jimi/
└── README.md
```

## Key Features

- **Shell Mode**: Interactive terminal with shell command execution
- **Print Mode**: Non-interactive batch processing
- **ACP Support**: Agent Client Protocol for IDE integration
- **MCP Support**: Model Context Protocol for tool integration
- **Context Management**: Automatic context compression and checkpointing
- **Approval Mechanism**: User confirmation for dangerous operations
- **D-Mail**: Time-travel capability for error correction

## Requirements

- Java 17 or higher
- Maven 3.9 or higher

## Building

```bash
mvn clean package
```

## Running

```bash
java -jar target/jimi-0.1.0.jar [options]
```

Or use the Maven wrapper:

```bash
mvn spring-boot:run
```

## Configuration

Configuration file location: `~/.kimi-cli/config.json`

## Development

### Compile

```bash
mvn compile
```

### Run Tests

```bash
mvn test
```

### Package

```bash
mvn package
```

## License

Same as the original Kimi CLI project.

## Acknowledgments

This is a Java reimplementation of [Kimi CLI](https://github.com/MoonshotAI/kimi-cli) by Moonshot AI.
