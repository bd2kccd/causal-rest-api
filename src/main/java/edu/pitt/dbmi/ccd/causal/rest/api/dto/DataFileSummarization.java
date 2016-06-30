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

import javax.ws.rs.FormParam;

/**
 * This bean is used to get form data when user wants to summarize a data file
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
public class DataFileSummarization {

    /*
    * @FormParam requires the @Consumes(APPLICATION_FORM_URLENCODED) to be specified in endpoint
     */
    @FormParam("id")
    private Long id;

    /*
    * We'll convert the string value to VariableType object
     */
    @FormParam("variableType")
    private String variableType;

    /*
    * We'll convert the string value to FileDelimiter object
     */
    @FormParam("fileDelimiter")
    private String fileDelimiter;

    public DataFileSummarization() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVariableType() {
        return variableType;
    }

    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }

    public String getFileDelimiter() {
        return fileDelimiter;
    }

    public void setFileDelimiter(String fileDelimiter) {
        this.fileDelimiter = fileDelimiter;
    }
}
