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
package edu.pitt.dbmi.ccd.causal.rest.api.endpoint;

import edu.pitt.dbmi.ccd.causal.rest.api.Role;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.DataFileDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.service.DataFileEndpointService;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * Jun 5, 2016 9:41:01 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Component
@PermitAll
@Path("/usr/{username}/data")
public class DataFileEndpoint {

    private final DataFileEndpointService dataFileEndpointService;

    @Autowired
    public DataFileEndpoint(DataFileEndpointService dataFileEndpointService) {
        this.dataFileEndpointService = dataFileEndpointService;
    }

    @DELETE
    @Path("/id/{id}")
    @RolesAllowed(Role.USER)
    public Response deleteById(@PathParam("username") String username, @PathParam("id") Long id) {
        dataFileEndpointService.deleteByIdAndUsername(id, username);

        return Response.noContent().build();
    }

    @GET
    @Path("/id/{id}")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response findById(@PathParam("username") String username, @PathParam("id") Long id) {
        DataFileDTO dataFileDTO = dataFileEndpointService.findByIdAndUsername(id, username);

        return Response.ok(dataFileDTO).build();
    }

    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response listDataFiles(@PathParam("username") String username) {
        List<DataFileDTO> dataFileDTOs = dataFileEndpointService.listDataFiles(username);
        GenericEntity<List<DataFileDTO>> entity = new GenericEntity<List<DataFileDTO>>(dataFileDTOs) {
        };

        return Response.ok(entity).build();
    }

}
