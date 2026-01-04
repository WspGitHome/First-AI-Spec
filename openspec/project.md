# Project Context

## Purpose
A Spring Boot application that automates attendance tracking and duty roster management. The system processes Excel-based duty schedules and attendance records, automatically updating attendance sheets based on parsed shift assignments. It handles complex shift scheduling logic including multi-day shifts and automatic "rest day" assignment for unassigned staff.

## Tech Stack
- **Framework**: Spring Boot 2.7.18
- **Language**: Java 8
- **Build Tool**: Maven
- **Utilities**:
  - Hutool 5.8.26 (Java utility library)
  - Apache POI 5.2.3 (Excel processing)
  - Apache Commons Lang3 3.20.0
  - Apache HttpClient 4.5.14
- **Testing**: Spring Boot Starter Test

## Project Conventions

### Code Style
- **Package Structure**: Standard Spring Boot layered architecture
  - `controller/` - REST API endpoints
  - `service/` - Business logic layer
  - `model/` - Data transfer objects and domain models
  - `config/` - Configuration and properties
- **Naming Conventions**:
  - Classes: PascalCase (e.g., `OrchestrationService`)
  - Methods: camelCase (e.g., `processFiles`)
  - Chinese comments and log messages are acceptable (domain-specific)
- **Language**: Code in English, comments and logs can include Chinese for domain clarity

### Architecture Patterns
- **Layered Architecture**: Controller → Service → Model
- **Service Orchestration**: Complex workflows orchestrated through dedicated services (e.g., `OrchestrationService`)
- **In-Memory Processing**: Excel files processed in memory without intermediate disk writes
- **Configuration Management**: Externalized configuration via `application.yml` and `@ConfigurationProperties`
- **Dependency Injection**: Spring's `@Autowired` for loose coupling
- **RESTful API**: Stateless operations with multipart file upload/download

### Testing Strategy
- Unit tests using Spring Boot Test framework
- Test files located in `src/test/java/` mirroring main package structure
- Test resources in `src/test/resources/`
- Example: `ConfigReaderTest.java` for configuration validation

### Git Workflow
- Not currently defined (to be documented)
- Commit conventions: TBD

## Domain Context

### Business Domain
- **Duty Roster Management**: Schedules assigning staff to shifts (上/白/下/夜/乘/休)
- **Attendance Tracking**: Monthly attendance records tracking shift assignments per day
- **Shift Types**:
  - 上/白 (白班)
  - 下 (大夜)
  - 夜 (小夜)
  - 乘 (乘, may span 2 days)
  - 休 (休息)
  - 下 can span to next day

### Key Workflows
1. **Multi-File Processing**: Process multiple duty roster Excel files for different days
2. **Name Extraction**: Dynamically extract employee names from attendance sheet
3. **Schedule Parsing**: Parse duty rosters with shift mapping normalization
4. **Batch Updates**: Collect all updates and apply in single batch operation
5. **Automatic Rest Assignment**: Assign "休" to unassigned staff on each day

### Important Rules
- Files must be processed in day order (sorted)
- "乘" and "下" shifts span multiple days
- Unassigned staff automatically marked as "休" (rest)
- Month boundary validation prevents out-of-range updates

## Important Constraints
- **Memory Constraints**: Excel files processed entirely in memory (suitable for typical attendance sheets, not massive datasets)
- **Java 8 Compatibility**: Code must run on Java 8 (Spring Boot 2.7.18 requirement)
- **File Format**: Input files must be valid Excel format (.xlsx)
- **Shift Mapping**: Hardcoded shift type mappings in `AppProperties` (e.g., "小夜" → "上")
- **Port Configuration**: Application runs on port 18881 with custom context path

## External Dependencies
- **Google Gemini**: Referenced in project name but integration details not yet visible in current codebase
- **Excel Files**: User-provided Excel files as input (duty rosters and attendance templates)
- **No Database**: Currently uses in-memory processing with no persistent database layer
- **No External APIs**: No integration with external services (HttpClient included but not actively used in current code)

## API Endpoints
- **POST** `/app-secret-path-a7b3c9d8/process-roster` - Process duty roster and attendance files
- **GET** `/app-secret-path-a7b3c9d8/download/{transactionId}` - Download processed attendance file
- Uses transaction ID pattern for file download with memory cache cleanup

## Configuration Properties
- `app.test-property` - Test configuration property
- `app.allowed-names` - List of allowed names (for validation/filtering)
- Shift mappings hardcoded in `AppProperties` class (not externalized)
