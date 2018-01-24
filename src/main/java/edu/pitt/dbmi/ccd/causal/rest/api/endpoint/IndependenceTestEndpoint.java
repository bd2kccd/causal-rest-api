/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.endpoint;

import edu.pitt.dbmi.ccd.causal.rest.api.Role;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.IndependenceTestDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.service.IndependenceTestEndpointService;
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
 * @author Zhou Yuan <zhy19@pitt.edu>
 */
@Component
@PermitAll
@Path("/{uid}")
public class IndependenceTestEndpoint {
    private final IndependenceTestEndpointService independenceTestEndpointService;

    @Autowired
    public IndependenceTestEndpoint(IndependenceTestEndpointService independenceTestEndpointService) {
        this.independenceTestEndpointService = independenceTestEndpointService;
    }
    
    /**
     * List all the available tests
     *
     * @return 200 with a list of available scores
     */
    @GET
    @Path("/tests")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response listAllIndependenceTests() {
        List<IndependenceTestDTO> independenceTestDTOs = independenceTestEndpointService.listAllIndependenceTests();
        GenericEntity<List<IndependenceTestDTO>> entity = new GenericEntity<List<IndependenceTestDTO>>(independenceTestDTOs) {
        };

        return Response.ok(entity).build();
    }
    
    /**
     * List all the available tests that work with the given data type 
     *
     * @param dataType
     * @return 200 with a list of available scores
     */
    @GET
    @Path("/tests/{dataType}")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response listIndependenceTests(@PathParam("dataType") String dataType) {
        List<IndependenceTestDTO> independenceTestDTOs = independenceTestEndpointService.listIndependenceTests(dataType);
        GenericEntity<List<IndependenceTestDTO>> entity = new GenericEntity<List<IndependenceTestDTO>>(independenceTestDTOs) {
        };

        return Response.ok(entity).build();
    }
}
