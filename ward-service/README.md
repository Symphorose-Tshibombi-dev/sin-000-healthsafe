# WardServiceApp

## Overview

Provides lists of wards and departments.

Part of the [HealthSafe](../README.md) project. Independent Maven module, no
parent pom.

MQ: this service subscribes to the ActiveMQ topic `staffing-events-topic` — see [`../common/`](../common) — and publishes to the ActiveMQ queue `equipment-failure-queue` when it detects an equipment failure on one of its wards, consumed by [`../equipment-alert-service`](../equipment-alert-service). Broker URL, topic name, and queue name come from the common `co.wethinkcode.healthsafe.mq.MqConfig` class alongside it in this module.

REST: called by `staffing-service` (`../staffing-service`) and `alert-level-service` (`../alert-level-service`) — see [Integration contracts](../README.md#integration-contracts) in the root README for the endpoint shapes.

## Project structure

```
ward-service/
├── pom.xml
└── src/main/java/co/wethinkcode/healthsafe/
    ├── WardServiceApp.java
    └── mq/
        └── MqConfig.java
```

## Build

```
mvn package
```

## Run

```
java -jar target/ward-service.jar
```

Listens on port `7031`.

## Test

No automated tests yet. Manually verify it's up:

```
curl http://localhost:7031/health   # -> OK
```

To add real tests, add JUnit 5 + the Surefire plugin to `pom.xml`, put tests under
`src/test/java/co/wethinkcode/healthsafe/`, and run `mvn test`.

## MQ

- **Queue (publish):** `POST /wards/{id}/equipment-failures` with
  `{"equipmentId":"VENT-12","description":"Lost power","severity":"CRITICAL"}` checks the ward
  (404 if unknown), validates the body (400), and puts a persistent message on
  `equipment-failure-queue` (202). If the broker is down it answers 503 "NOT queued".
- **Topic (subscribe):** subscribes to `staffing-events-topic` and keeps the latest update per ward.
  `GET /wards/{id}/staffing` returns it (404 until one has arrived). The subscription is
  non-durable, so events sent while ward-service is down are missed. The next event replaces them.
- ward-service still starts without the broker; only the MQ features are unavailable.