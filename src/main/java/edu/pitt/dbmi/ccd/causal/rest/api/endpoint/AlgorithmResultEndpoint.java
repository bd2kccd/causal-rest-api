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
import edu.pitt.dbmi.ccd.causal.rest.api.dto.AlgorithmResultDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.service.AlgorithmResultEndpointService;
import java.io.IOException;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
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
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
@Component
@PermitAll
@Path("/usr/{username}/results/algorithm")
public class AlgorithmResultEndpoint {
    
    private final AlgorithmResultEndpointService algorithmResultEndpointService;
    
    @Autowired
    public AlgorithmResultEndpoint(AlgorithmResultEndpointService algorithmResultEndpointService) {
        this.algorithmResultEndpointService = algorithmResultEndpointService;
    }
    
    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response listAlgorithmResultFiles(@PathParam("username") String username) throws IOException {
        List<AlgorithmResultDTO> algorithmResultDTOs = algorithmResultEndpointService.listAlgorithmResults(username);
        GenericEntity<List<AlgorithmResultDTO>> entity = new GenericEntity<List<AlgorithmResultDTO>>(algorithmResultDTOs) {
        };

        return Response.ok(entity).build();
    }

}
