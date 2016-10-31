package edu.pitt.dbmi.ccd.causal.rest.api.dto;

/**
 *
 * Oct 3, 2016 9:01:04 PM
 *
 * @author Chirayu (Kong) Wongchokprasitti, PhD
 *
 */
public class GfciContinuousDataValidation extends BasicDataValidation {

    // Skip check for zero variance variables
    protected boolean skipNonzeroVariance;

    public GfciContinuousDataValidation() {
    }

    public boolean isSkipNonzeroVariance() {
        return skipNonzeroVariance;
    }

    public void setSkipNonzeroVariance(boolean skipNonzeroVariance) {
        this.skipNonzeroVariance = skipNonzeroVariance;
    }

}
