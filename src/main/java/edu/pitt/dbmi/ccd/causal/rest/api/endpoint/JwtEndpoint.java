/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.endpoint;

import edu.pitt.dbmi.ccd.causal.rest.api.dto.JwtDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.service.JwtEndpointService;
import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
@Path("/jwt")
public class JwtEndpoint {

    private final JwtEndpointService jwtEndpointService;

    @Autowired
    public JwtEndpoint(JwtEndpointService jwtEndpointService) {
        this.jwtEndpointService = jwtEndpointService;
    }

    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Response getJwt(@HeaderParam("Authorization") String authString) {
        JwtDTO jwtDTO = jwtEndpointService.generateJwt(authString);
        return Response.ok(jwtDTO).build();
    }
}
