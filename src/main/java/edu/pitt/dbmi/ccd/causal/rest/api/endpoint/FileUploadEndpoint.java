/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.endpoint;

import edu.pitt.dbmi.ccd.causal.rest.api.Role;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.DatasetFileDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.PriorKnowledgeFileDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.ResumableChunkViaGet;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.ResumableChunkViaPost;
import edu.pitt.dbmi.ccd.causal.rest.api.service.FileUploadEndpointService;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
@Component
@PermitAll
@Path("/{uid}/upload")
public class FileUploadEndpoint {

    private final FileUploadEndpointService fileUploadEndpointService;

    @Autowired
    public FileUploadEndpoint(FileUploadEndpointService fileUploadEndpointService) {
        this.fileUploadEndpointService = fileUploadEndpointService;
    }

    /**
     * For small dataset file upload
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
     * @param uid
     * @param inputStream
     * @param fileDetail
     * @return 200 with uploaded file info
     * @throws IOException
     */
    @POST
    @Path("/dataset")
    @Consumes(MULTIPART_FORM_DATA)
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response uploadDatasetFile(
            @PathParam("uid") Long uid,
            @FormDataParam("file") InputStream inputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {

        DatasetFileDTO dataFileDTO = fileUploadEndpointService.uploadDatasetFile(uid, inputStream, fileDetail);

        return Response.ok(dataFileDTO).build();
    }

    /**
     * For small prior knowledge file upload
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
     * @param uid
     * @param inputStream
     * @param fileDetail
     * @return 200 with uploaded file info
     * @throws IOException
     */
    @POST
    @Path("/priorknowledge")
    @Consumes(MULTIPART_FORM_DATA)
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    @RolesAllowed(Role.USER)
    public Response uploadPriorKnowledgeFile(
            @PathParam("uid") Long uid,
            @FormDataParam("file") InputStream inputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {

        PriorKnowledgeFileDTO priorKnowledgeFileDTO = fileUploadEndpointService.uploadPriorKnowledgeFile(uid, inputStream, fileDetail);

        return Response.ok(priorKnowledgeFileDTO).build();
    }

    /**
     * Check to see if the resumable file chunk has already been uploaded
     *
     * needs resumable client (either resumable.js via the HTML5 File API or
     * resumable upload java client) based on https://github.com/bd2kccd/ccd-ws
     *
     * @param uid
     * @param chunkViaGet
     * @return 200 or 404
     * @throws IOException
     */
    @GET
    @Path("/chunk")
    @RolesAllowed(Role.USER)
    public Response checkChunkExistence(@PathParam("uid") Long uid, @Valid @BeanParam ResumableChunkViaGet chunkViaGet) throws IOException {
        if (fileUploadEndpointService.chunkExists(chunkViaGet, uid)) {
            // No need to re-upload the same chunk
            // This is used by the resumable clinet internally
            return Response.status(Response.Status.OK).build();
        } else {
            // Let's upload this chunk
            return Response.status(Response.Status.NOT_FOUND).build();
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
     * @param uid
     * @param chunkViaPost
     * @return 200 OK status code with md5checkSum string
     * @throws IOException
     */
    @POST
    @Path("/chunk")
    @Consumes(MULTIPART_FORM_DATA)
    @RolesAllowed(Role.USER)
    public Response processChunkUpload(@PathParam("uid") Long uid, @Valid @BeanParam ResumableChunkViaPost chunkViaPost) throws IOException {
        String md5checkSum = fileUploadEndpointService.uploadChunk(chunkViaPost, uid);
        // Only the last POST request will get a md5checksum on the completion of whole file
        // all requests before the last chunk will only get a 200 status code without response body
        return Response.ok(md5checkSum).build();
    }
}
