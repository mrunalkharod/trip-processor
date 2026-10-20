# Trip Processor
A Spring Boot app that processes card taps and generates trip file. 
## How it works
1. **Upload** a `taps.csv` through the REST API. It's saved and recorded as `UPLOADED`.
2. Run **`import-taps`** command to read the uploaded files and stores their taps in PostgreSQL.
3. Run **`process-taps`** command to process the stored taps into trips.
4. **Download** a day's `trips.csv` from the REST API.

## Table of contents

1. [Prerequisites](#1-prerequisites)

---

## 1. Prerequisites

| Tool | Why you need it |
|---|---|
| **JDK 21** | The project targets Java 21 (Spring Boot 4) |
| **Maven 3.9+** | Builds and runs the project |
| **Docker Desktop** | Runs PostgreSQL and the app via Docker Compose |

---
