# Contributing to Doma Tools for IntelliJ

Thank you for your interest in contributing to this project!

Please follow the guidelines below to set up your environment and submit contributions.

## License

All original contributions to Doma Tools are licensed under ASL - [Apache License, version 2.0](https://www.apache.org/licenses/LICENSE-2.0) or later.

## About This Project

This plugin project is based on the [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template).

For other basic project information and best practices, please refer to the template repository.

## Reporting Issues

  Use the [GitHub Issues page](https://github.com/domaframework/doma-tools-for-intellij/issues) to report bugs or request features.
  Write the issue in English to share it with many people.

## Contact

Let's work together to make this project better!

If you have any questions or suggestions, feel free to open an issue or contact the maintainers via GitHub.

## Prerequisites

- Install **Git** and configure your GitHub access
- Install **JDK 17**

## Recommended IntelliJ IDEA Plugins

We recommend installing the following plugins in IntelliJ IDEA to improve your development experience:

You can install these plugins from `File > Settings > Plugins` in IntelliJ IDEA.

- **[Plugin DevKit](https://plugins.jetbrains.com/plugin/22851-plugin-devkit)**
- **[PsiViewer](https://plugins.jetbrains.com/plugin/227-psiviewer)**
- **[Spotless Gradle](https://plugins.jetbrains.com/plugin/18321-spotless-gradle)**

## Setting Up the Development Environment

1. **Clone the Repository**

   ```sh
   git clone https://github.com/domaframework/doma-tools-for-intellij.git
   cd doma-tools-for-intellij
   ```

2. **Open the Project in IntelliJ IDEA**
   - Select `Open` and choose the project root directory.
   - IntelliJ will automatically import the Gradle project.

3. **Generate Custom Language Java Files (Grammar-Kit)**
   - Before building the project, generate the lexer and parser Java files using Grammar-Kit tasks:
     ```sh
     ./gradlew generateLexer
     ./gradlew generateParser
     ```
   - If you make changes to `Sql.bnf` or `Sql.flex`, be sure to re-run the above tasks to regenerate the Java files.
   - If you encounter build errors related to generated sources, try deleting the generated `gen` directory and re-running the Grammar-Kit tasks.

4. **Build the Project**
   - Use the Gradle tool window or run:
     ```sh
     ./gradlew build
     ```

5. **Run/Debug the Plugin**
   - Use the Gradle task `runIde` to launch a sandboxed IntelliJ instance:
     ```sh
     ./gradlew runIde
     ```
## Code Style

We use [Spotless](https://github.com/diffplug/spotless) and [google-java-format](https://github.com/google/google-java-format) for code formatting and style checking.

- To check or apply formatting, run the following Gradle tasks:
  ```sh
  ./gradlew spotlessCheck   # Check code format
  ./gradlew spotlessApply   # Apply code format
  ```
- Alternatively, formatting will also be applied automatically when you build the project:
  ```sh
  ./gradlew build
  ```

Please ensure your code is formatted with Spotless before submitting a pull request.

## Branch Naming Rules

When creating a working branch, please follow the naming rules below according to the type of change:

- Bug fixes: `fix/`
- Documentation changes: `document/`
- CI-related changes: `ci/`
- Dependency updates: `dependencies/` (Note: Version upgrades for dependencies are handled automatically by Renovate)
- New features: `feature/`
- Refactoring: `chore/`

For example, a bug fix branch might be named `fix/typo-in-readme`, and a new feature branch might be `feature/add-user-authentication`.

## Submitting a Pull Request

1. Fork the repository and create your branch from `main`.
2. Make your changes and ensure all tests pass:
   ```sh
   ./gradlew check
   ```
3. Submit a pull request with a clear description of your changes.
