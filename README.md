# Trip Processor
A Spring Boot app that processes card taps and generates trip file. 
## How it works
1. **Upload** a `taps.csv` through the REST API. It's saved and recorded as `UPLOADED`.
2. Run **`import-taps`** command to read the uploaded files and stores their taps in PostgreSQL.
3. Run **`process-taps`** command to process the stored taps into trips.
4. **Download** a day's `trips.csv` from the REST API.

## Table of contents

1. [Prerequisites](#1-prerequisites)
2. [Running and testing the solution](#2-running-and-testing-the-solution)
3. [Configuration](#3-configuration)
4. [Assumptions & Limitations](#4-assumptions-and-limitations)
5. [How to add new rule](#5-how-to-add-new-rule)
6. [Run automated tests](#6-run-tests)
---

## 1. Prerequisites

| Tool | Why you need it |
|---|---|
| **JDK 21** | The project targets Java 21 (Spring Boot 4) |
| **Maven 3.9+** | Builds and runs the project |
| **Docker Desktop** | Runs PostgreSQL and the app via Docker Compose |

---

## 2. Running and testing the solution
### Step 1: Start the database and the API

```bash
docker compose up --build -d
```
verify that the API is running by checking the health endpoint http://localhost:8080/actuator/health

### Step 2: Upload file.
Use swagger UI to upload file http://localhost:8080/swagger-ui/index.html
![Swagger UI](/docs/swagger.png)
Note : You need to login in as admin. Please see [Configuration](#3-configuration)
Use the `/tap-files` endpoint to upload the sample file given in [/sample/taps.csv](/sample/taps.csv).

You can use `/tap-files/{fileId}` endpoint to see the status of the file. 

### Step 3: Import file.
```bash
docker compose run --rm app --spring.profiles.active=cli import-taps
```

### Step 4: Process Taps.
```bash
docker compose run --rm app --spring.profiles.active=cli process-taps
```
### Step 5: Download CSV
Use the swagger UI to download the csv by supplying the date (use 2026-10-20 if you have used the sample file). 

## 3. Configuration

HTTP Basic authentication with two roles:

| Role | Can | Default username | Default password | Env vars to override |
|---|---|---|---|---|
| ADMIN | everything: upload files, check their status, download trips.csv | `admin` | `admin123` | `ADMIN_USERNAME`, `ADMIN_PASSWORD` |
| USER | download trips.csv | `user` | `user123` | `USER_USERNAME`, `USER_PASSWORD` |

Other settings in `application.yml`:

| Setting | Default | Meaning |
|---|---|---|
| `app.fare.base-fare` | `1.45` | Base fare added to every trip  |
| `app.processing.cards-per-chunk` | `1000` | Cards  processed per transaction by `process-taps` |
| `app.upload.dir` | `uploads` (env `UPLOAD_DIR`) | Where uploaded files are saved for `import-taps`. `/data/uploads`  |
| `app.upload.batch-size` | `1000` | Taps inserted per database batch by `import-taps` |

Database connection details must match `compose.yaml`.

## 4. Assumptions and limitations

- **Fare Calculation**
  Every fare follows one formula:
```
fare = |v(from) - v(to)| + base fare
```
The **base fare** is the same for the whole application. Each stop has a
**distance value** `v` which gets added through the database seeder. 
Example :

| Stop | v | Fares |
|---|---|---|
| Stop1 | 0.00 | Stop1-Stop2: 1.80 + 1.45 = $3.25 |
| Stop2 | 1.80 | Stop2-Stop3: 4.05 + 1.45 = $5.50 |
| Stop3 | 5.85 | Stop1-Stop3: 5.85 + 1.45 = $7.30 |


- **TESTS** I didn't get time to write integration test and I have only added test for the critical part to save time.
- Passengers must tap on and off the same bus. Each bus always serves the same stops.
- Tap IDs are unique.
- You will be running the commands in sequence.
- If a user miss the tap off in the morning and board the bus without tap on in the evening and just tap off then 
  Morning trip will be counted as INCOMPLETE and the evening tap off will be marked as orphan tap off.
- The tap data is uploaded before the processing starts.
- **No duplicate-file check.** The same file can be uploaded twice. Its taps are only
  stored once, and the second upload reports 0 taps stored.
- **Bad rows.** Rows that can't be read are not stored. They are just logged.

---

## 5. How to add new rule

Each rule is a class implementing `TripRule`. `TripMatcher` gets every rule from
Spring in `@Order` and uses the first one that applies to a pair.

### Steps

1. **Create a class** in the `rule` package that implements `TripRule`.
2. **Annotate it** `@Component` and give it an `@Order` .
3. **Implement** `appliesTo(pair)`  and `apply(pair, result)` .

### Example: new rule


```java
@Component
@Order(0)
public class NewRule implements TripRule {

    private final FareService fareService;

    public NewRule(FareService fareService) {
        this.fareService = fareService;
    }

    @Override
    public boolean appliesTo(TapPair pair) {
        return true;
    }

    @Override
    public void apply(TapPair pair, MatchResult result) {
        // your logic
    }
}
```
## 6. Run tests
```bash
mvn test
```