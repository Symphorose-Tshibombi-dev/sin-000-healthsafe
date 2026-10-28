# AlertLevelServiceApp

## Overview

Tracks the hospital Emergency Status (0–8, where 8 = full Code Blue).

Part of the [HealthSafe](../README.md) project. This is an independent Maven module with no parent POM.

REST: Called by `staffing-service` (`../staffing-service`) to read the current status when computing on-call schedules. See [Integration contracts](../README.md#integration-contracts) in the root README for the endpoint specifications.

## Project structure

```text
alert-level-service/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── co/
                └── wethinkcode/
                    └── healthsafe/
                        └── AlertLevelServiceApp.java
```

## Build

```bash
mvn package
```

## Run

```bash
java -jar target/alert-level-service.jar
```

The service listens on port `7032`.

## Endpoints

| Call | Result |
|---|---|
| `GET /health` | `OK` |
| `GET /alert-level` | `{"level": 0}` — returns the current Emergency Status |
| `PUT /alert-level` with `{"level": 5}` | Stores the level and returns `200` |

A level outside the range 0–8, a body that is not valid JSON, or a missing `level` returns **400 Bad Request**. The previous level remains unchanged.

## Known limitations

- The level is stored in memory and resets to `0` when the service restarts.
- Anyone can change the level because authentication is not implemented.

See the **"Harden Stage 2 services"** issue.

## Tests

Run the tests from this module's directory:

```bash
mvn test
```

## Manual checks

With the service running, use:

```bash
curl http://localhost:7032/health
curl http://localhost:7032/alert-level
```

To update the Emergency Status:

```bash
curl -X PUT http://localhost:7032/alert-level \
  -H "Content-Type: application/json" \
  -d '{"level": 5}'
```

## Message Queue (MQ)

Every successful `PUT /alert-level` request that actually changes the level publishes a message to the `alert-level-topic` topic.

Example message:

```json
{
  "previousLevel": 2,
  "level": 5,
  "changedAt": "..."
}
```

Because this is a topic, every active subscriber receives a copy of the message. Messages are non-persistent, so a service that misses an event should call `GET /alert-level` to retrieve the current status.

If ActiveMQ is unavailable, the `PUT` request still succeeds and the problem is logged.

## Maven module note

This module has its own `pom.xml` and no parent POM. Run Maven commands from inside the `alert-level-service` directory rather than using `-pl alert-level-service` from the repository root, unless a root Maven reactor is configured.
