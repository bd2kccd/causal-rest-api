package edu.pitt.dbmi.ccd.causal.rest.api.dto;

import org.springframework.beans.factory.annotation.Value;

/**
 *
 * Oct 3, 2016 9:01:04 PM
 *
 * @author Chirayu (Kong) Wongchokprasitti, PhD
 *
 */
public class GfciContinuousDataValidation extends BasicDataValidation {

    // Non-zero Variance - ensure that each variable has non-zero variance
    @Value("true")
    protected boolean nonZeroVariance;

    public GfciContinuousDataValidation() {
    }

    public boolean isNonZeroVariance() {
        return nonZeroVariance;
    }

    public void setNonZeroVariance(boolean nonZeroVariance) {
        this.nonZeroVariance = nonZeroVariance;
    }
}
