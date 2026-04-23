package com.smartcampus.exception;

import com.smartcampus.model.ErrorMessage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        LOGGER.log(Level.SEVERE, "Unexpected server error", exception);

        ErrorMessage errorMessage = new ErrorMessage(
                "An unexpected error occurred.",
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "/api/v1"
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorMessage)
                .build();
    }
}
