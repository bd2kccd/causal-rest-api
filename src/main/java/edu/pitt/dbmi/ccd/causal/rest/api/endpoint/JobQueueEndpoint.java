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
import edu.pitt.dbmi.ccd.causal.rest.api.dto.FgsContinuousNewJob;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.FgsDiscreteNewJob;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.JobInfoDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.service.JobQueueEndpointService;
import java.io.IOException;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
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

    /**
     * Adding a new job and run FGS continuous
     *
     * @param username
     * @param newJob
     * @return 201 Created status code with new job ID
     * @throws IOException
     */
    @POST
    @Path("/fgs")
    @Consumes(APPLICATION_JSON)
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response addFgsContinuousNewJob(@PathParam("username") String username, @Valid FgsContinuousNewJob newJob) throws IOException {
        Long id = jobQueueEndpointService.addFgsContinuousNewJob(username, newJob);

        // Return 201 Created status code and the job id in body
        return Response.status(Status.CREATED).entity(id).build();
    }

    /**
     * Adding a new job and run FGS discrete
     *
     * @param username
     * @param newJob
     * @return 201 Created status code with new job ID
     * @throws IOException
     */
    @POST
    @Path("/fgs-discrete")
    @Consumes(APPLICATION_JSON)
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response addFgsDiscreteNewJob(@PathParam("username") String username, @Valid FgsDiscreteNewJob newJob) throws IOException {
        Long id = jobQueueEndpointService.addFgsDiscreteNewJob(username, newJob);

        // Return 201 Created status code and the job id in body
        return Response.status(Status.CREATED).entity(id).build();
    }

    /**
     * List all Queued or Running jobs associated with the user
     *
     * @param username
     * @return
     * @throws IOException
     */
    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response listAllJobs(@PathParam("username") String username) throws IOException {
        List<JobInfoDTO> jobInfoDTOs = jobQueueEndpointService.listAllJobs(username);
        GenericEntity<List<JobInfoDTO>> entity = new GenericEntity<List<JobInfoDTO>>(jobInfoDTOs) {
        };

        return Response.ok(entity).build();
    }

    /**
     * Checking job status for a given job ID
     *
     * Note: job ID is not associated with user account at this moment that's
     * why we don't have an endpoint that lists all the running jobs
     *
     * @param username
     * @param id
     * @return 200 OK status code with job status ("Pending" or "Completed")
     * @throws IOException
     */
    @GET
    @Path("/{id}")
    @RolesAllowed(Role.USER)
    public Response jobStatus(@PathParam("username") String username, @PathParam("id") Long id) throws IOException {
        boolean completed = jobQueueEndpointService.checkJobStatus(username, id);

        if (completed) {
            return Response.ok("Job " + id + " has been completed.").build();
        } else {
            return Response.ok("Job " + id + " is still running.").build();
        }
    }

    /**
     * Cancel a job (job status can be Queued or Running)
     *
     * @param username
     * @param id
     * @return
     * @throws IOException
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed(Role.USER)
    public Response cancelJob(@PathParam("username") String username, @PathParam("id") Long id) throws IOException {
        boolean canceled = jobQueueEndpointService.cancelJob(username, id);

        if (canceled) {
            return Response.ok("Job " + id + " has been canceled").build();
        } else {
            return Response.ok("Unable to cancel job " + id).build();
        }
    }

}
