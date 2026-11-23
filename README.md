# Telerehab Session Manager

Telerehabilitation & Biofeedback Session Manager, project of the course
*Medical Software Engineering* (Master Degree in Information Engineering for
Digital Medicine, University of Salerno, a.y. 2026/27).

A desktop application for clinicians to design rehabilitation protocols and run
them with simulated physiological telemetry (package `edu.unisa.medsim`),
real-time safety monitoring and clinical filtering.

## Team

| Member | Area |
| --- | --- |
| Raffaele | A. Telemetry & Safety |
| Carmen | B. Exercise Catalog & Clinical Filtering |
| Filippo | C. Protocol Editing & History |
| Christian | D. Session Execution & Monitor |

## Requirements

- JDK 17 or newer (JDK 21 recommended)
- Maven 3.9 or newer

## Build and run

```
mvn test          # compile and run all unit tests
mvn javafx:run    # start the application
```

In VS Code, install the recommended *Extension Pack for Java* and use the
launch configuration "Run Telerehab Session Manager" (main class
`it.unisa.telerehab.app.Launcher`).

## Project structure

```
src/main/java/edu/unisa/medsim/        provided telemetry simulator (refactored in US-07)
src/main/java/it/unisa/telerehab/
    app/          entry points
    ui/           JavaFX controllers (FXML views in src/main/resources)
    model/        domain model
    service/      exercise catalog and protocol management
    command/      undo/redo commands
    session/      session execution and states
    telemetry/    telemetry provider, medsim adapter, safety monitor
    persistence/  save and load (Epic 4)
docs/             architecture, backlog links, sprint reports, presentations
```

## Process artifacts

- Product and sprint backlogs, burndown: see [docs/backlog-links.md](docs/backlog-links.md)
- Software architecture: [docs/architecture.md](docs/architecture.md)
- Sprint reports: `docs/sprint-N/`

## Git workflow

- `main` contains only released sprint increments, tagged `v0.1-sprint1`, `v0.2-sprint2`, `v1.0-final`
- daily work goes to `develop` through Pull Requests reviewed by another member
- one branch per story or task: `feature/US-07-profile-strategy`
- commit messages start with the story id: `US-07: introduce ClinicalProfile interface`
