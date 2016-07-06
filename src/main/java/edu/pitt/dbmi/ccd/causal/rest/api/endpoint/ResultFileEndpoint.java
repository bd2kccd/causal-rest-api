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
import edu.pitt.dbmi.ccd.causal.rest.api.dto.ResultFileDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.ResultComparisonFileDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.service.ResultFileEndpointService;
import java.io.File;
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
@Path("/{username}/results")
public class ResultFileEndpoint {

    private final ResultFileEndpointService algorithmResultEndpointService;

    @Autowired
    public ResultFileEndpoint(ResultFileEndpointService algorithmResultEndpointService) {
        this.algorithmResultEndpointService = algorithmResultEndpointService;
    }

    /**
     * List all the algorithm result files
     *
     * @param username
     * @return 200 with a list of existing result files
     * @throws IOException
     */
    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response listAlgorithmResultFiles(@PathParam("username") String username) throws IOException {
        List<ResultFileDTO> algorithmResultDTOs = algorithmResultEndpointService.listAlgorithmResults(username);
        GenericEntity<List<ResultFileDTO>> entity = new GenericEntity<List<ResultFileDTO>>(algorithmResultDTOs) {
        };

        return Response.ok(entity).build();
    }

    /**
     * Download the content of a result file for a given file name
     *
     * @param username
     * @param fileName
     * @return Plain text file content
     * @throws IOException
     */
    @GET
    @Path("/{fileName}")
    @RolesAllowed(Role.USER)
    public Response downloadAlgorithmResultFile(@PathParam("username") String username, @PathParam("fileName") String fileName) throws IOException {
        File file = algorithmResultEndpointService.getAlgorithmResultFile(username, fileName);

        return Response.ok(file)
                .header("Content-Disposition", "attachment; filename=" + fileName)
                .build();
    }

    /**
     * List all the comparison files
     *
     * @param username
     * @return
     * @throws IOException
     */
    @GET
    @Path("/comparisons")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response listAlgorithmResultComparisonFiles(@PathParam("username") String username) throws IOException {
        List<ResultFileDTO> algorithmResultDTOs = algorithmResultEndpointService.listAlgorithmResultComparisons(username);
        GenericEntity<List<ResultFileDTO>> entity = new GenericEntity<List<ResultFileDTO>>(algorithmResultDTOs) {
        };

        return Response.ok(entity).build();
    }

    /**
     * Download the content of a results comparison file for a given file name
     *
     * @param username
     * @param fileName
     * @return Plain text file content
     * @throws IOException
     */
    @GET
    @Path("/comparisons/{fileName}")
    @RolesAllowed(Role.USER)
    public Response downloadAlgorithmResultsComparisonFile(@PathParam("username") String username, @PathParam("fileName") String fileName) throws IOException {
        File file = algorithmResultEndpointService.getAlgorithmResultsComparisonFile(username, fileName);

        return Response.ok(file)
                .header("Content-Disposition", "attachment; filename=" + fileName)
                .build();
    }

    /**
     * Compare multi result files
     *
     * @param username
     * @param fileNames
     * @return The comparison result text file content
     * @throws IOException
     */
    @GET
    @Path("/compare/{fileNames}")
    @RolesAllowed(Role.USER)
    public Response compareAlgorithmResults(@PathParam("username") String username, @PathParam("fileNames") String fileNames) throws IOException {
        // Get the result comparsion file content and file name
        ResultComparisonFileDTO comparisonFileDTO = algorithmResultEndpointService.compareAlgorithmResults(username, fileNames);

        return Response.ok(comparisonFileDTO.getFile())
                .header("Content-Disposition", "attachment; filename=" + comparisonFileDTO.getFileName())
                .build();
    }
}
