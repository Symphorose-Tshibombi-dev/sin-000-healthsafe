# StaffingServiceApp

## Overview

Provides on-call schedules for doctors based on the ward and current Emergency Status.

Part of the [HealthSafe](../README.md) project. This is an independent Maven module with no parent POM.

## REST Integration

This service calls:

- `ward-service` (`../ward-service`) to validate the ward.
- `alert-level-service` (`../alert-level-service`) to retrieve the current Emergency Status before computing a schedule.

See [Integration contracts](../README.md#integration-contracts) in the root README for the endpoint specifications.

## Endpoints

| Call | Result |
|---|---|
| `GET /health` | `OK` |
| `GET /schedule/{wardId}` | Returns the schedule for the specified ward (`200 OK`) |

Example response:

```json
{
  "wardId": "W-01",
  "department": "Cardiology",
  "alertLevel": 8,
  "mode": "CODE BLUE",
  "doctorsOnCall": 4
}
```

## Error Handling

| Situation | Response |
|---|---|
| The ward does not exist | `404 Not Found` with an error message |
| `ward-service` or `alert-level-service` is unavailable, takes longer than 3 seconds to respond, returns an error, or sends unreadable data | `503 Service Unavailable`, identifying the service that failed |

`staffing-service` makes no calls to other services during startup. It can therefore start even when the other services are unavailable.

## How Doctors Are Counted

These rules are project assumptions because the project README does not specify the hospital's staffing rules.

| Alert level | Mode | Doctors on call |
|---|---|---:|
| 0–2 | `NORMAL` | 1 |
| 3–5 | `ELEVATED` | 2 |
| 6–7 | `HIGH` | 3 |
| 8 | `CODE BLUE` | 4 |

An ICU ward always receives one additional doctor.

## Project Structure

```text
staffing-service/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── co/
                └── wethinkcode/
                    └── healthsafe/
                        ├── StaffingServiceApp.java
                        └── mq/
                            └── MqConfig.java
```

## Build

Run the following command from the `staffing-service` directory:

```bash
mvn package
```

## Run

Start `ward-service` and `alert-level-service` first, then run:

```bash
java -jar target/staffing-service.jar
```

The service listens on port `7033`.

The addresses of the other services are configured in:

`src/main/resources/service.properties`

## Tests

Run the tests from the `staffing-service` directory:

```bash
mvn test
```

To check a running service manually:

```bash
curl http://localhost:7033/health
curl http://localhost:7033/schedule/W-01
```

## Known Limitations

- The doctor counts are project assumptions, not confirmed hospital staffing rules.
- There is no retry mechanism. A single slow response from another service results in an immediate `503 Service Unavailable`.
- A ward without a department may appear in the schedule with an empty department field.

## Message Queue (MQ)

This service publishes staffing events to the ActiveMQ topic `staffing-events-topic`.

The broker URL and topic name are configured in the common `co.wethinkcode.healthsafe.mq.MqConfig` class within this module. See [`common/`](../common/) for related project configuration.

When `GET /schedule/{wardId}` computes a schedule that differs from the last schedule broadcast for that ward, it publishes a JSON message to `staffing-events-topic`.

Example message:

```json
{
  "wardId": "W-01",
  "department": "Cardiology",
  "alertLevel": 5,
  "mode": "ELEVATED",
  "doctorsOnCall": 2
}
```

Publishing is best-effort. If the broker is unavailable, the REST response remains unaffected, and the next read attempts to publish again.

Messages are non-persistent because a newer schedule can replace the previous one. Consumers that need the latest state should retrieve a fresh schedule through the REST endpoint.
