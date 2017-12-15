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
import edu.pitt.dbmi.ccd.causal.rest.api.dto.JobInfoDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.NewJob;
import edu.pitt.dbmi.ccd.causal.rest.api.service.JobQueueEndpointService;
import java.io.IOException;
import java.util.List;
import javax.annotation.security.PermitAll;
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
import org.springframework.stereotype.Component;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
@Component
@PermitAll
@Path("/{uid}")
public class JobQueueEndpoint {

    private final JobQueueEndpointService jobQueueEndpointService;

    @Autowired
    public JobQueueEndpoint(JobQueueEndpointService jobQueueEndpointService) {
        this.jobQueueEndpointService = jobQueueEndpointService;
    }

    
    /**
     * Adding a new job and run a given algorithm with user provided parameters
     * 
     * @param uid
     * @param algoId
     * @param newJob
     * @return
     */
    @POST
    @Path("/jobs/{algoId}")
    @Consumes(APPLICATION_JSON)
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response addNewJob(@PathParam("uid") Long uid, @PathParam("algoId") String algoId, @Valid NewJob newJob) {
        JobInfoDTO jobInfo = jobQueueEndpointService.addNewJob(uid, algoId, newJob);
        GenericEntity<JobInfoDTO> jobRequestEntity = new GenericEntity<JobInfoDTO>(jobInfo) {
        };
        // Return 201 Created status code and the job id in body
        return Response.status(Status.CREATED).entity(jobRequestEntity).build();
    }
    
    /**
     * List all Queued or Running jobs associated with the user
     *
     * @param uid
     * @return
     * @throws IOException
     */
    @GET
    @Path("/jobs")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response listAllJobs(@PathParam("uid") Long uid) throws IOException {
        List<JobInfoDTO> jobInfoDTOs = jobQueueEndpointService.listAllJobs(uid);
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
     * @param uid
     * @param id
     * @return 200 OK status code with JobInfoDTO object or
     * ResourceNotFoundException
     * @throws IOException
     */
    @GET
    @Path("/jobs/{id}")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response jobStatus(@PathParam("uid") Long uid, @PathParam("id") Long id) throws IOException {
        JobInfoDTO jobInfoDTO = jobQueueEndpointService.checkJobStatus(uid, id);
        GenericEntity<JobInfoDTO> entity = new GenericEntity<JobInfoDTO>(jobInfoDTO) {
        };
        return Response.ok(entity).build();
    }

    /**
     * Cancel a job (job status can be Queued or Running)
     *
     * @param uid
     * @param id
     * @return
     * @throws IOException
     */
    @DELETE
    @Path("/jobs/{id}")
    @RolesAllowed(Role.USER)
    public Response cancelJob(@PathParam("uid") Long uid, @PathParam("id") Long id) throws IOException {
        boolean canceled = jobQueueEndpointService.cancelJob(uid, id);

        if (canceled) {
            return Response.ok("Job " + id + " has been canceled").build();
        } else {
            return Response.ok("Unable to cancel job " + id).build();
        }
    }

}
