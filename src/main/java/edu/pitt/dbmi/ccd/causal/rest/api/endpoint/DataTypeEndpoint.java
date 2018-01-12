/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.endpoint;

import edu.pitt.dbmi.ccd.causal.rest.api.Role;
import edu.pitt.dbmi.ccd.causal.rest.api.service.DataTypeEndpointService;
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
 * @author Zhou Yuan <zhy19@pitt.edu>
 */
@Component
@PermitAll
@Path("/{uid}")
public class DataTypeEndpoint {
    private final DataTypeEndpointService dataTypeEndpointService;

    @Autowired
    public DataTypeEndpoint(DataTypeEndpointService dataTypeEndpointService) {
        this.dataTypeEndpointService = dataTypeEndpointService;
    }
    
    /**
     * List all the supported data types
     *
     * @return 200 with a list of available algorithms
     */
    @GET
    @Path("/datatypes")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response listAllDataTypes() {
        List<String> dataTypes = dataTypeEndpointService.listDataTypes();
        GenericEntity<List<String>> entity = new GenericEntity<List<String>>(dataTypes) {
        };

        return Response.ok(entity).build();
    }
}
