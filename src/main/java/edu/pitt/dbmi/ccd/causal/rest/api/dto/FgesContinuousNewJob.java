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

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
public class FgesContinuousNewJob extends NewJob {

    // Algorithm parameters
    private FgesContinuousParameters algorithmParameters;

    // Data validation flag
    private FgesContinuousDataValidation dataValidation;

    public FgesContinuousNewJob() {
    }

    public FgesContinuousParameters getAlgorithmParameters() {
        return algorithmParameters;
    }

    public void setAlgorithmParameters(FgesContinuousParameters algorithmParameters) {
        this.algorithmParameters = algorithmParameters;
    }

    public FgesContinuousDataValidation getDataValidation() {
        return dataValidation;
    }

    public void setDataValidation(FgesContinuousDataValidation dataValidation) {
        this.dataValidation = dataValidation;
    }

}