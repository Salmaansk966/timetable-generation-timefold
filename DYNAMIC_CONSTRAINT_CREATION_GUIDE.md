# Dynamic Constraint Creation Guide

This guide explains how users can dynamically create, modify, and delete custom constraints in the timetable optimization system.

## 🎯 Overview

The system now supports **user-defined constraints** that can be created through the UI without requiring code changes or application restart. Users can:

- ✅ **Create custom constraints** with their own names and logic
- ✅ **Modify constraint properties** (weight, type, description)
- ✅ **Enable/disable constraints** dynamically
- ✅ **Delete custom constraints** (core constraints are protected)
- ✅ **See changes take effect immediately** in the solver

## 🚀 API Endpoints

### 1. Create New Constraint
```http
POST /api/constraint
Content-Type: application/json

{
  "constraintName": "customMorningPreference",
  "constraintWeight": 5,
  "constraintType": "Soft",
  "enableFlag": true,
  "description": "Custom constraint to prefer morning classes"
}
```

**Response:**
```json
{
  "id": 15,
  "constraintName": "customMorningPreference",
  "constraintWeight": 5,
  "constraintType": "Soft",
  "enableFlag": true,
  "description": "Custom constraint to prefer morning classes"
}
```

### 2. Update Existing Constraint
```http
PUT /api/constraint/{id}
Content-Type: application/json

{
  "constraintName": "customMorningPreference",
  "constraintWeight": 10,
  "constraintType": "Soft",
  "enableFlag": true,
  "description": "Updated description for morning preference"
}
```

### 3. Delete Custom Constraint
```http
DELETE /api/constraint/{id}
```

**Response:**
```json
{
  "message": "Constraint deleted successfully",
  "constraintName": "customMorningPreference"
}
```

### 4. Toggle Constraint Enable/Disable
```http
PUT /api/constraint/{id}/toggle
Content-Type: application/json

{
  "enabled": false
}
```

### 5. Update Constraint Weight
```http
PUT /api/constraint/{id}/weight
Content-Type: application/json

5
```

## 📋 Validation Rules

### Constraint Name Rules:
- ✅ Must start with a letter
- ✅ Can contain letters, numbers, and underscores
- ✅ Maximum 50 characters
- ❌ Cannot use reserved system names
- ❌ Must be unique

### Constraint Type:
- ✅ Must be "Hard" or "Soft"
- ✅ Case insensitive

### Constraint Weight:
- ✅ Must be between 1 and 1000
- ✅ Higher weight = higher priority

### Description:
- ✅ Optional
- ✅ Maximum 500 characters

## 🔒 Protected System Constraints

The following constraints are **protected** and cannot be deleted:

- `teacherConflict`
- `studentGroupConflict`
- `noFreeTimeslots`
- `teacherDailyWorkloadLimit`
- `noBackToBackSameSubject`
- `teacherNoThreeConsecutive`
- `practicalSubjectConsecutivePeriods`
- `teacherTimeEfficiency`
- `practicalSubjectsWeekdaysOnly`
- `onePracticalPerDayPerSection`
- `practicalSubjectTwoDaysPerWeek`
- `lowDifficultySubjectWeeklyLimit`
- `lowDifficultyOncePerDay`
- `preferHighPriorityTheory`

## 🎨 Custom Constraint Logic

### How Custom Constraints Work:

1. **Hard Constraints**: Penalize violations (e.g., unassigned lessons)
2. **Soft Constraints**: Reward good solutions (e.g., assigned lessons)

### Example Custom Constraints:

#### Morning Preference (Soft)
```json
{
  "constraintName": "morningPreference",
  "constraintWeight": 3,
  "constraintType": "Soft",
  "enableFlag": true,
  "description": "Rewards scheduling classes in the morning"
}
```

#### Afternoon Avoidance (Hard)
```json
{
  "constraintName": "avoidAfternoon",
  "constraintWeight": 5,
  "constraintType": "Hard",
  "enableFlag": true,
  "description": "Penalizes scheduling classes after 2 PM"
}
```

#### Subject Grouping (Soft)
```json
{
  "constraintName": "groupRelatedSubjects",
  "constraintWeight": 2,
  "constraintType": "Soft",
  "enableFlag": true,
  "description": "Rewards scheduling related subjects on the same day"
}
```

## 🔄 Automatic Solver Reload

**Important**: The system automatically reloads the solver configuration whenever:
- ✅ New constraint is created
- ✅ Constraint is updated
- ✅ Constraint is deleted
- ✅ Constraint is enabled/disabled
- ✅ Constraint weight is changed

This ensures that **changes take effect immediately** for the next solve operation.

## 🎯 Usage Workflow

### 1. Create Custom Constraint
```bash
# User creates a new constraint through UI
POST /api/constraint
{
  "constraintName": "weekendAvoidance",
  "constraintWeight": 4,
  "constraintType": "Soft",
  "enableFlag": true,
  "description": "Avoid scheduling classes on weekends"
}
```

### 2. Verify Constraint Created
```bash
# Check that constraint appears in the list
GET /api/constraint
# Response includes the new constraint
```

### 3. Start Solving
```bash
# Solve with the new constraint active
POST /api/solver/solve
{
  "timeslots": [...],
  "lessons": [...]
}
```

### 4. Monitor Results
```bash
# Check solving progress
GET /api/solver/{jobId}
# The solver will use the new constraint in optimization
```

## 🛠️ Advanced Features

### Constraint Templates
Users can create constraint templates for common scenarios:

```json
{
  "constraintName": "template_morning_preference",
  "constraintWeight": 3,
  "constraintType": "Soft",
  "enableFlag": false,
  "description": "Template: Prefer morning classes (disable by default)"
}
```

### Constraint Groups
Organize constraints by category:

```json
{
  "constraintName": "time_preference_morning",
  "constraintWeight": 3,
  "constraintType": "Soft",
  "enableFlag": true,
  "description": "Time Preference Group: Morning classes"
}
```

## 🚨 Error Handling

### Common Errors:

1. **Duplicate Name**:
   ```json
   HTTP 400 Bad Request
   ```

2. **Invalid Name Format**:
   ```json
   HTTP 400 Bad Request
   "Constraint name must start with a letter"
   ```

3. **Reserved Name**:
   ```json
   HTTP 400 Bad Request
   "Constraint name 'teacherConflict' is reserved"
   ```

4. **Invalid Weight**:
   ```json
   HTTP 400 Bad Request
   "Constraint weight must be 1 or greater"
   ```

5. **Delete Protected Constraint**:
   ```json
   HTTP 400 Bad Request
   "Cannot delete core system constraints"
   ```

## 📊 Performance Considerations

- **Constraint Limit**: Recommended maximum of 50 custom constraints
- **Weight Range**: Keep weights between 1-100 for optimal performance
- **Solver Reload**: Takes ~1-2 seconds, happens automatically
- **Database**: All constraints stored in `constraint_settings` table

## 🎉 Benefits

1. **No Code Changes**: Add constraints without developer intervention
2. **Immediate Effect**: Changes apply to next solve operation
3. **User Control**: Users can experiment with different constraint combinations
4. **Flexible**: Support for both hard and soft constraints
5. **Safe**: Core constraints are protected from deletion
6. **Validated**: Comprehensive validation prevents invalid constraints

## 🔮 Future Enhancements

Potential future features:
- **Constraint Templates**: Pre-built constraint templates
- **Constraint Import/Export**: Share constraint configurations
- **Constraint Analytics**: Track constraint effectiveness
- **Visual Constraint Builder**: Drag-and-drop constraint creation
- **Constraint Scheduling**: Enable/disable constraints by time periods

---

**Ready to create your first custom constraint?** Use the API endpoints above or integrate with your UI to give users the power to customize their timetable optimization! 🚀

