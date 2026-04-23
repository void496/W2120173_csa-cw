# Smart Campus API Coursework

## 1. Project Overview
This project is a small Smart Campus REST API built for a university coursework submission using JAX-RS. It manages three main resource types:

- rooms
- sensors
- sensor readings

The API uses simple in-memory storage with `HashMap` and `ArrayList`, so the data is reset every time the application restarts. This was a deliberate choice to keep the code easy to understand and suitable for coursework.

The base path for the API is:

```text
http://localhost:8080/smart-campus-api/api/v1
```

## 2. Technologies Used
- Java 11
- JAX-RS
- Maven
- Apache Tomcat 9
- In-memory Java collections: `HashMap` and `ArrayList`

## 3. How to Build
1. Open a terminal in the project folder.
2. Run:

```bash
mvn clean package
```

3. After the build finishes, the generated WAR file will be:

```text
target/smart-campus-api.war
```

## 4. How to Run
1. Build the project with `mvn clean package`.
2. Copy `target/smart-campus-api.war` into the Tomcat `webapps` folder.
3. Start Tomcat.

Windows:

```bat
%CATALINA_HOME%\bin\startup.bat
```

Linux or macOS:

```bash
$CATALINA_HOME/bin/startup.sh
```

4. Wait for Tomcat to deploy the WAR file.
5. Open or test the API using:

```text
http://localhost:8080/smart-campus-api/api/v1
```

Starter data is loaded automatically when the application starts. The sample data includes rooms `R001`, `R002`, `R003` and sensors `S001`, `S002`, `S003`.

## 5. Endpoint Summary
| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/v1` | Discovery endpoint |
| GET | `/api/v1/rooms` | Return all rooms |
| POST | `/api/v1/rooms` | Create a new room |
| GET | `/api/v1/rooms/{roomId}` | Return one room |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room if it has no linked sensors |
| GET | `/api/v1/sensors` | Return all sensors |
| GET | `/api/v1/sensors?type=TEMPERATURE` | Return sensors filtered by type |
| POST | `/api/v1/sensors` | Create a new sensor |
| GET | `/api/v1/sensors/{sensorId}/readings` | Return readings for one sensor |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a reading for one sensor |

## 6. curl Examples
Base URL:

```text
http://localhost:8080/smart-campus-api/api/v1
```

### Discovery request
```bash
curl http://localhost:8080/smart-campus-api/api/v1
```

### Get all rooms
```bash
curl http://localhost:8080/smart-campus-api/api/v1/rooms
```

### Create a room
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "id": "R010",
    "name": "Innovation Lab",
    "capacity": 35
  }'
```

### Get one room
```bash
curl http://localhost:8080/smart-campus-api/api/v1/rooms/R010
```

### Create a sensor successfully
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "S010",
    "type": "CO2",
    "status": "ACTIVE",
    "currentValue": 415.2,
    "roomId": "R010"
  }'
```

### Filter sensors by type
```bash
curl "http://localhost:8080/smart-campus-api/api/v1/sensors?type=TEMPERATURE"
```

### Get readings for a sensor
```bash
curl http://localhost:8080/smart-campus-api/api/v1/sensors/S001/readings
```

### Add a reading successfully
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/S001/readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 25.9
  }'
```

### Error case: create a sensor with a room that does not exist
```bash
curl -i -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "S099",
    "type": "TEMPERATURE",
    "status": "ACTIVE",
    "currentValue": 20.0,
    "roomId": "R999"
  }'
```

This should return `422 Unprocessable Entity`.

### Error case: add a reading to a sensor in maintenance
```bash
curl -i -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/S002/readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 55.0
  }'
```

This should return `403 Forbidden`.

### Error case: try to delete a room that still has sensors
```bash
curl -i -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/R001
```

This should return `409 Conflict`.

## 7. Report Answers

### 7.1 Project design
I designed the API around resources because that fits REST clearly and is easy to explain. The main resources are rooms, sensors, and sensor readings. I used simple resource paths such as `/rooms` and `/sensors`, and the discovery endpoint at `/api/v1` acts as a simple starting point for the API.

### 7.2 Data storage choice
The project uses in-memory `HashMap` and `ArrayList` collections. `HashMap` is useful for storing rooms and sensors by ID because it makes lookup straightforward. `ArrayList` is used for sensor readings because readings are naturally stored as a simple list. This approach is enough for coursework and keeps the implementation easy to follow.

### 7.3 Resource relationships
Each sensor belongs to a room, so `Sensor` contains a `roomId`. Each room also stores a list of `sensorIds` so that linked sensors can be checked when deleting a room. Sensor readings belong to a sensor, which is why the readings are exposed through the nested path `/sensors/{sensorId}/readings`.

### 7.4 Why a sub-resource locator was used
I used a sub-resource locator for sensor readings because readings are a child resource of a sensor. It keeps the code organised and matches the idea that readings should only be accessed through a specific sensor.

### 7.5 Error handling
The API uses custom exceptions and exception mappers to return clear JSON error responses. `RoomNotEmptyException` is used when a room still has sensors linked to it. `LinkedResourceNotFoundException` is used when a sensor is created with a room ID that does not exist. `SensorUnavailableException` is used when a reading is posted to a sensor with status `MAINTENANCE`. A generic mapper is also included for unexpected errors.

### 7.6 JSON responses and status codes
All normal responses and error responses use JSON. For errors, the API returns an `ErrorMessage` object with `errorMessage`, `errorCode`, and `documentation`. Common status codes used in this project are `200 OK`, `201 Created`, `204 No Content`, `403 Forbidden`, `404 Not Found`, `409 Conflict`, `422 Unprocessable Entity`, and `500 Internal Server Error`.

### 7.7 Logging
The project includes a simple logging filter using JAX-RS request and response filters. It logs the incoming HTTP method and URI, and it also logs the outgoing status code. This helps during testing and debugging without making the code complicated.

### 7.8 Limitations
This project is intentionally simple because it is coursework. The data is not permanent, so it is lost when the server restarts. Validation is basic, and the API is designed to demonstrate JAX-RS resources, sub-resources, exception mapping, and request logging rather than production-level features.
