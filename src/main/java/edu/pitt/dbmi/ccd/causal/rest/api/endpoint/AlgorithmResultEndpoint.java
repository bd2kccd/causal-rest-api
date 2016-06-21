/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.endpoint;

import edu.pitt.dbmi.ccd.causal.rest.api.Role;
import edu.pitt.dbmi.ccd.causal.rest.api.service.AlgorithmResultEndpointService;
import edu.pitt.dbmi.ccd.causal.rest.api.service.DataFileEndpointService;
import java.io.IOException;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author zhy19
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
        List<java.nio.file.Path> algorithmResults = algorithmResultEndpointService.listAlgorithmResults(username);

        System.out.println(algorithmResults);
        
        return Response.ok(algorithmResults).build();
    }
}
