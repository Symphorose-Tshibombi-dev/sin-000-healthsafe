# HealthSafe

Hospital ward status and emergency staffing, built as a small set of independent Java services
(Javalin + Maven) that talk to each other over HTTP and, for two flows, through an ActiveMQ broker.

Every class lives in one flat package, `co.wethinkcode.healthsafe`. Each service folder is its own
Maven project, with no parent pom.

## What each service does

| Service | Folder | Port | Role |
|---|---|---|---|
| ingestion-service | `ingestion-service/` | 7030 | Reads and cleans the messy `wards-outdated.csv`, then serves the clean wards at `GET /wards` |
| ward-service | `ward-service/` | 7031 | Loads the clean wards from ingestion-service and serves wards and departments |
| alert-level-service | `alert-level-service/` | 7032 | Keeps the hospital Emergency Status (0 to 8, where 8 is Code Blue) |
| staffing-service | `staffing-service/` | 7033 | Works out how many doctors must be on call for a ward, using ward-service and alert-level-service |
| equipment-alert-service | `equipment-alert-service/` | 7034 | Receives equipment failure alerts through an ActiveMQ queue with guaranteed delivery |
| common | `common/` | none | The ActiveMQ broker setup (ports 61616 and 8161) and notes |

```
                 GET /wards                  GET /wards/{id}
ingestion  <-----------------  ward-service  <-----------------  staffing-service
(7030)                          (7031)                           (7033)
                                                                    |  GET /alert-level
                                                                    v
                                                              alert-level-service
                                                              (7032)

equipment-alert-service (7034)  <== equipment-failure-queue ==>  ActiveMQ broker (61616)
```

## Status

| Stage | State |
|---|---|
| 1. Ingestion | Done and tested |
| 2. REST services (ward, alert-level, staffing) | Done and tested, and run together end to end |
| 3. Topic `staffing-events-topic` | In progress: staffing-service announces each schedule through a publisher interface; the ActiveMQ publisher and the ward-service subscriber are still to do |
| 4. Queue `equipment-failure-queue` | The consumer side (equipment-alert-service) is built and tested; the ward-service publisher is still to do. Alerts can be sent today through `POST /equipment-alerts` |

## Requirements

- Java 17 or newer (`java -version`)
- Maven 3.8 or newer (`mvn -version`)
- Docker, or Podman, for the message broker (only equipment-alert-service and the later stages need it)
- A terminal for each running service. You need 5 terminals plus one to run the demo calls.

## 1. Start the message broker

The broker is ActiveMQ in a container. Pick the section that matches your computer.

### Linux, with Docker

```
cd common
docker compose up -d
docker compose ps
```

### Linux, with Podman

Older Podman versions (such as 3.4) have no `compose` command, so start the container directly:

```
podman run -d --name activemq -p 61616:61616 -p 8161:8161 docker.io/apache/activemq-classic:5.18.3
podman ps
```

Next time, `podman start activemq` starts the same container again. If `podman run` says the name is
already in use, run `podman start activemq` instead.

### Windows, with Docker Desktop

1. Install Docker Desktop and start it. Wait until it says it is running.
2. Open PowerShell in the project folder:

```
cd common
docker compose up -d
docker compose ps
```

If `docker compose` is not found, use this instead:

```
docker run -d --name activemq -p 61616:61616 -p 8161:8161 apache/activemq-classic:5.18.3
```

### Check it works

Open http://localhost:8161 in a browser and log in with `admin` / `admin`. The ActiveMQ page means the
broker is ready.

To stop it: `docker compose down` (or `docker stop activemq` / `podman stop activemq`).

## 2. Build every service

Run this once from the project root. The Maven "overlapping classes" warnings are normal.

**Linux:**

```
for s in ingestion-service ward-service alert-level-service staffing-service equipment-alert-service; do (cd $s && mvn -q package -DskipTests); done
```

**Windows (PowerShell):**

```
foreach ($s in 'ingestion-service','ward-service','alert-level-service','staffing-service','equipment-alert-service') { Push-Location $s; mvn -q package -DskipTests; Pop-Location }
```

## 3. Start the services

One terminal per service, in this order. They stay running, so leave each window open.
Run each command from the project root.

| Order | Linux | Windows (PowerShell) |
|---|---|---|
| 1 | `cd ingestion-service && java -jar target/ingestion-service.jar` | `cd ingestion-service; java -jar target\ingestion-service.jar` |
| 2 | `cd ward-service && java -jar target/ward-service.jar` | `cd ward-service; java -jar target\ward-service.jar` |
| 3 | `cd alert-level-service && java -jar target/alert-level-service.jar` | `cd alert-level-service; java -jar target\alert-level-service.jar` |
| 4 | `cd staffing-service && java -jar target/staffing-service.jar` | `cd staffing-service; java -jar target\staffing-service.jar` |
| 5 | `cd equipment-alert-service && java -jar target/equipment-alert-service.jar` | `cd equipment-alert-service; java -jar target\equipment-alert-service.jar` |

- ward-service must start **after** ingestion-service. It loads its wards at startup and stops with a clear
  message if it cannot reach ingestion-service. When it works you see `Loaded 17 wards from ingestion-service`.
- equipment-alert-service needs the broker from step 1, and stops with a message if it is not running.
- staffing-service makes no calls at startup, so it starts even when the others are down.

## 4. Demo

Use a sixth terminal. On Windows, type **`curl.exe`**, not `curl`, because plain `curl` in PowerShell is a
different command.

### a. Everything is up

**Linux:**

```
for p in 7030 7031 7032 7033 7034; do curl -s http://localhost:$p/health; echo; done
```

**Windows:**

```
7030..7034 | ForEach-Object { curl.exe -s http://localhost:$_/health; "" }
```

Each one prints `OK`.

### b. Wards and departments (ward-service)

```
curl http://localhost:7031/wards/W-01
curl http://localhost:7031/departments
curl -i http://localhost:7031/wards/W-99
```

- W-01 is East Wing, Cardiology, 3 beds.
- The departments come back once each.
- W-99 gives `404` and `{"error":"Ward W-99 not found"}`.

### c. The on-call schedule changes with the Emergency Status

```
curl http://localhost:7033/schedule/W-01
```

At level 0 it shows `"mode":"NORMAL"` and 1 doctor. Now raise the alert level to 8 (Code Blue).

**Linux:**

```
curl -X PUT -H "Content-Type: application/json" -d '{"level":8}' http://localhost:7032/alert-level
```

**Windows:**

```
Invoke-RestMethod -Method Put -Uri http://localhost:7032/alert-level -ContentType "application/json" -Body '{"level":8}'
```

Ask for the schedule again:

```
curl http://localhost:7033/schedule/W-01
```

It now shows `"mode":"CODE BLUE"` and 4 doctors. A level outside 0 to 8 is rejected with `400` and the old level
stays.

### d. Errors are handled, not crashes

```
curl -i http://localhost:7033/schedule/W-99
```

An unknown ward gives `404`. Now press **Ctrl+C** in the alert-level-service terminal and ask again:

```
curl -i http://localhost:7033/schedule/W-01
```

staffing-service answers `503` and names `alert-level-service`, and it keeps running. Start alert-level-service
again and the schedule works again.

### e. Equipment alerts through the queue (needs the broker)

**Linux:**

```
curl -i -X POST http://localhost:7034/equipment-alerts -H "Content-Type: application/json" \
  -d '{"equipmentId":"VENT-12","wardId":"W-05","description":"Ventilator lost power","severity":"CRITICAL"}'
curl http://localhost:7034/equipment-alerts
```

**Windows:**

```
Invoke-RestMethod -Method Post -Uri http://localhost:7034/equipment-alerts -ContentType "application/json" -Body '{"equipmentId":"VENT-12","wardId":"W-05","description":"Ventilator lost power","severity":"CRITICAL"}'
Invoke-RestMethod -Uri http://localhost:7034/equipment-alerts
```

- The first call answers `202` with `QUEUED`. The service log shows `PROCESSED equipment alert`.
- The second call lists the alerts that were processed.
- A bad alert (for example `"severity":"PANIC"`) answers `400` and nothing is queued.

**Guaranteed delivery:** stop equipment-alert-service with Ctrl+C. In the broker page (http://localhost:8161,
`admin` / `admin`) open **Queues**, then `equipment-failure-queue`, then **Send To**, paste the JSON body above,
and tick **Persistent**. The queue shows 1 pending message. Start equipment-alert-service again and it processes
the waiting message on startup.

### Stop everything

Press **Ctrl+C** in each service terminal, then stop the broker (see step 1).

## Run the tests

One service:

```
cd ward-service
mvn test
```

Every service, from the project root:

```
for s in ingestion-service ward-service alert-level-service staffing-service equipment-alert-service; do (cd $s && mvn test); done
```

The equipment-alert-service queue tests start their own in-memory broker, so they need no Docker.

## Why a queue for equipment alerts and a topic for staffing

| | Topic (`staffing-events-topic`) | Queue (`equipment-failure-queue`) |
|---|---|---|
| Who gets a message | Every subscriber gets a copy | Exactly one consumer gets it |
| Meaning | "This changed, FYI" | "Someone must act on this" |
| Consumer is down | A non-durable subscriber misses it | The message waits on the broker |
| Losing one is | Fine, the next update corrects it | Not acceptable |

## Assumptions and trade-offs

The task did not fix these rules, so they are our choices:

- **Doctors on call:** Emergency Status 0 to 2 gives 1 doctor, 3 to 5 gives 2, 6 to 7 gives 3, and 8 (Code Blue)
  gives 4. An ICU ward always gets one extra doctor.
- **The alert level lives in memory.** After a restart it goes back to 0.
- **No authentication.** Anyone who can reach alert-level-service can change the Emergency Status.
- **No retry.** One slow or failed answer from another service gives an immediate `503` from staffing-service.
- **A ward with a missing department** would show an empty department in the schedule.
- Processed equipment alerts are kept in memory and reset on restart.

This is an educational prototype, not a live clinical system.

## Troubleshooting

| What you see | What to do |
|---|---|
| `Cannot start ward-service: Could not reach ingestion-service` | Start ingestion-service first and check `curl http://localhost:7030/health` |
| equipment-alert-service stops with a broker message | Start the broker (step 1) and check http://localhost:8161 |
| `Address already in use` | A service is already running on that port. Stop it, or find it with `lsof -i :7031` (Linux) or `netstat -ano \| findstr 7031` (Windows) |
| `unrecognized command podman compose` | Your Podman is too old for `compose`. Use the `podman run` command in step 1 |
| `curl` prints strange output in PowerShell | Use `curl.exe` instead of `curl` |
| `cd` fails with a path like `C:UsersUser...` | You are in a Linux terminal. Use Linux paths such as `~/ElectivesProject/...` |
| `mvn test` reports `Tests run: 0` | Maven is using an old test runner. Check the pom has `maven-surefire-plugin` version 3.2.5 |

## Project structure

```
sin-000-healthsafe/
├── README.md
├── ingestion-service/          (7030)
├── ward-service/               (7031)
├── alert-level-service/        (7032)
├── staffing-service/           (7033)
├── equipment-alert-service/    (7034)
└── common/                     (broker: docker-compose.yml)
```

Each service has its own `README.md` with its endpoints and tests.