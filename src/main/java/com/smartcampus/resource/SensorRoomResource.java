package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.ErrorMessage;
import com.smartcampus.model.Room;
import com.smartcampus.store.SmartCampusStore;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    @GET
    public List<Room> getAllRooms() {
        return SmartCampusStore.getAllRooms();
    }

    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        if (room == null) {
            return badRequest("Room details are required.");
        }

        String roomId = room.getId();
        if (roomId != null) {
            roomId = roomId.trim();
        }

        if (roomId == null || roomId.isEmpty()) {
            room.setId(SmartCampusStore.generateRoomId());
        } else {
            room.setId(roomId);
        }

        if (SmartCampusStore.roomExists(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorMessage("Room with id " + room.getId() + " already exists.", 409, "/api/v1"))
                    .build();
        }

        room.setSensorIds(new ArrayList<>());
        SmartCampusStore.addRoom(room);

        URI location = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        return Response.created(location).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = SmartCampusStore.getRoom(roomId);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorMessage("Room not found.", 404, "/api/v1"))
                    .build();
        }

        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = SmartCampusStore.getRoom(roomId);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorMessage("Room not found.", 404, "/api/v1"))
                    .build();
        }

        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room cannot be deleted because it still has sensors assigned.");
        }

        SmartCampusStore.removeRoom(roomId);
        return Response.noContent().build();
    }

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorMessage(message, 400, "/api/v1"))
                .build();
    }
}
