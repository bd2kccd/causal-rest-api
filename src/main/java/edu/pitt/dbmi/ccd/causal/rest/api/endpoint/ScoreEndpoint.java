/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.endpoint;

import edu.pitt.dbmi.ccd.causal.rest.api.Role;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.ScoreDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.service.ScoreEndpointService;
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
 * @author Zhou Yuan <zhy19@pitt.edu>
 */
@Component
@PermitAll
@Path("/{uid}")
public class ScoreEndpoint {
    private final ScoreEndpointService scoreEndpointService;

    @Autowired
    public ScoreEndpoint(ScoreEndpointService scoreEndpointService) {
        this.scoreEndpointService = scoreEndpointService;
    }
    
    /**
     * List all the available scores
     *
     * @return 200 with a list of available scores
     * @throws IOException
     */
    @GET
    @Path("/scores")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response listAllScores() throws IOException {
        List<ScoreDTO> scoreDTOs = scoreEndpointService.listAllScores();
        GenericEntity<List<ScoreDTO>> entity = new GenericEntity<List<ScoreDTO>>(scoreDTOs) {
        };

        return Response.ok(entity).build();
    }

    /**
     * List all the available scores that work with the given data type 
     * 
     * @param dataType
     * @return
     * @throws IOException 
     */
    @GET
    @Path("/scores/{dataType}")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response listScores(@PathParam("dataType") String dataType) throws IOException {
        List<ScoreDTO> scoreDTOs = scoreEndpointService.listScores(dataType);
        GenericEntity<List<ScoreDTO>> entity = new GenericEntity<List<ScoreDTO>>(scoreDTOs) {
        };

        return Response.ok(entity).build();
    }
}
