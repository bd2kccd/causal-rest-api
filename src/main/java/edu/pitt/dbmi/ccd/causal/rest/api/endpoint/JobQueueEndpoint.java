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
import edu.pitt.dbmi.ccd.causal.rest.api.dto.NewJob;
import edu.pitt.dbmi.ccd.causal.rest.api.service.JobQueueEndpointService;
import java.io.IOException;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
@Path("/{username}/jobs")
public class JobQueueEndpoint {

    private final JobQueueEndpointService jobQueueEndpointService;

    @Autowired
    public JobQueueEndpoint(JobQueueEndpointService jobQueueEndpointService) {
        this.jobQueueEndpointService = jobQueueEndpointService;
    }

    /*
     * Adding a new job
     */
    @POST
    @Consumes(APPLICATION_FORM_URLENCODED)
    @RolesAllowed(Role.USER)
    public Response addNewJob(@PathParam("username") String username, @BeanParam NewJob newJob) throws IOException {
        Long id = jobQueueEndpointService.addNewJob(username, newJob);

        // Return 201 Created status code and the job id in body
        return Response.status(Status.CREATED).entity(id).build();
    }
}
