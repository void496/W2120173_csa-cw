package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.ErrorMessage;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.SmartCampusStore;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    @GET
    public List<Sensor> getSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = SmartCampusStore.getAllSensors();

        if (type == null || type.trim().isEmpty()) {
            return sensors;
        }

        type = type.trim();
        List<Sensor> filteredSensors = new ArrayList<>();
        for (Sensor sensor : sensors) {
            if (sensor.getType() != null && type.equalsIgnoreCase(sensor.getType())) {
                filteredSensors.add(sensor);
            }
        }

        return filteredSensors;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        if (sensor == null) {
            return badRequest("Sensor details are required.");
        }

        String sensorId = sensor.getId();
        if (sensorId != null) {
            sensorId = sensorId.trim();
        }

        String type = sensor.getType();
        if (type != null) {
            type = type.trim();
        }

        if (type == null || type.isEmpty()) {
            return badRequest("type is required.");
        }

        sensor.setType(type);

        String roomId = sensor.getRoomId();
        if (roomId == null || roomId.trim().isEmpty()) {
            return badRequest("roomId is required.");
        }

        roomId = roomId.trim();
        sensor.setRoomId(roomId);

        if (!SmartCampusStore.roomExists(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("Room with id " + sensor.getRoomId() + " was not found.");
        }

        if (sensorId == null || sensorId.isEmpty()) {
            sensor.setId(SmartCampusStore.generateSensorId());
        } else {
            sensor.setId(sensorId);
        }

        if (SmartCampusStore.sensorExists(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorMessage("Sensor with id " + sensor.getId() + " already exists.", 409, "/api/v1"))
                    .build();
        }

        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        } else {
            sensor.setStatus(sensor.getStatus().trim());
        }

        SmartCampusStore.addSensor(sensor);

        URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(location).entity(sensor).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorMessage(message, 400, "/api/v1"))
                .build();
    }
}
