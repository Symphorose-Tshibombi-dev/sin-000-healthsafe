# AlertLevelServiceApp

## Overview

Tracks the hospital Emergency Status (0-8, 8 = full Code Blue).

Part of the [HealthSafe](../README.md) project. Independent Maven module, no
parent pom.

REST: called by `staffing-service` (`../staffing-service`) to read the current
status when computing on-call schedules — see [Integration contracts](../README.md#integration-contracts)
in the root README for the endpoint shapes.

## Project structure

```
alert-level-service/
├── pom.xml
└── src/main/java/co/wethinkcode/healthsafe/AlertLevelServiceApp.java
```

## Build

```
mvn package
```

## Run

```
java -jar target/alert-level-service.jar
```

Listens on port `7032`.

## Endpoints

| Call | Result |
|---|---|
| `GET /health` | `OK` |
| `GET /alert-level` | `{"level": 0}`, the current Emergency Status |
| `PUT /alert-level` with `{"level": 5}` | Stores the level (200) |

A level outside 0 to 8, a body that isn't JSON, or a missing level answers **400**, and the old level stays.

## Known limitations

- The level is kept in memory, so it resets to 0 when the service restarts.
- Anyone can change the level (no authentication).

See the "Harden Stage 2 services" issue.

## Test

```
mvn test
```

To check a running service by hand:

```
curl.exe http://localhost:7032/health
curl.exe http://localhost:7032/alert-level
```