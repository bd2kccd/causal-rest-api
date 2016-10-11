package edu.pitt.dbmi.ccd.causal.rest.api.dto;

import org.springframework.beans.factory.annotation.Value;

/**
 * 
 * Oct 3, 2016 6:18:45 PM
 * 
 * @author Chirayu (Kong) Wongchokprasitti, PhD (chw20@pitt.edu)
 * 
 */
public class FciParameters {

    @Value("100")
    protected int maxInDegree;

    @Value("true")
    protected boolean faithfulnessAssumed;

    @Value("true")
    protected boolean verbose;

    public int getMaxInDegree() {
        return maxInDegree;
    }

    public void setMaxInDegree(int maxInDegree) {
        this.maxInDegree = maxInDegree;
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
