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

import java.util.List;
import javax.ws.rs.FormParam;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
public class NewJob {

    /*
     * @FormParam requires the @Consumes(APPLICATION_FORM_URLENCODED) to be specified in endpoint
     */
    @FormParam("algorithmName")
    private String algorithmName;

    @FormParam("algorithm")
    private String algorithm;

    @FormParam("dataset")
    private List<String> dataset;

    @FormParam("jvmOptions")
    private List<String> jvmOptions;

    @FormParam("parameters")
    private List<String> parameters;

    public NewJob() {
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public List<String> getDataset() {
        return dataset;
    }

    public void setDataset(List<String> dataset) {
        this.dataset = dataset;
    }

    public List<String> getJvmOptions() {
        return jvmOptions;
    }

    public void setJvmOptions(List<String> jvmOptions) {
        this.jvmOptions = jvmOptions;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }
}
