# EquipmentAlertServiceApp

## Overview

Uses a Queue to guarantee delivery of critical medical equipment failure alerts.

Part of the [HealthSafe](../README.md) project — its alerting service.
Independent Maven module, no parent pom.

Mechanism: ActiveMQ Queue (guaranteed delivery)

Queue: `equipment-failure-queue`

- Producer: `ward-service` (`../ward-service`) — publishes when it detects an
  equipment failure on one of its wards.
- Consumer: this service.

Broker URL and queue name are shared via a common `co.wethinkcode.healthsafe.mq.MqConfig`
class (`BROKER_URL`, `QUEUE`). It's identical in every participating service's own
source tree — each service here is an independent Maven project with no shared
parent pom, so the common package is duplicated rather than imported from one place.

A queue (not a topic) is used deliberately: each failure alert must be delivered to
exactly one consumer and processed at least once, even if this service is briefly
down — unlike the broadcast `staffing-events-topic` in [`../common/`](../common),
where every subscriber gets every message.

## Project structure

```
equipment-alert-service/
├── pom.xml
└── src/
    ├── main/java/co/wethinkcode/healthsafe/
    │   ├── EquipmentAlertServiceApp.java   REST endpoints + startup wiring
    │   ├── EquipmentAlertService.java      validate + publish; remembers processed alerts
    │   ├── EquipmentAlertValidator.java    the validation rules
    │   ├── EquipmentAlert.java / Severity.java / ProcessedAlert.java
    │   ├── AlertPublisher.java             interface (so tests can fake the broker)
    │   ├── JmsAlertPublisher.java          puts a persistent JSON message on the queue
    │   ├── EquipmentAlertConsumer.java     takes alerts off the queue (transacted)
    │   └── mq/MqConfig.java
    └── test/java/co/wethinkcode/healthsafe/
        ├── EquipmentAlertValidatorTest, EquipmentAlertServiceTest   (unit)
        ├── EquipmentAlertServiceAppTest                             (REST endpoints)
        └── EquipmentAlertQueueTest                                  (real in-memory broker)
```

## Setup

Requirements: Java 17+, Maven 3.8+, Docker (for the broker).

```
cd ../common && docker compose up -d          # ActiveMQ on tcp://localhost:61616, console http://localhost:8161
cd ../equipment-alert-service
mvn package
java -jar target/equipment-alert-service.jar  # port 7034
```

The service exits at startup with a clear message if the broker is not running.

## Endpoints

| Call | Result |
|---|---|
| `GET /health` | `OK` |
| `POST /equipment-alerts` | Validates and puts the alert on the queue. **202** `{"status":"QUEUED","equipmentId":"VENT-12"}` |
| `GET /equipment-alerts` | Alerts the consumer has finished processing (200, JSON array) |

Request body (also the format of every queue message, so `ward-service` can publish the same JSON):

```json
{ "equipmentId": "VENT-12", "wardId": "W-05", "description": "Ventilator lost power", "severity": "CRITICAL" }
```

`severity` is `LOW`, `MEDIUM`, `HIGH` or `CRITICAL`. `description` is at most 500 characters.

| Status | When |
|---|---|
| 202 | Accepted and handed to the broker |
| 400 | Body is not JSON, a field is missing/blank, or severity is unknown. `{"error": "wardId is required"}`. Nothing is queued. |
| 503 | Broker unreachable. `{"error": "Alert was NOT queued ..."}`. The caller must retry; we never pretend it was accepted. |

## Why a queue here, and a topic for alert levels

| | Topic (`staffing-events-topic`) | Queue (`equipment-failure-queue`) |
|---|---|---|
| Who gets a message | **Every** subscriber gets a copy | **Exactly one** consumer gets it |
| Message use | "This changed, FYI": any service may care | "Someone must act on this": work to be done |
| Consumer is down | Non-durable subscribers simply miss it | Message waits on the broker until a consumer is back |
| Losing one is... | Fine, the next update corrects it | Not acceptable |

An equipment failure is a task, not a broadcast. If two instances of this service run, each alert must
be handled once, not twice, and a failure raised while the service restarts must not disappear. A queue
gives both. An emergency level change is the opposite: several services (staffing, wards, a dashboard)
each want their own copy, and only the latest value matters, so that belongs on a topic.

How "guaranteed" is achieved here:

1. The publisher sends **persistent** messages, so the broker keeps them until consumed.
2. The consumer uses a **transacted session**. The broker only deletes a message when we `commit()`.
3. If processing fails we `rollback()` and the broker redelivers (3 retries, 1 s apart).
4. After the retries, the broker moves the message to the dead-letter queue `ActiveMQ.DLQ`, so one
   bad message cannot block the ones behind it.
5. Redelivery means **at least once**, so the consumer skips JMS message ids it has already finished.

## Test

```
mvn test
```

| Test class | Covers |
|---|---|
| `EquipmentAlertValidatorTest` | every validation rule and its error message |
| `EquipmentAlertServiceTest` | valid alert published, invalid alert never published, publish failure surfaces |
| `EquipmentAlertServiceAppTest` | 202, 400 (missing field, bad JSON, unknown severity), 503, `GET` list, health |
| `EquipmentAlertQueueTest` | alert flows through a real broker; alerts wait while the consumer is down; two consumers never share an alert; malformed message goes to the DLQ and the next good one still processes; unreachable broker |

The queue tests start an in-memory ActiveMQ broker, so they need no Docker.

## Demo (end to end)

With the broker and the service running:

```
# 1. valid alert -> 202
curl -i -X POST http://localhost:7034/equipment-alerts -H "Content-Type: application/json" \
  -d '{"equipmentId":"VENT-12","wardId":"W-05","description":"Ventilator lost power","severity":"CRITICAL"}'

# 2. the service log shows: PROCESSED equipment alert: EquipmentAlert[equipmentId=VENT-12, ...]
# 3. it now appears in the processed list
curl http://localhost:7034/equipment-alerts

# 4. invalid alert -> 400, nothing queued
curl -i -X POST http://localhost:7034/equipment-alerts -H "Content-Type: application/json" \
  -d '{"equipmentId":"VENT-12","wardId":"W-05","description":"x","severity":"PANIC"}'

# 5. guaranteed delivery: stop the service (Ctrl+C), open http://localhost:8161 (admin/admin) ->
#    Queues -> equipment-failure-queue -> "Send To", paste the JSON body above, tick Persistent.
#    The queue now shows 1 pending message. Start the service again: it processes it on startup.
```

## Known limitations

- Educational prototype, **not** a live clinical alerting system.
- Processed alerts and the duplicate-check ids are kept in memory and reset on restart.
- The DLQ is not read by anything. A real system would alert on it.
- No authentication on the endpoint.