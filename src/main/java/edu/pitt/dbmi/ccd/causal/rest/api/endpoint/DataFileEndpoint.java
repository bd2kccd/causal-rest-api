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
import edu.pitt.dbmi.ccd.causal.rest.api.dto.DataFileDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.DataFileSummarization;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.ResumableChunkViaGet;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.ResumableChunkViaPost;
import edu.pitt.dbmi.ccd.causal.rest.api.service.DataFileEndpointService;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
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
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
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
@Path("/{username}/data")
public class DataFileEndpoint {

    private final DataFileEndpointService dataFileEndpointService;

    @Autowired
    public DataFileEndpoint(DataFileEndpointService dataFileEndpointService) {
        this.dataFileEndpointService = dataFileEndpointService;
    }

    /**
     * Delete a data file based on a given ID
     *
     * @param username
     * @param id
     * @return 204 No content
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed(Role.USER)
    public Response deleteById(@PathParam("username") String username, @PathParam("id") Long id) {
        dataFileEndpointService.deleteByIdAndUsername(id, username);

        return Response.noContent().build();
    }

    /**
     * Get data file info for a given ID
     *
     * @param username
     * @param id
     * @return 200 with data file info
     */
    @GET
    @Path("/{id}")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response findById(@PathParam("username") String username, @PathParam("id") Long id) {
        DataFileDTO dataFileDTO = dataFileEndpointService.findByIdAndUsername(id, username);

        return Response.ok(dataFileDTO).build();
    }

    /**
     * List all the existing data files
     *
     * @param username
     * @return 200 with file list
     */
    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response listDataFiles(@PathParam("username") String username) {
        List<DataFileDTO> dataFileDTOs = dataFileEndpointService.listDataFiles(username);
        GenericEntity<List<DataFileDTO>> entity = new GenericEntity<List<DataFileDTO>>(dataFileDTOs) {
        };

        return Response.ok(entity).build();
    }

    /**
     * For small file upload
     *
     * If you need to bin the named body part(s) of a multipart/form-data
     * request entity body to a resource method parameter you can use the
     *
     * @FormDataParam annotation. This annotation in conjunction with the media
     * type multipart/form-data should be used for submitting and consuming
     * forms that contain files, non-ASCII data, and binary data.
     *
     * Client must use name="file" for their file upload
     *
     * @param username
     * @param inputStream
     * @param fileDetail
     * @return 200 with uploaded file info
     * @throws IOException
     */
    @POST
    @Path("/upload")
    @Consumes(MULTIPART_FORM_DATA)
    @RolesAllowed(Role.USER)
    public Response upload(
            @PathParam("username") String username,
            @FormDataParam("file") InputStream inputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {

        DataFileDTO dataFileDTO = dataFileEndpointService.upload(username, inputStream, fileDetail);

        return Response.ok(dataFileDTO).build();
    }

    /**
     * Check to see if the resumable file chunk has already been uploaded
     *
     * needs resumable client (either resumable.js via the HTML5 File API or
     * resumable upload java client) based on https://github.com/bd2kccd/ccd-ws
     *
     * @param username
     * @param chunkViaGet
     * @return 200 or 404
     * @throws IOException
     */
    @GET
    @Path("/chunkUpload")
    @RolesAllowed(Role.USER)
    public Response checkChunkExistence(@PathParam("username") String username, @BeanParam ResumableChunkViaGet chunkViaGet) throws IOException {
        if (dataFileEndpointService.chunkExists(chunkViaGet, username)) {
            // No need to re-upload the same chunk
            // This is used by the resumable clinet internally
            return Response.status(Status.OK).build();
        } else {
            // Let's upload this chunk
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    /**
     * Upload each chunk via multipart post and returns the md5checkSum string
     * on the last one
     *
     * The @FormParam annotation in conjunction with the media type
     * "application/x-www-form-urlencoded" is inefficient for sending and
     * consuming large quantities of binary data or text containing non-ASCII
     * characters.
     *
     * The @FormDataParam annotation in conjunction with the media type
     * "multipart/form-data" should be used for submitting and consuming forms
     * that contain files, non-ASCII data, and binary data.
     *
     * @param username
     * @param chunkViaPost
     * @return 200 OK status code with md5checkSum string
     * @throws IOException
     */
    @POST
    @Path("/chunkUpload")
    @Consumes(MULTIPART_FORM_DATA)
    @RolesAllowed(Role.USER)
    public Response processChunkUpload(@PathParam("username") String username, @Valid @BeanParam ResumableChunkViaPost chunkViaPost) throws IOException {
        String md5checkSum = dataFileEndpointService.uploadChunk(chunkViaPost, username);
        // Only the last POST request will get a md5checksum on the completion of whole file
        // all requests before the last chunk will only get a 200 status code without response body
        return Response.ok(md5checkSum).build();
    }

    /**
     * Data Summary
     *
     * @param username
     * @param dataFileSummarization
     * @return
     * @throws IOException
     */
    @POST
    @Path("/summarize")
    @Consumes(APPLICATION_JSON)
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response summarizeDataFile(@PathParam("username") String username, @Valid DataFileSummarization dataFileSummarization) throws IOException {
        DataFileDTO dataFileDTO = dataFileEndpointService.summarizeDataFile(username, dataFileSummarization);
        return Response.ok(dataFileDTO).build();
    }
}
