package edu.pitt.dbmi.ccd.causal.rest.api.dto;

import javax.validation.constraints.Min;

/**
 *
 * Oct 3, 2016 6:18:45 PM
 *
 * @author Chirayu (Kong) Wongchokprasitti, PhD (chw20@pitt.edu)
 *
 */
public class FciParameters {

    // Search max degree must be at least -1
    @Min(-1)
    protected int maxDegree;

    protected boolean faithfulnessAssumed;

    protected boolean verbose;

    public int getMaxDegree() {
        return maxDegree;
    }

    public void setMaxDegree(int maxDegree) {
        this.maxDegree = maxDegree;
    }

    public boolean isFaithfulnessAssumed() {
        return faithfulnessAssumed;
    }

    public void setFaithfulnessAssumed(boolean faithfulnessAssumed) {
        this.faithfulnessAssumed = faithfulnessAssumed;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

}
