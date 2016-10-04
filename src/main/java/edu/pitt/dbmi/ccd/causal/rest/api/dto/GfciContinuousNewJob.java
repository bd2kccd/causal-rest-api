package edu.pitt.dbmi.ccd.causal.rest.api.dto;

/**
 * 
 * Oct 3, 2016 6:16:40 PM
 * 
 * @author Chirayu (Kong) Wongchokprasitti, PhD (chw20@pitt.edu)
 * 
 */
public class GfciContinuousNewJob extends NewJob {

    // Algorithm parameters
    private GfciContinuousParameters algorithmParameters;

    // Data validation flag
    private GfciContinuousDataValidation dataValidation;

    public GfciContinuousParameters getAlgorithmParameters() {
        return algorithmParameters;
    }

    public void setAlgorithmParameters(GfciContinuousParameters algorithmParameters) {
        this.algorithmParameters = algorithmParameters;
    }

    public GfciContinuousDataValidation getDataValidation() {
        return dataValidation;
    }

    public void setDataValidation(GfciContinuousDataValidation dataValidation) {
        this.dataValidation = dataValidation;
    }

}
