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
package edu.pitt.dbmi.ccd.causal.rest.api.dto;

import java.io.InputStream;
import org.glassfish.jersey.media.multipart.FormDataParam;

/**
 * All form data (metadata and input stream) found in the multipart post
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
public class ResumableChunkViaPost {

    // We have to use @FormDataParam rather than @FormParam
    // since this is multipart post
    @FormDataParam("resumableChunkSize")
    private long resumableChunkSize;

    @FormDataParam("resumableTotalSize")
    private long resumableTotalSize;

    @FormDataParam("resumableCurrentChunkSize")
    private long resumableCurrentChunkSize;

    @FormDataParam("resumableChunkNumber")
    private int resumableChunkNumber;

    @FormDataParam("resumableTotalChunks")
    private int resumableTotalChunks;

    @FormDataParam("resumableIdentifier")
    private String resumableIdentifier;

    @FormDataParam("resumableFilename")
    private String resumableFilename;

    @FormDataParam("resumableRelativePath")
    private String resumableRelativePath;

    @FormDataParam("resumableType")
    private String resumableType;

    @FormDataParam("file")
    private InputStream file;

    public ResumableChunkViaPost() {
    }

    public long getResumableChunkSize() {
        return resumableChunkSize;
    }

    public void setResumableChunkSize(long resumableChunkSize) {
        this.resumableChunkSize = resumableChunkSize;
    }

    public long getResumableTotalSize() {
        return resumableTotalSize;
    }

    public void setResumableTotalSize(long resumableTotalSize) {
        this.resumableTotalSize = resumableTotalSize;
    }

    public long getResumableCurrentChunkSize() {
        return resumableCurrentChunkSize;
    }

    public void setResumableCurrentChunkSize(long resumableCurrentChunkSize) {
        this.resumableCurrentChunkSize = resumableCurrentChunkSize;
    }

    public int getResumableChunkNumber() {
        return resumableChunkNumber;
    }

    public void setResumableChunkNumber(int resumableChunkNumber) {
        this.resumableChunkNumber = resumableChunkNumber;
    }

    public int getResumableTotalChunks() {
        return resumableTotalChunks;
    }

    public void setResumableTotalChunks(int resumableTotalChunks) {
        this.resumableTotalChunks = resumableTotalChunks;
    }

    public String getResumableIdentifier() {
        return resumableIdentifier;
    }

    public void setResumableIdentifier(String resumableIdentifier) {
        this.resumableIdentifier = resumableIdentifier;
    }

    public String getResumableFilename() {
        return resumableFilename;
    }

    public void setResumableFilename(String resumableFilename) {
        this.resumableFilename = resumableFilename;
    }

    public String getResumableRelativePath() {
        return resumableRelativePath;
    }

    public void setResumableRelativePath(String resumableRelativePath) {
        this.resumableRelativePath = resumableRelativePath;
    }

    public String getResumableType() {
        return resumableType;
    }

    public InputStream getFile() {
        return file;
    }

    public void setFile(InputStream file) {
        this.file = file;
    }
}
