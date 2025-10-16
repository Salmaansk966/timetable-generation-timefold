# Dynamic Constraint Management System

This document describes the dynamic constraint management system implemented for the Timefold Solver timetable optimization application.

## Overview

The system allows constraints to be enabled/disabled dynamically through the database without requiring application restart. When constraints are toggled in the UI, the solver automatically reloads with the new configuration.

## Key Components

### 1. ConstraintSettings Entity
- **File**: `src/main/java/com/timetable/problem_solver/model/ConstraintSettings.java`
- **Purpose**: Represents constraint settings in the database
- **Fields**:
  - `id`: Primary key
  - `constraintName`: Unique constraint identifier
  - `constraintWeight`: Weight/penalty for the constraint
  - `constraintType`: Type (Hard/Soft)
  - `enableFlag`: Whether the constraint is enabled
  - `description`: Human-readable description

### 2. ConstraintSettingsRepository
- **File**: `src/main/java/com/timetable/problem_solver/repository/ConstraintSettingsRepository.java`
- **Purpose**: Database access for constraint settings
- **Key Methods**:
  - `findByConstraintName()`: Find constraint by name
  - `findAllEnabled()`: Get all enabled constraints
  - `findAllDisabled()`: Get all disabled constraints
  - `isConstraintEnabled()`: Check if constraint is enabled

### 3. TimeTableConstraintProvider
- **File**: `src/main/java/com/timetable/problem_solver/constraints/TimeTableConstraintProvider.java`
- **Purpose**: Dynamic constraint provider that reads from database
- **Key Features**:
  - Reads constraint settings from database on each solve
  - Only applies enabled constraints
  - Uses constraint weights from database

### 4. SolverService
- **File**: `src/main/java/com/timetable/problem_solver/service/SolverService.java`
- **Purpose**: Manages dynamic SolverManager creation and reloading
- **Key Methods**:
  - `getSolverManager()`: Get current solver manager
  - `reloadSolver()`: Reload solver with fresh constraint configuration
  - Thread-safe operations with read/write locks

### 5. SolverController
- **File**: `src/main/java/com/timetable/problem_solver/controller/SolverController.java`
- **Purpose**: REST API for solver operations
- **Endpoints**:
  - `POST /api/solver/solve`: Start solving with current constraints
  - `POST /api/solver/reload`: Reload solver configuration
  - `GET /api/solver/{jobId}`: Get solution status
  - `GET /api/solver/info`: Get solver information

### 6. ConstraintController
- **File**: `src/main/java/com/timetable/problem_solver/controller/ConstraintController.java`
- **Purpose**: REST API for constraint management
- **Endpoints**:
  - `GET /api/constraint`: Get all constraints
  - `PUT /api/constraint/{id}/toggle`: Toggle constraint enabled state
  - `PUT /api/constraint/{id}/weight`: Update constraint weight
  - **Auto-reload**: Automatically triggers solver reload after updates

### 7. ConstraintSettingsInitializationService
- **File**: `src/main/java/com/timetable/problem_solver/service/ConstraintSettingsInitializationService.java`
- **Purpose**: Initializes default constraint settings on startup
- **Features**:
  - Creates default constraints if they don't exist
  - Sets up all available constraints with default values

## Available Constraints

### Hard Constraints (Must never be violated)
1. **teacherConflict**: Prevents teacher double-booking
2. **studentGroupConflict**: Prevents student group double-booking
3. **noFreeTimeslots**: Ensures all lessons are assigned
4. **teacherDailyWorkloadLimit**: Limits teacher daily workload
5. **noBackToBackSameSubject**: Prevents consecutive same subjects

### Soft Constraints (Optimization goals)
1. **teacherNoThreeConsecutive**: Avoids 3 consecutive teacher periods
2. **practicalSubjectConsecutivePeriods**: Ensures practical subjects are consecutive
3. **teacherTimeEfficiency**: Rewards efficient teacher schedules
4. **practicalSubjectsWeekdaysOnly**: Prefers weekdays for practical subjects
5. **onePracticalPerDayPerSection**: Limits practical subjects per day
6. **practicalSubjectTwoDaysPerWeek**: Limits practical subjects to 2 days/week
7. **lowDifficultySubjectWeeklyLimit**: Limits low-difficulty subjects
8. **lowDifficultyOncePerDay**: Limits low-difficulty subjects per day
9. **preferHighPriorityTheory**: Rewards high-priority theory subjects

## Usage Flow

### 1. Application Startup
1. `ConstraintSettingsInitializationService` creates default constraints in database
2. `SolverService` initializes with default constraint configuration

### 2. Constraint Management
1. UI calls `GET /api/constraint` to load constraint list
2. User toggles constraint in UI
3. UI calls `PUT /api/constraint/{id}/toggle` with new state
4. `ConstraintController` updates database
5. `ConstraintController` automatically calls `solverService.reloadSolver()`
6. Solver configuration is reloaded with new constraints

### 3. Solving
1. UI calls `POST /api/solver/solve` with timetable problem
2. `SolverController` gets current `SolverManager` from `SolverService`
3. Solver uses latest constraint configuration from database
4. Solving proceeds with only enabled constraints

## API Examples

### Get All Constraints
```bash
GET /api/constraint
```

### Toggle Constraint
```bash
PUT /api/constraint/1/toggle
Content-Type: application/json

{
  "enabled": false
}
```

### Update Constraint Weight
```bash
PUT /api/constraint/1/weight
Content-Type: application/json

5
```

### Start Solving
```bash
POST /api/solver/solve
Content-Type: application/json

{
  "timeslots": [...],
  "lessons": [...]
}
```

### Reload Solver (Manual)
```bash
POST /api/solver/reload
```

### Get Solver Info
```bash
GET /api/solver/info
```

## Database Schema

```sql
CREATE TABLE constraint_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    constraint_name VARCHAR(255) NOT NULL UNIQUE,
    constraint_weight INT NOT NULL DEFAULT 1,
    constraint_type VARCHAR(50),
    enable_flag BOOLEAN NOT NULL DEFAULT true,
    description TEXT
);
```

## Benefits

1. **No Restart Required**: Constraints can be changed without application restart
2. **Real-time Updates**: Changes take effect immediately for new solve operations
3. **Thread-Safe**: Multiple users can modify constraints safely
4. **Persistent**: Constraint settings are stored in database
5. **Flexible**: Easy to add new constraints or modify existing ones
6. **Production-Safe**: Robust error handling and logging

## Error Handling

- Constraint updates are logged with detailed information
- Solver reload failures don't break constraint updates
- Database errors are properly handled and logged
- Thread-safe operations prevent race conditions

## Performance Considerations

- Constraint settings are loaded fresh for each solve operation
- Solver reload is only triggered when constraints change
- Read/write locks ensure thread safety without blocking
- Database queries are optimized with proper indexing
