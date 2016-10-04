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
    private BasicDataValidation dataValidation;

    public GfciContinuousParameters getAlgorithmParameters() {
        return algorithmParameters;
    }

    public void setAlgorithmParameters(GfciContinuousParameters algorithmParameters) {
        this.algorithmParameters = algorithmParameters;
    }

    public BasicDataValidation getDataValidation() {
        return dataValidation;
    }

    public void setDataValidation(BasicDataValidation dataValidation) {
        this.dataValidation = dataValidation;
    }

}
