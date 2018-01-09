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
public class NewJob {

    @NotNull
    protected String algoId;

    @NotNull
    protected String testId;

    @NotNull
    protected String scoreId;
    
    @NotNull
    @Range(min = 1, max = Long.MAX_VALUE)
    protected long datasetFileId;

    @Range(min = 1, max = Long.MAX_VALUE)
    protected long priorKnowledgeFileId;

    @Valid
    protected boolean skipDataValidation;
    
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

    public String getAlgoId() {
        return algoId;
    }

    public void setAlgoId(String algoId) {
        this.algoId = algoId;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getScoreId() {
        return scoreId;
    }

    public void setScoreId(String scoreId) {
        this.scoreId = scoreId;
    }

    public long getDatasetFileId() {
        return datasetFileId;
    }

    public void setDatasetFileId(long datasetFileId) {
        this.datasetFileId = datasetFileId;
    }

    public long getPriorKnowledgeFileId() {
        return priorKnowledgeFileId;
    }

    public void setPriorKnowledgeFileId(long priorKnowledgeFileId) {
        this.priorKnowledgeFileId = priorKnowledgeFileId;
    }

    public boolean isSkipDataValidation() {
        return skipDataValidation;
    }

    public void setSkipDataValidation(boolean skipDataValidation) {
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
