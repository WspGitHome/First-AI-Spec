# Project Context

## Purpose
A Spring Boot application that automates attendance tracking and duty roster management. The system processes Excel-based duty schedules and attendance records, automatically updating attendance sheets based on parsed shift assignments. It handles complex shift scheduling logic including multi-day shifts and automatic "rest day" assignment for unassigned staff.

## Tech Stack
- **Framework**: Spring Boot 2.7.18
- **Language**: Java 8
- **Build Tool**: Maven
- **Security**: Spring Security (session-based authentication)
- **Utilities**:
  - Hutool 5.8.26 (Java utility library)
  - Apache POI 5.2.3 (Excel processing)
  - Apache Commons Lang3 3.20.0
  - Apache HttpClient 4.5.14
- **Testing**: Spring Boot Starter Test
- **Development Tools**: Maven, Git, OpenSpec for spec-driven development

## Project Conventions

### Code Style
- **Package Structure**: Standard Spring Boot layered architecture
  - `controller/` - REST API endpoints
  - `service/` - Business logic layer
  - `model/` - Data transfer objects and domain models
  - `config/` - Configuration and properties
  - `util/` - Utility classes and helpers
- **Naming Conventions**:
  - Classes: PascalCase (e.g., `OrchestrationService`)
  - Methods: camelCase (e.g., `processFiles`)
  - Constants: UPPER_SNAKE_CASE (e.g., `DEFAULT_TIMEOUT`)
  - Variables: camelCase (e.g., `employeeName`)
  - Chinese comments and log messages are acceptable (domain-specific)
- **Language**: Code in English, comments and logs can include Chinese for domain clarity
- **Documentation**: Javadoc for public methods, inline comments for complex logic

### Architecture Patterns
- **Layered Architecture**: Controller → Service → Model
- **Service Orchestration**: Complex workflows orchestrated through dedicated services (e.g., `OrchestrationService`)
- **In-Memory Processing**: Excel files processed in memory without intermediate disk writes
- **Configuration Management**: Externalized configuration via `application.yml` and `@ConfigurationProperties`
- **Dependency Injection**: Spring's `@Autowired` for loose coupling
- **RESTful API**: Stateless operations with multipart file upload/download
- **Session-Based Authentication**: Spring Security with HTTP session management (30-minute timeout)
- **File Processing Pipeline**: Sequential processing of Excel files with validation and error handling

### Testing Strategy
- Unit tests using Spring Boot Test framework
- Integration tests for API endpoints
- Test files located in `src/test/java/` mirroring main package structure
- Test resources in `src/test/resources/`
- Example: `ConfigReaderTest.java` for configuration validation
- Mock objects for external dependencies in unit tests
- Test coverage should aim for 80%+ for critical business logic

### Git Workflow
- **Branching Strategy**: Feature branches from `main`, pull requests for merging
- **Commit Conventions**:
  - Imperative mood: "Add feature" not "Added feature"
  - Prefix with scope when relevant: "api: add new endpoint", "auth: fix login bug"
  - Maximum 72 characters for subject line
  - Separate subject from body with blank line when adding details
- **Pull Requests**: Required for all changes, minimum 1 review
- **Versioning**: Semantic versioning (MAJOR.MINOR.PATCH)

## Domain Context

### Business Domain
- **Duty Roster Management**: Schedules assigning staff to shifts (上/白/下/夜/乘/休)
- **Attendance Tracking**: Monthly attendance records tracking shift assignments per day
- **Shift Types**:
  - 上/白 (白班) - Day shift
  - 下 (大夜) - Night shift (may span to next day)
  - 夜 (小夜) - Evening shift
  - 乘 (乘) - Special shift that may span 2 days
  - 休 (休息) - Rest day
  - Shifts can span multiple days, requiring special handling

### Key Workflows
1. **Multi-File Processing**: Process multiple duty roster Excel files for different days
2. **Name Extraction**: Dynamically extract employee names from attendance sheet
3. **Schedule Parsing**: Parse duty rosters with shift mapping normalization
4. **Batch Updates**: Collect all updates and apply in single batch operation
5. **Automatic Rest Assignment**: Assign "休" to unassigned staff on each day
6. **File Validation**: Validate Excel file format and content before processing
7. **Result Generation**: Create updated attendance sheets with processing results

### Important Rules
- Files must be processed in day order (sorted)
- "乘" and "下" shifts span multiple days
- Unassigned staff automatically marked as "休" (rest)
- Month boundary validation prevents out-of-range updates
- Shift mapping normalization (e.g., "小夜" → "上") for consistency
- Employee names must match between duty roster and attendance sheet

## Important Constraints
- **Memory Constraints**: Excel files processed entirely in memory (suitable for typical attendance sheets, not massive datasets)
- **Java 8 Compatibility**: Code must run on Java 8 (Spring Boot 2.7.18 requirement)
- **File Format**: Input files must be valid Excel format (.xlsx)
- **Shift Mapping**: Hardcoded shift type mappings in `AppProperties` (e.g., "小夜" → "上")
- **Port Configuration**: Application runs on port 18881 with custom context path
- **Session Management**: 30-minute session timeout for security
- **File Size Limits**: Processing limited by available memory

## External Dependencies
- **Google Gemini**: Referenced in project name but integration details not yet visible in current codebase
- **Excel Files**: User-provided Excel files as input (duty rosters and attendance templates)
- **No Database**: Currently uses in-memory processing with no persistent database layer
- **No External APIs**: No integration with external services (HttpClient included but not actively used in current codebase)
- **Static Resources**: CSS, JavaScript files for web interface

## API Endpoints
- **GET** `/app-secret-path-a7b3c9d8/` - Main web interface (requires authentication)
- **GET** `/app-secret-path-a7b3c9d8/login` - Login page (public)
- **POST** `/app-secret-path-a7b3c9d8/login` - Login authentication (public)
- **POST** `/app-secret-path-a7b3c9d8/logout` - Logout (requires authentication)
- **POST** `/app-secret-path-a7b3c9d8/process-roster` - Process duty roster and attendance files (requires authentication)
- **GET** `/app-secret-path-a7b3c9d8/download/{transactionId}` - Download processed attendance file (requires authentication)
- Uses transaction ID pattern for file download with memory cache cleanup

## Security
- **Authentication**: Session-based authentication with username/password
- **Default Credentials**: username `admin`, password `admin` (configurable in `application.yml`)
- **Session Timeout**: 30 minutes of inactivity
- **Protected Resources**: All endpoints except `/login`, `/css/**`, `/js/**`, `/images/**`
- **Password Storage**: Plaintext with NoOpPasswordEncoder (acceptable for internal tool behind firewall)
- **Public Access**: Login page and static resources (CSS, JS) are accessible without authentication

## Configuration Properties
- `app.test-property` - Test configuration property
- `app.allowed-names` - List of allowed names (for validation/filtering)
- `spring.security.user.name` - Login username (default: `admin`)
- `spring.security.user.password` - Login password (default: `admin`)
- `server.servlet.session.timeout` - Session timeout duration (default: `30m`)
- Shift mappings hardcoded in `AppProperties` class (not externalized)

## Development Environment
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code with Java extensions
- **JDK**: Java 8 (required for compatibility)
- **Maven**: Version 3.6.0 or higher
- **Git**: Version control system
- **Testing**: JUnit 4/5 for unit tests, Spring Test for integration tests
- **Build Commands**: `mvn clean install`, `mvn spring-boot:run`

## File Structure
- `src/main/java/` - Main application source code
- `src/main/resources/` - Configuration files, static resources, templates
- `src/test/java/` - Test source code
- `src/test/resources/` - Test resources
- `target/` - Compiled classes and packaged artifacts (Maven output)
- `openspec/` - OpenSpec documentation and change proposals
- `pom.xml` - Maven build configuration and dependencies
