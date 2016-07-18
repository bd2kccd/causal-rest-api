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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
public class NewJob {

    @NotNull
    @Min(1)
    @Max(Long.MAX_VALUE)
    protected Long dataFileId;

    // Algorithm parameters
    protected int depth;

    @Value("true")
    protected boolean heuristicSpeedup;

    @Value("true")
    protected boolean verbose;

    // Data validation
    @Value("true")
    protected boolean nonZeroVarianceValidation;

    @Value("true")
    protected boolean uniqueVarNameValidation;

    protected JvmOptions jvmOptions;

    public NewJob() {
    }

    public Long getDataFileId() {
        return dataFileId;
    }

    public void setDataFileId(Long dataFileId) {
        this.dataFileId = dataFileId;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean isHeuristicSpeedup() {
        return heuristicSpeedup;
    }

    public void setHeuristicSpeedup(boolean heuristicSpeedup) {
        this.heuristicSpeedup = heuristicSpeedup;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isNonZeroVarianceValidation() {
        return nonZeroVarianceValidation;
    }

    public void setNonZeroVarianceValidation(boolean nonZeroVarianceValidation) {
        this.nonZeroVarianceValidation = nonZeroVarianceValidation;
    }

    public boolean isUniqueVarNameValidation() {
        return uniqueVarNameValidation;
    }

    public void setUniqueVarNameValidation(boolean uniqueVarNameValidation) {
        this.uniqueVarNameValidation = uniqueVarNameValidation;
    }

    public JvmOptions getJvmOptions() {
        return jvmOptions;
    }

    public void setJvmOptions(JvmOptions jvmOptions) {
        this.jvmOptions = jvmOptions;
    }

}
