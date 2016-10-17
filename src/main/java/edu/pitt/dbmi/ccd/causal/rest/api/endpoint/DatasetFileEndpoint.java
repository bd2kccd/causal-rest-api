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
import edu.pitt.dbmi.ccd.causal.rest.api.dto.DatasetFileDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.DatasetFileSummarization;
import edu.pitt.dbmi.ccd.causal.rest.api.service.DatasetFileEndpointService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * Jun 5, 2016 9:41:01 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Component
@PermitAll
@Path("/{uid}/dataset")
public class DatasetFileEndpoint {

    private final DatasetFileEndpointService datasetFileEndpointService;

    @Autowired
    public DatasetFileEndpoint(DatasetFileEndpointService datasetFileEndpointService) {
        this.datasetFileEndpointService = datasetFileEndpointService;
    }

    /**
     * Delete a data file based on a given ID
     *
     * @param uid
     * @param id
     * @return 204 No content
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed(Role.USER)
    public Response deleteById(@PathParam("uid") Long uid, @PathParam("id") Long id) {
        datasetFileEndpointService.deleteByIdAndUid(id, uid);

        return Response.noContent().build();
    }

    /**
     * Get data file info for a given ID
     *
     * @param uid
     * @param id
     * @return 200 with data file info
     */
    @GET
    @Path("/{id}")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response findById(@PathParam("uid") Long uid, @PathParam("id") Long id) {
        DatasetFileDTO dataFileDTO = datasetFileEndpointService.findByIdAndUid(id, uid);

        return Response.ok(dataFileDTO).build();
    }

    /**
     * List all the existing data files
     *
     * @param uid
     * @return 200 with file list
     */
    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response listDataFiles(@PathParam("uid") Long uid) {
        List<DatasetFileDTO> dataFileDTOs = datasetFileEndpointService.listAllDatasetFiles(uid);
        GenericEntity<List<DatasetFileDTO>> entity = new GenericEntity<List<DatasetFileDTO>>(dataFileDTOs) {
        };

        return Response.ok(entity).build();
    }

    /**
     * Data Summary
     *
     * @param uid
     * @param dataFileSummarization
     * @return
     * @throws IOException
     */
    @POST
    @Path("/summarize")
    @Consumes(APPLICATION_JSON)
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response summarizeDataFile(@PathParam("uid") Long uid, @Valid DatasetFileSummarization dataFileSummarization) throws IOException {
        DatasetFileDTO dataFileDTO = datasetFileEndpointService.summarizeDatasetFile(uid, dataFileSummarization);
        return Response.ok(dataFileDTO).build();
    }
}
