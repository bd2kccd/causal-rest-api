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

import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
public abstract class NewJob {

    @NotNull
    @Range(min = 1, max = Long.MAX_VALUE)
    protected Long datasetFileId;

    @Range(min = 1, max = Long.MAX_VALUE)
    protected Long priorKnowledgeFileId;

    @Valid
    protected Boolean skipDataValidation;
    
    @Valid
    protected Set<AlgoParameter> algoParameters;
    
    // We must ues @Valid annoation here again to enable the bean validation in JvmOptions,
    // because the @Valid annoation used in endpopint only works on this NewJob bean,
    // the validation won't populate to the nested bean without this @Valid here
    @Valid
    protected JvmOptions jvmOptions;

    @Valid
    protected Set<HpcParameter> hpcParameters;

    public NewJob() {
    }

    public Long getDatasetFileId() {
        return datasetFileId;
    }

    public void setDatasetFileId(Long datasetFileId) {
        this.datasetFileId = datasetFileId;
    }

    public Long getPriorKnowledgeFileId() {
        return priorKnowledgeFileId;
    }

    public void setPriorKnowledgeFileId(Long priorKnowledgeFileId) {
        this.priorKnowledgeFileId = priorKnowledgeFileId;
    }

    public Boolean getSkipDataValidation() {
        return skipDataValidation;
    }

    public void setSkipDataValidation(Boolean skipDataValidation) {
        this.skipDataValidation = skipDataValidation;
    }

    public Set<AlgoParameter> getAlgoParameters() {
        return algoParameters;
    }

    public void setAlgoParameters(Set<AlgoParameter> algoParameters) {
        this.algoParameters = algoParameters;
    }

    public JvmOptions getJvmOptions() {
        return jvmOptions;
    }

    public void setJvmOptions(JvmOptions jvmOptions) {
        this.jvmOptions = jvmOptions;
    }

    public Set<HpcParameter> getHpcParameters() {
        return hpcParameters;
    }

    public void setHpcParameters(Set<HpcParameter> hpcParameters) {
        this.hpcParameters = hpcParameters;
    }

}
