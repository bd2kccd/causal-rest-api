/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.dto;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
public class GfciDiscreteNewJob extends NewJob {

    // Algorithm parameters
    private GfciDiscreteParameters algorithmParameters;

    // Data validation flag
    private GfciDiscreteDataValidation dataValidation;

    public GfciDiscreteParameters getAlgorithmParameters() {
        return algorithmParameters;
    }

    public void setAlgorithmParameters(GfciDiscreteParameters algorithmParameters) {
        this.algorithmParameters = algorithmParameters;
    }

    public GfciDiscreteDataValidation getDataValidation() {
        return dataValidation;
    }

    public void setDataValidation(GfciDiscreteDataValidation dataValidation) {
        this.dataValidation = dataValidation;
    }
}
