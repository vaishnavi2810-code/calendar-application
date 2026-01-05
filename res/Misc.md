# Calendar Application - Architecture Documentation

## Overview

This is a Java-based calendar management system that supports multiple calendars with independent timezones, recurring events, and both command-line and graphical user interfaces. The application is designed to handle complex scheduling scenarios including timezone conversions, series editing, and bulk operations.

---

## System Architecture

The application follows a **layered MVC (Model-View-Controller) architecture with an additional Service layer**. This separation ensures that business logic remains independent of the user interface, making the system maintainable and extensible.

```
┌─────────────────────────────────────────────────────┐
│                     VIEW LAYER                      │
│            (User Interface Components)              │
│         CLI Input / GUI Forms & Dialogs             │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│                  CONTROLLER LAYER                   │
│           (Coordinates User Interactions)           │
│         EventController / SimpleGuiController       │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│                    DTO LAYER                        │
│          (Data Transformation & Parsing)            │
│  ┌───────────────────┬───────────────────────┐     │
│  │ CommandParser     │   GuiBuilderService   │     │
│  │   Service         │                       │     │
│  │ (CLI text → DTO)  │  (GUI forms → DTO)    │     │
│  └───────────────────┴───────────────────────┘     │
│            Both produce structured DTOs             │
└─────────────────────────────────────────────────────┘
                         ↓
                      [DTO]
                         ↓
┌─────────────────────────────────────────────────────┐
│                  CONTROLLER LAYER                   │
│              (Passes DTO to Model)                  │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│                   MODEL LAYER                       │
│      (Business Logic, Domain & Persistence)         │
│                                                     │
│  ┌────────────────────────────────────────────┐    │
│  │  1. CalendarModel (business coordinator)   │    │
│  │     - Receives DTO from controller         │    │
│  │     - Validates operations                 │    │
│  │     - Selects appropriate strategy         │    │
│  └────────────────────────────────────────────┘    │
│                      ↓                              │
│  ┌────────────────────────────────────────────┐    │
│  │  2. Strategy Layer                         │    │
│  │     - CreateEventSingle                    │    │
│  │     - EditSeries                           │    │
│  │     - QueryOnDate                          │    │
│  │     - CopyEventsBetweenDates               │    │
│  │     - Performs specific algorithms         │    │
│  └────────────────────────────────────────────┘    │
│                      ↓                              │
│  ┌────────────────────────────────────────────┐    │
│  │  3. Domain Entities                        │    │
│  │     - Calendar (updated with new events)   │    │
│  │     - Event (created/modified instances)   │    │
│  └────────────────────────────────────────────┘    │
│                      ↓                              │
│  ┌────────────────────────────────────────────┐    │
│  │  4. CalendarCollection (persistence)       │    │
│  │     - Saves updated Calendar               │    │
│  │     - Implements Icalendarcollection       │    │
│  │     - HashMap storage                      │    │
│  └────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────┘
```

---

## Layer Breakdown

### Model Layer (Domain, Business Logic & Storage)
**Package**: `calendar.model`

The Model layer is the heart of the application. It contains everything related to the domain and operates through four internal sub-layers that work together sequentially.

**The Model layer processes requests through these 4 sub-layers:**

#### Sub-layer 1: CalendarModel (Business Coordinator)
- **Role**: Entry point for all operations; receives DTOs from the controller
- **Responsibilities**:
    - Receives and validates incoming DTOs
    - Manages the "active calendar" concept
    - Selects appropriate strategy based on operation type
    - Coordinates the flow through the remaining sub-layers
    - Completes operation (Controller must query Model to access results)

#### Sub-layer 2: Strategy Layer
- **Role**: Executes specific algorithms for different operation types
- **Strategy Types**:
    - **Create strategies**: `CreateEventSingle`, `CreateTimedRecurringFor`, `CreateAllDaySingle`, etc.
    - **Edit strategies**: `EditSingle`, `EditSeries`, `EditForward`
    - **Query strategies**: `PrintOnDate`, `PrintInRange`, `ShowStatusAt`
    - **Copy strategies**: `CopySingleEvent`, `CopyEventsOnDate`, `CopyEventsBetweenDates`
    - **Export strategies**: `GoogleCsvExporter`, `IcalExporter`
- **Responsibilities**:
    - Implements the specific algorithm selected by CalendarModel
    - Creates or modifies Event objects
    - Validates business rules specific to the operation
    - Returns updated Calendar object with modified events

#### Sub-layer 3: Domain Entities
- **Role**: Core business objects that represent the domain
- **Components**:
    - **`Calendar`**: Represents a complete calendar with name, timezone, and collection of events
    - **`Event`**: Represents a single event occurrence with all properties (subject, times, location, description, series ID)
- **Characteristics**:
    - Both are immutable (all fields final)
    - Built using Builder pattern
    - Events use `ZonedDateTime` for timezone awareness
    - Event equality based on subject + start + end times
- **Responsibilities**:
    - Define the structure and properties of calendars and events
    - Ensure data integrity through immutability
    - Provide domain-level validation through builders

#### Sub-layer 4: CalendarCollection (Persistence)
- **Role**: Handles storage and retrieval of calendars
- **Implementation**: `CalendarCollection` implements `Icalendarcollection` interface
- **Storage mechanism**: In-memory HashMap (key: calendar name, value: Calendar object)
- **Responsibilities**:
    - Save updated Calendar objects after operations
    - Retrieve Calendar objects by name
    - Check calendar existence
    - Delete calendars
    - Provide list of all calendar names
    - Abstract storage mechanism behind interface

---

### Key characteristics of the Model layer:
- All domain objects are immutable (fields are final and cannot be changed after creation)
- Events use `ZonedDateTime` for timezone-aware scheduling
- Recurring events share a common `seriesId` to link occurrences together
- Event equality is based on subject + start time + end time to prevent duplicates
- Storage is abstracted behind the `Icalendarcollection` interface

---

### What it does:
- Receives DTOs from the controller and orchestrates operations
- Executes business logic through appropriate strategies
- Manages domain entities (Calendar, Event)
- Persists changes through CalendarCollection
- Stores updated state - Controller must call Model methods to access updated Calendar objects

---

### Four responsibilities organized in four sub-layers:
1. **Coordination**: CalendarModel orchestrates the flow
2. **Algorithm execution**: Strategies implement specific operations
3. **Domain modeling**: Calendar and Event define the domain structure
4. **Persistence**: CalendarCollection handles storage
---

## View Layer (Presentation)
**Packages**: `calendar.view`, `calendar.input`

The View layer is responsible for presenting information to users and capturing their input. It contains zero business logic.

**CLI View**:
- Displays text output to the console
- Formats query results as readable text
- Reads commands from standard input or files

**GUI View**:
- Swing-based graphical interface with a calendar grid showing days of the month
- Multiple dialog boxes for user input (creating/editing events, managing calendars, searching)
- Event list panel showing all events for the selected date
- Navigation controls for moving between months
- Dropdown selector for switching between calendars

**What it does**:
- Displays data to users in a readable format
- Captures user input through text commands or graphical forms
- Delegates all user actions to the Controller layer
- Updates the display when data changes

**What it does NOT do**:
- Perform any calculations or validations
- Access the Model directly (goes through Controller)
- Contain any business rules or logic
- Make decisions about how operations should be performed

---

## Controller Layer (Coordination)
**Package**: `calendar.controller`

The Controller layer acts as the bridge between the View and the Model layers. It translates user interface actions into business operations.

**Components**:
- **`EventController`**: Handles command-line mode interactions. It runs a read-eval-print loop (REPL) that reads text commands, parses them into structured data, executes them via the model layer, and displays results.
- **`SimpleGuiController`**: Handles GUI mode interactions. It wires up event listeners for buttons and dialogs, extracts data from GUI forms, converts it to appropriate data transfer objects (DTOs), invokes model operations, and updates the view with results.

**What it does**:
- Listens for user actions (button clicks, command submissions)
- Extracts data from view components
- Calls appropriate helper classes that converts data received from the view into a DTO which can be passed to the model
- Calls appropriate methods on `CalendarModel`
- Handles results and updates the view
- Manages UI state (current selected date, active calendar)

**What it does NOT do**:
- Contain business logic or validation rules
- Directly modify domain objects
- Access the storage layer directly (uses `CalendarModel`)
- Make decisions about how events should be created or edited

---

## Data Flow Example

Here's how a typical operation flows through the system:

**Creating an Event (GUI mode)**:
1. User fills out CreateEventDialog form
2. User clicks "Create"
3. `SimpleGuiController` extracts form data
4. Controller uses `GuiBuilderService` to create a `CreateEventDto`
5. Controller calls `CalendarModel.createEvent(dto)`
6. `CalendarModel` validates the operation and selects appropriate strategy (e.g., `CreateEventSingle`)
7. Strategy creates new `Event` object with proper timezone
8. Strategy validates against existing events
9. `CalendarModel` merges new event with existing events
10. `CalendarModel` saves updated calendar via `CalendarCollection`
11. Controller updates view to show success message
12. View refreshes event list

**Key observation**: The view knows nothing about how events are created. The model contains all business logic and storage. The controller simply translates between view events and model operations.

---

## Key Concepts

### Immutability
Domain objects (`Calendar`, `Event`) cannot be modified after creation. To "change" a calendar or event, you must create a new instance with the desired properties. This prevents accidental modifications and makes the system more predictable.

### Timezone Awareness
Every calendar has its own timezone. All event times are stored as `ZonedDateTime` which includes timezone information. This allows the system to correctly handle events across different geographic regions and properly convert times when copying events between calendars.

### Recurring Events
Recurring events are represented as multiple separate `Event` objects that share a common `seriesId` (a UUID). This design allows each occurrence to be queried, edited, or deleted independently while still maintaining the relationship between occurrences.

### Edit Scopes
When editing a recurring event, users can choose:
- **Edit Single**: Modifies only the selected occurrence
- **Edit Forward**: Modifies the selected occurrence and all future occurrences
- **Edit Series**: Modifies all occurrences in the entire series

Each scope is handled by a different strategy implementation.

### Strategy Pattern
Different types of operations (creating different event types, editing with different scopes, querying with different criteria) are implemented as separate strategy classes. This makes it easy to add new event types or operation modes without modifying existing code. The `CalendarModel` selects the appropriate strategy based on the operation type.

### Storage Abstraction
While `CalendarCollection` currently stores calendars in memory (HashMap), it implements the `Icalendarcollection` interface. This means the storage mechanism could be swapped out (e.g., to a database or file system) without changing any other code.

---

## Extension Points

The architecture is designed to be easily extensible:

**Adding a new event type**: Create a new strategy class implementing the create interface, register it in the factory, and update the UI to support the new type.

**Adding a new export format**: Implement the export interface with the new format logic and add a case to the export factory.

**Changing storage mechanism**: Create a new class implementing `Icalendarcollection` with a different storage backend (database, file system, cloud), then inject it into `CalendarModel`. No changes to business logic required.

**Adding a new user interface**: Create a new controller that uses the existing `CalendarModel`. The business logic remains unchanged.

---

## Summary

This calendar application demonstrates a well-separated architecture where:

- **The Model layer** contains everything about the domain: what calendars and events are, how operations are performed on them, and where they're stored. This all lives in one package for cohesion.

- **The View layer** handles all user interaction and display without any business logic.

- **The Controller layer** translates between user actions and model operations.

- **The Service layer** provides specialized strategies and helper services that the model coordinator uses.

The key insight is that the **Model layer is a complete, self-contained unit** that handles domain entities, business logic, and storage. This makes it easy to understand where all the core functionality lives, while still maintaining clean separation from the UI through the controller.

# Miscellaneous Documentation

## Design Changes for This Iteration

### Changes Made

| Change | Description | Justification |
|--------|-------------|---------------|
| **Added GUI Interface** | Implemented complete Swing-based graphical user interface with calendar grid, dialogs, and event management | Requirement for this iteration; provides more intuitive user experience compared to CLI |
| **Added `SimpleGuiController`** | New controller specifically for handling GUI events and user interactions | Separates GUI-specific coordination logic from CLI controller; maintains MVC separation |
| **Added GUI Dialog Classes** | Created `CreateEventDialog`, `EditEventDialog`, `CreateCalendarDialog`, `EditCalendarDialog`, `SearchEditEventDialog`, `BulkEditEventDialog` | Provides structured forms for user input in GUI mode; implements dialog interfaces for controller decoupling |
| **Added `GuiBuilderService`** | Service class that builds DTOs from GUI form data | Centralizes DTO construction logic for GUI; mirrors `CommandParserService` functionality for CLI |
| **Added Dialog Interfaces** | Created `IcreateEventDialogData`, `IeditEventDialogData`, `IguiViewCalendar` | Decouples controller from specific Swing implementations; allows testing without GUI dependencies |
| **Added Multi-Property Edit Support** | Enhanced `EditEvent` utility class with new method to handle multiple property changes using a HashMap | CLI only supported single-property edits; GUI allows users to change multiple properties (subject, time, location, description) in one operation for better UX |
| **Added Bulk Edit Functionality** | Implemented search-and-edit feature allowing users to find and edit multiple matching events | Provides efficient way to update multiple events simultaneously; supports mass operations |

### No Major Architectural Changes

The core architecture remains unchanged from the previous iteration:
- Model layer still contains domain objects, business logic (`CalendarModel`), and storage (`CalendarCollection`)
- Strategy pattern implementation unchanged
- Service layer structure maintained
- CLI functionality preserved exactly as before

**Key Point**: We added a new interface (GUI) without modifying the existing business logic, demonstrating the flexibility of our layered architecture.

---

## Feature Status

### ✅ All Features Working

All implemented features are fully functional:

**Calendar Management:**
- ✅ Create new calendars with custom timezones
- ✅ Edit calendar names
- ✅ Edit calendar timezones with validation
- ✅ Switch between multiple calendars
- ✅ Display all calendars in dropdown

**Event Creation:**
- ✅ Single timed events
- ✅ Single all-day events
- ✅ Recurring timed events (N times)
- ✅ Recurring timed events (until date)
- ✅ Recurring all-day events (N times)
- ✅ Recurring all-day events (until date)

**Event Editing:**
- ✅ Edit single occurrence
- ✅ Edit this and future occurrences
- ✅ Edit entire series
- ✅ Multi-property editing (subject, time, location, description)
- ✅ Bulk edit via search - This search will work based on subject, start date and time provided by the user. All the events (irrespective of whether it is a single event or a series of events) that have the same subject and start date + time will get updated together as per the requirement. If it is a series of events all the events in that series will get updated as per the requirement. 

**Event Querying:**
- ✅ Query events by date
- ✅ Query events by date range
- ✅ Check availability at specific time
- ✅ Search events by subject and start time

**Event Operations:**
- ✅ Copy single event between calendars
- ✅ Copy all events from one date
- ✅ Copy events between date range (with weekday preservation)
- ✅ Export to CSV (Google Calendar format)
- ✅ Export to iCalendar (.ics format)

**User Interfaces:**
- ✅ CLI mode (interactive)
- ✅ CLI mode (headless/file input)
- ✅ GUI mode (full Swing interface)

**Timezone Features:**
- ✅ Timezone-aware event scheduling
- ✅ Cross-timezone event copying
- ✅ Calendar timezone conversion
- ✅ Proper time display and input handling

---

## Important Clarifications for Grading

### 1. Model vs. Controller Distinction

**Please note the following architectural clarification:**

#### What is the MODEL:
- **`CalendarModel`** (located in `calendar.model` package) is our **Model** class
- It contains the core business logic and coordinates all calendar operations
- It manages the "active calendar" concept
- It validates operations and delegates to strategy implementations
- It coordinates with `CalendarCollection` for persistence

#### What is the CONTROLLER:
- **`EventController`** (CLI) and **`SimpleGuiController`** (GUI) are our **Controller** classes
- They translate user actions into calls to `CalendarModel`
- They do NOT contain business logic
- They only extract data from views and pass it to the model

**Common Confusion**: `CalendarModel` is not the controller because it contains business logic. `CalendarModel` is the **Model** in our MVC architecture - it's the "service" or "business logic" layer that the controllers delegate to.

**Why Controllers Appear to Have Logic**: Our controllers call many methods and coordinate flows, but they do NOT implement business rules. All validation, event creation algorithms, and business decisions happen in `CalendarModel` and the strategy classes.

#### Visual Clarification:

```
User Action in GUI
       ↓
SimpleGuiController ← This is the CONTROLLER
  - Extracts form data
  - Builds DTO
  - Calls model method
       ↓
CalendarModel ← This is the MODEL (business logic)
  - Validates operation
  - Selects strategy
  - Coordinates business logic
  - Manages persistence
       ↓
Strategy Implementation
  - Implements specific algorithm
       ↓
CalendarCollection
  - Handles storage
```

### 2. Copy Events Between Dates - Weekday Preservation Logic

**IMPORTANT**: Our implementation of `copy events between <start> and <end> --target <calendar> to <targetDate>` includes weekday preservation logic.

#### How It Works:

When copying events between a date range:

1. **Identify all events** in the source date range
2. **Sort events** by start time to maintain order
3. **Find the first event's weekday** (e.g., Monday)
4. **Adjust target date** to match the first event's weekday if needed
    - If target date is Wednesday but first event is Monday, adjust to the next Monday
5. **Calculate day offset** between first event and adjusted target date
6. **Apply offset** to all events, maintaining relative positioning

#### Example:

```
Source events:
  - 2024-03-11 (Monday) - Event A
  - 2024-03-12 (Tuesday) - Event B
  - 2024-03-14 (Thursday) - Event C

Copy to: 2024-03-21 (Thursday)

Result:
  - Target date adjusted to 2024-03-18 (Monday) to match first event's weekday
  - Event A copied to 2024-03-25 (Monday)
  - Event B copied to 2024-03-26 (Tuesday)
  - Event C copied to 2024-03-28 (Thursday)
```

**Weekday pattern is preserved** in the copied events.

#### Rationale:

This design was based on Professor Mitra's guidance on Piazza (Post #666) (https://piazza.com/class/mercsdgnsyd2ee/post/666#) which indicated that weekday preservation should be maintained for bulk copy operations to keep recurring patterns intact.

#### Contrast with Other Copy Commands:

- **`copy event <name> on <date> to <targetDate>`**: Copies to exact target date, no weekday adjustment
- **`copy events on <date> to <targetDate>`**: Copies all events from one date to exact target date, no weekday adjustment
- **`copy events between <start> and <end> to <targetDate>`**: Copies with weekday preservation (as described above)

### 3. Edit Series Start Time Behavior

**IMPORTANT**: When editing the start time of an entire event series, the weekday MAY change.

#### How It Works:

When using "Edit Series" to change the start time:

1. **Calculate time offset** between old start time and new start time
2. **Apply same offset** to all events in the series
3. **Weekdays are NOT preserved** - events shift by the time offset

#### Example:

```
Original series (every Monday at 10:00):
  - 2024-03-11 (Monday) 10:00-11:00
  - 2024-03-18 (Monday) 10:00-11:00
  - 2024-03-25 (Monday) 10:00-11:00

Edit series start time to: 2024-03-12 (Tuesday) 10:00

Result:
  - 2024-03-12 (Tuesday) 10:00-11:00  ← Weekday changed!
  - 2024-03-19 (Tuesday) 10:00-11:00  ← All shifted by 1 day
  - 2024-03-26 (Tuesday) 10:00-11:00
```

#### Rationale:

Per the guidance provided on the piazza post  #289 (https://northeastern.instructure.com/courses/225910/external_tools/289), we had freedom to implement series editing logic as we saw fit. Our design treats the time offset as the primary constraint, allowing weekday shifts as a natural consequence of the time change.

This approach:
- Maintains consistent time offsets across all occurrences
- Provides predictable behavior (all events shift by same amount)
- Aligns with the concept that changing a series start time fundamentally changes the series schedule

#### Alternative Interpretation:

Some might expect weekday preservation when editing series. However, our interpretation is that if a user explicitly changes the start date/time of a series, they intend to move the entire series to that new point in time, including any weekday changes that result.

### 4. Test Coverage and Quality Analysis Report

#### 1. Executive Summary

This section outlines the testing strategy and coverage metrics for the Calendar application. The primary objective of the testing phase was to ensure the robustness of the core business logic, data transformation, and command execution layers.

To provide an accurate representation of the application's reliability, automated unit and mutation tests focused on the logical components of the system. The User Interface (View layer) as well parts of the SimpleGuiController that trigger the view were excluded from these specific metrics, as it is tested via manual system testing and integration verification.

---

#### 2. Methodology & Scope

The project utilized two primary tools to assess code quality:

1. **JaCoCo (Java Code Coverage)**: Used to measure the percentage of code lines and logic branches executed during testing.
2. **PIT (Mutation Testing)**: Used to assess the quality of the tests by introducing "mutants" (artificial bugs) to ensure tests fail appropriately when logic is altered.

##### 2.1 Exclusions

The following components were excluded from the automated coverage analysis:

- **Package**: `calendar.view`
- **Class**: `calendar.controller.SimpleGuiController`

**Justification**: These components handle UI rendering, event listening, and framework-specific logic (Swing/AWT) that requires user interaction. Including them in unit test metrics artificially lowers the coverage score without reflecting the actual reliability of the application's business rules.

---

#### 3. Coverage Analysis Results

After filtering out the UI components, the core application logic demonstrates exceptional test coverage.

##### 3.1 Structural Coverage (JaCoCo)

The following metrics represent the structural execution of the code:

| Metric | Covered / Total | Percentage | Interpretation |
|--------|----------------|------------|----------------|
| **Instruction Coverage** | 6,895 / 7,092 | **97.22%** | Nearly all bytecode instructions are executed. |
| **Line Coverage** | 1,621 / 1,671 | **97.01%** | 97% of source code lines are touched by tests. |
| **Method Coverage** | 310 / 318 | **97.48%** | Almost every method is invoked at least once. |
| **Branch Coverage** | 480 / 539 | **89.05%** | High coverage of `if/else` and `switch` decision points. |

##### 3.2 Mutation Coverage (PIT)

While JaCoCo measures execution, PIT measures test quality.

| Metric | Ratio | Percentage | Interpretation |
|--------|-------|------------|----------------|
| **Mutation Coverage** | 494 / 538 | **91.82%** | The tests detected and "killed" 92% of introduced bugs. |
| **Test Strength** | 494 / 526 | **93.91%** | Of the code that was actually executed, the tests verified the logic 94% of the time. |

---
## Summary

**Design**: Minimal changes from previous iteration - added GUI layer without modifying core architecture

**Status**: All features working correctly in both CLI and GUI modes

**Key Points**:
1. `CalendarModel` is the MODEL (not controller)
2. Copy between dates preserves weekdays
3. Edit series allows weekday shifts when start time changes