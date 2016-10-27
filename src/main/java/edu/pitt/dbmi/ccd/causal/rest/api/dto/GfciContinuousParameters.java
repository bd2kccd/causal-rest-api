package edu.pitt.dbmi.ccd.causal.rest.api.dto;

/**
 *
 * Oct 3, 2016 6:21:13 PM
 *
 * @author Chirayu (Kong) Wongchokprasitti, PhD (chw20@pitt.edu)
 *
 */
public class GfciContinuousParameters extends FciParameters {

    private double alpha;

    private double penaltyDiscount;

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getPenaltyDiscount() {
        return penaltyDiscount;
    }

    public void setPenaltyDiscount(double penaltyDiscount) {
        this.penaltyDiscount = penaltyDiscount;
    }

}
