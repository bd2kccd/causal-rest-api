/*
 * Copyright (C) 2016 University of Pittsburgh.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package edu.pitt.dbmi.ccd.causal.rest.api.exception.mapper;

import edu.pitt.dbmi.ccd.causal.rest.api.dto.ErrorResponse;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * Jun 5, 2016 10:15:27 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Context
    private HttpServletRequest httpRequest;

    @Override
    public Response toResponse(WebApplicationException exception) {
        Response response = exception.getResponse();
        StatusType statusType = response.getStatusInfo();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setError(statusType.getReasonPhrase());
        errorResponse.setMessage(exception.getMessage());
        errorResponse.setPath(httpRequest.getPathInfo());
        errorResponse.setStatus(statusType.getStatusCode());
        errorResponse.setTimestamp(System.currentTimeMillis());

        return Response.status(statusType).entity(errorResponse).build();
    }

}
