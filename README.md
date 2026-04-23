w2120173-csa_cw
Coursework for Client-Server Architecture Module: Developing a JAX-RS RESTful Service

============================================================================
- OVERVIEW OF API DESIGN -
============================================================================

------------------------------
The API manages 3 entities
------------------------------

Room - id, name, capacity, sensorIds

Sensor - id, type, status, currentValue, roomId

SensorReading - id, timestamp, value

The API uses a simple in-memory design with HashMap and ArrayList. This keeps the
project easy to understand and suitable for university coursework.

------------------------------
Endpoints
------------------------------

GET /api/v1 - returns API metadata

GET /api/v1/rooms - get all rooms

GET /api/v1/rooms/{roomId} - get a specific room by ID

POST /api/v1/rooms - create a new room

DELETE /api/v1/rooms/{roomId} - delete a room only if it has no sensors assigned

GET /api/v1/sensors - get all sensors

GET /api/v1/sensors?type=... - get sensors filtered by type

POST /api/v1/sensors - create a new sensor, but the parent roomId must exist

GET /api/v1/sensors/{sensorId}/readings - get all readings for a sensor

POST /api/v1/sensors/{sensorId}/readings - create a new reading and update the parent
sensor's currentValue

============================================================================
- INSTRUCTIONS TO BUILD PROJECT AND LAUNCH SERVER -
============================================================================

------------------------------
STEP 1
------------------------------

Clone the repository or download the project files.

------------------------------
STEP 2
------------------------------

Make sure Java 11, Maven, and Apache Tomcat 9 are available on your machine.

If you are using Apache NetBeans, configure Tomcat in NetBeans before running the
project.

------------------------------
STEP 3
------------------------------

Open the project folder and build it.

You can do this in one of two ways:

Using terminal:

mvn clean package

Using NetBeans:

Right-click the project
Select Clean and Build

------------------------------
STEP 4
------------------------------

After a successful build, the WAR file will be created here:

target/smart-campus-api.war

Deploy this WAR file to Apache Tomcat.

If you are using NetBeans with Tomcat configured, you can also right-click the project
and select Run.

------------------------------
STEP 5
------------------------------

Once Tomcat starts and the project is deployed, open the API using:

http://localhost:8080/smart-campus-api/api/v1

============================================================================
- SAMPLE CURL COMMANDS -
============================================================================

------------------------------
1. Discovery endpoint
------------------------------

curl -X GET http://localhost:8080/smart-campus-api/api/v1

Expected Response:

{
  "name": "Smart Campus API",
  "version": "v1",
  "contact": "admin@smartcampus.local",
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}

------------------------------
2. Get all rooms
------------------------------

curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms

Expected Response:

[
  {
    "id": "R001",
    "name": "Lab 1",
    "capacity": 40,
    "sensorIds": ["S001"]
  },
  {
    "id": "R002",
    "name": "Lecture Hall A",
    "capacity": 120,
    "sensorIds": ["S002"]
  },
  {
    "id": "R003",
    "name": "Server Room",
    "capacity": 10,
    "sensorIds": ["S003"]
  }
]

------------------------------
3. Create a room
------------------------------

curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms ^
  -H "Content-Type: application/json" ^
  -d "{\"id\":\"R010\",\"name\":\"Innovation Lab\",\"capacity\":35}"

Expected Response:

{
  "id": "R010",
  "name": "Innovation Lab",
  "capacity": 35,
  "sensorIds": []
}

------------------------------
4. Create a sensor successfully
------------------------------

curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors ^
  -H "Content-Type: application/json" ^
  -d "{\"id\":\"S010\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":415.2,\"roomId\":\"R010\"}"

Expected Response:

{
  "id": "S010",
  "type": "CO2",
  "status": "ACTIVE",
  "currentValue": 415.2,
  "roomId": "R010"
}

------------------------------
5. Filter sensors by type
------------------------------

curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=TEMPERATURE"

Expected Response:

[
  {
    "id": "S001",
    "type": "TEMPERATURE",
    "status": "ACTIVE",
    "currentValue": 24.5,
    "roomId": "R001"
  }
]

------------------------------
6. Add a reading for a sensor
------------------------------

curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/S001/readings ^
  -H "Content-Type: application/json" ^
  -d "{\"id\":\"SR010\",\"timestamp\":1776571000,\"value\":28.5}"

Expected Response:

{
  "id": "SR010",
  "timestamp": 1776571000,
  "value": 28.5
}

------------------------------
7. Error case: create sensor with missing room
------------------------------

curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors ^
  -H "Content-Type: application/json" ^
  -d "{\"id\":\"S099\",\"type\":\"TEMPERATURE\",\"status\":\"ACTIVE\",\"currentValue\":20.0,\"roomId\":\"R999\"}"

Expected Response:

{
  "errorMessage": "Room with id R999 was not found.",
  "errorCode": 422,
  "documentation": "/api/v1"
}

------------------------------
8. Error case: add reading while sensor is in maintenance
------------------------------

curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/S002/readings ^
  -H "Content-Type: application/json" ^
  -d "{\"value\":55.0}"

Expected Response:

{
  "errorMessage": "Sensor S002 is currently in maintenance mode.",
  "errorCode": 403,
  "documentation": "/api/v1"
}

------------------------------
9. Error case: delete a room that still has sensors
------------------------------

curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/R001

Expected Response:

{
  "errorMessage": "Room cannot be deleted because it still has sensors assigned.",
  "errorCode": 409,
  "documentation": "/api/v1"
}

============================================================================
- REPORT -
============================================================================

------------------------------
1.1
------------------------------

By default, a JAX-RS resource class follows a request-scoped lifecycle. This means a new
resource instance is normally created for each incoming request, and that instance is then
discarded after the request has been processed. Because of this, per-request data stored
inside the resource object itself is not shared between clients.

However, this does not automatically make shared application data thread-safe. In this
project, the shared data is stored centrally in an in-memory store using HashMap and
ArrayList. Those structures can still be accessed by multiple requests, so write operations
must be handled carefully to avoid race conditions or lost updates. For a simple coursework
project, keeping all shared data in one store class is acceptable and easy to understand.
If stronger concurrency control was needed, synchronized write logic could be added around
the shared collections.

------------------------------
1.2
------------------------------

The provision of hypermedia is considered an important part of advanced RESTful design
because it helps make the API self-descriptive. Instead of relying only on external
documentation, the client can discover useful entry points from the API responses
themselves. In this project, the discovery endpoint returns metadata such as version
information, contact details, and links to the main resource collections.

This benefits client developers because it makes the API easier to navigate and understand.
Compared with static documentation, hypermedia reduces guesswork and helps clients adapt
more easily if the API structure changes later.

------------------------------
2.1
------------------------------

When returning a list of rooms, returning only IDs would reduce the response size and use
less network bandwidth. However, this would force the client to make extra requests to
retrieve each room's full details.

Returning the full room objects increases the response size, but it makes the client side
simpler because the main information is already available in one response. In this project,
returning full room objects is the better choice because it is clearer, easier to test, and
more practical for a coursework API of this size.

------------------------------
2.2
------------------------------

The DELETE operation is idempotent in this implementation because repeated identical
DELETE requests lead to the same final server state.

If a room without sensors is deleted successfully, the room is removed from the system.
If the client sends the same DELETE request again, the API returns 404 Not Found
because the room no longer exists. Even though the response is different, the final state
remains the same, which is why the operation is still idempotent.

If the room still has sensors assigned, the API blocks deletion and returns 409 Conflict.
Repeating that same request gives the same result until the room becomes deletable.

------------------------------
3.1
------------------------------

The @Consumes(MediaType.APPLICATION_JSON) annotation tells JAX-RS that the POST
method only accepts request bodies in JSON format. If a client sends data in a different
format such as text/plain or application/xml, the media type no longer matches what
the resource method expects.

In that case, JAX-RS usually returns 415 Unsupported Media Type and the method is
not executed. This is useful because it prevents the API from trying to process data in a
format it was not designed to handle.

------------------------------
3.2
------------------------------

In this project, filtering was implemented using @QueryParam, for example
/api/v1/sensors?type=CO2. This is generally better than putting the filter directly in the
path such as /api/v1/sensors/type/CO2.

The reason is that query parameters are more suitable for optional filtering and searching
on a collection. The collection is still the same resource, and the client is only asking for
a filtered view of it. Query parameters also make it easier to add more filters later without
changing the basic endpoint structure.

------------------------------
4.1
------------------------------

The Sub-Resource Locator pattern improves modularity by delegating nested resource logic
to a separate class. In this project, SensorResource handles the main sensors collection,
while SensorReadingResource handles the nested readings path for a specific sensor.

This helps manage complexity because each class has a clearer responsibility. It also makes
the code easier to read, maintain, and extend. Compared with placing every nested route in
one large resource class, a dedicated sub-resource class is cleaner and easier to organise.

------------------------------
5.1
------------------------------

HTTP 422 is more semantically accurate than a standard 404 when the issue is a missing
reference inside a valid JSON payload. In this project, the endpoint /api/v1/sensors
exists and the request reaches the correct resource method, so the URI itself is valid.

The real problem is that the roomId inside the JSON body does not refer to an existing
room. This means the server understands the request structure, but it cannot process it
correctly because of invalid data inside the payload. That is why 422 Unprocessable
Entity is a good fit here.

------------------------------
5.2
------------------------------

From a cybersecurity point of view, exposing internal Java stack traces to API consumers is
risky because stack traces reveal technical information about the application. They can show
package names, class names, method names, line numbers, internal file paths, and details
about the libraries being used.

An attacker could use that information to learn how the backend is structured and to look
for weak points more easily. For this reason, the API should return a clean generic 500
error to the client and keep the detailed error information only in the server logs.

------------------------------
5.3
------------------------------

It is advantageous to use JAX-RS filters for cross-cutting concerns like logging because the
same logging logic can be applied automatically to every request and response in one place.
This avoids repeating Logger.info() calls inside every resource method.

Using filters keeps the resource classes more focused on business logic and improves
maintainability. It also gives a more consistent logging approach because all endpoints are
processed in the same way before and after execution.
