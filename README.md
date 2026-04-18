# Healthcare Appointment and Patient Record Manager
**Team E12 | OOAD Mini Project**

| Name | USN | Primary Responsibility |
|------|-----|------------------------|
| Jayanth Reddy | PES1UG23CS264 | Appointments + Facade + Command Patterns |
| K Sailakshmi Srinivas | PES1UG23CS271 | Prescriptions + Prototype + Flyweight + Chain of Responsibility |
| Karthikeya Thotamsetty | PES1UG23CS287 | Admin + Proxy + Adapter + Iterator + Interpreter |
| Kireeti Reddy P | PES1UG23CS307 | Patients/Auth + Singleton + Factory + Builder + DB Layer |

---

## How to Build & Run

```bash
bash build.sh
```

Requires: Java 17+ (OpenJDK works fine). No framework — plain Java packages only.
The H2 in-memory DB is initialized automatically on each run.

---

## Teacher Demo (Java Code + Output)

Run:

```bash
bash build.sh
```

Then show these Java code sections:

1. Authentication + 5-attempt lockout:
    - `healthcare/controller/AuthController.java`
2. Teacher demo flow and printed evidence:
    - `healthcare/Main.java`
3. Role permissions in model classes:
    - `healthcare/model/Patient.java`
    - `healthcare/model/Receptionist.java`
    - `healthcare/model/Clinician.java`
    - `healthcare/model/Pharmacist.java`
    - `healthcare/model/ClinicAdmin.java`

What the console output now demonstrates:

1. Role permission matrix checks from Java `hasPermission(...)`
2. Login lockout after 5 wrong password attempts
3. Blocked login while lock timer is active
4. Prescription lifecycle evidence before and after issue action

---

## Architecture — MVC + Design Patterns

```
com.healthcare/
├── model/             ← M (Domain entities from class diagram)
│   ├── enums/
│   └── dto/
├── repository/        ← Data access layer (DB via JDBC)
├── service/           ← Business logic (Service interfaces + impls)
├── controller/        ← C (MVC Controllers, one per domain)
├── view/              ← V (ConsoleView — all display logic here)
├── db/                ← DatabaseConnection (Singleton) + SchemaInitializer
└── pattern/
    ├── creational/
    ├── structural/
    └── behavioral/
```

---

## Design Patterns — Full Map

### Creational (Kireeti + Sailakshmi)
| Pattern | Class | Purpose |
|---------|-------|---------|
| **Singleton** | `db/DatabaseConnection.java` | One DB connection instance JVM-wide |
| **Builder** | `model/Patient.java` (inner `Builder`) | Construct Patient with optional fields cleanly |
| **Factory** | `pattern/creational/UserFactory.java` | Create User subclass by Role without exposing constructors |
| **Prototype** | `pattern/creational/PrescriptionPrototype.java` | Clone a repeat/template prescription |

### Structural (Karthikeya + Sailakshmi)
| Pattern | Class | Purpose |
|---------|-------|---------|
| **Facade** | `pattern/structural/ClinicFacade.java` | Single entry-point into patient+appt+rx subsystems |
| **Proxy** | `pattern/structural/PatientServiceProxy.java` | Role-based access control wrapper on PatientService |
| **Adapter** | `pattern/structural/EHRAdapter.java` | Bridge our Prescription model to legacy HL7 EHR system |
| **Flyweight** | `pattern/structural/DrugFlyweight.java` | Shared Drug object pool — avoids duplication across thousands of prescription items |

### Behavioral (Jayanth + Karthikeya + Sailakshmi)
| Pattern | Class | Purpose |
|---------|-------|---------|
| **Command** | `pattern/behavioral/NotifyCommand.java`, `CancelAppointmentCommand.java` | Encapsulate actions (notify/cancel) with undo support |
| **Chain of Responsibility** | `AllergyCheckerHandler` → `DrugInteractionHandler` | Pipeline conflict checks on prescriptions |
| **Iterator** | `pattern/behavioral/MedicalHistoryIterator.java` | Traverse patient medical records without exposing List |
| **Interpreter** | `EmailSearchExpression`, `AllergySearchExpression` | Parse search DSL: `email:x` / `allergy:y` |

---

## State Machine Diagrams (implemented in model classes)

| Class | States | Methods |
|-------|--------|---------|
| `Appointment` | PENDING → CONFIRMED → COMPLETED / CANCELLED | `confirm()`, `complete()`, `cancel()`, `reschedule()` |
| `Prescription` | DRAFT → ISSUED → DISPENSED / VOID | `issue()`, `dispense()`, `voidPrescription()` |
| `MedicalRecord` | CREATED → UPDATED → ARCHIVED | `addNote()`, `archive()` |

---

## Activity Diagrams → Controller Methods

| Activity | Controller Method |
|----------|------------------|
| Authenticate User | `AuthController.login()` |
| Register Patient | `PatientController.register()` |
| Schedule Appointment | `AppointmentController.scheduleAppointment()` |
| Create Prescription | `PrescriptionController.createPrescription()` |

---

## Database (H2 — no setup needed)

Tables: `users`, `patients`, `staff`, `appointments`, `medical_records`,
`medical_record_notes`, `drugs`, `prescriptions`, `prescription_items`, `audit_log`

All foreign key relationships mirror the class diagram associations.
