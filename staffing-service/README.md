# StaffingServiceApp

## Overview

Provides on-call schedules for doctors based on ward and status.

Part of the [HealthSafe](../README.md) project. Independent Maven module, no
parent pom.

MQ: this service publishes to the ActiveMQ topic `staffing-events-topic` — see [`../common/`](../common). Broker URL and topic name come from the common `co.wethinkcode.healthsafe.mq.MqConfig` class alongside it in this module.

REST: calls `ward-service` (`../ward-service`) to validate the ward and
`alert-level-service` (`../alert-level-service`) to read the current Emergency
Status before computing a schedule — see [Integration contracts](../README.md#integration-contracts)
in the root README for the endpoint shapes.

## Endpoints

| Call | Result |
|---|---|
| `GET /health` | `OK` |
| `GET /schedule/{wardId}` | The schedule for that ward (200) |

Example answer:

```
{"wardId":"W-01","department":"Cardiology","alertLevel":8,"mode":"CODE BLUE","doctorsOnCall":4}

## When something goes wrong

| Situation | Answer |
|---|---|
| The ward does not exist | 404 with an error message |
| ward-service or alert-level-service is down, too slow (over 3 seconds), answers with an error, or sends data we cannot read | 503 naming the service that failed |

staffing-service has no calls at startup, so it starts even when the other services are down.

## How the doctors are counted

These rules are our own assumption, because the project README does not fix them.

| Alert level | Mode | Doctors on call |
|---|---|---|
| 0 to 2 | NORMAL | 1 |
| 3 to 5 | ELEVATED | 2 |
| 6 to 7 | HIGH | 3 |
| 8 | CODE BLUE | 4 |

An ICU ward always gets one extra doctor.

## Project structure

```
staffing-service/
├── pom.xml
└── src/main/java/co/wethinkcode/healthsafe/
    ├── StaffingServiceApp.java
    └── mq/
        └── MqConfig.java
```

## Build

```
mvn package
```

## Run

Start ward-service and alert-level-service first, then:

```
java -jar target/staffing-service.jar
```

Listens on port `7033`. The addresses of the other services are in
`src/main/resources/service.properties`.

Listens on port `7033`.


## Test

```
mvn test
```
To check a running service by hand:
```

curl http://localhost:7033/health   # -> OK
curl http://localhost:7033/schedule/W-01
```

To add real tests, add JUnit 5 + the Surefire plugin to `pom.xml`, put tests under
`src/test/java/co/wethinkcode/healthsafe/`, and run `mvn test`.

## Known limitations

- The doctor counts are an assumption, not a rule from the hospital.
- There is no retry: one slow answer from another service gives an immediate 503.
- A ward with no department would show an empty department in the schedule.
