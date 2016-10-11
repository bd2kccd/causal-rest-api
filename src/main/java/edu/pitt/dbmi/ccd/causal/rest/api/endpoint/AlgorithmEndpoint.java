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
import edu.pitt.dbmi.ccd.causal.rest.api.dto.AlgorithmDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.service.AlgorithmEndpointService;
import java.io.IOException;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu) We need this uid in the URI since JWT
 * compares the uid
 */
@Component
@PermitAll
@Path("/{uid}/algorithms")
public class AlgorithmEndpoint {

    private final AlgorithmEndpointService algorithmEndpointService;

    @Autowired
    public AlgorithmEndpoint(AlgorithmEndpointService algorithmEndpointService) {
        this.algorithmEndpointService = algorithmEndpointService;
    }

    /**
     * List all the available algorithms
     *
     * @return 200 with a list of available algorithms
     * @throws IOException
     */
    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response listAlgorithmResultFiles() throws IOException {
        List<AlgorithmDTO> algorithmDTOs = algorithmEndpointService.listAlgorithms();
        GenericEntity<List<AlgorithmDTO>> entity = new GenericEntity<List<AlgorithmDTO>>(algorithmDTOs) {
        };

        return Response.ok(entity).build();
    }
}
