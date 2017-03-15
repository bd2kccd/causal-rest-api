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
public class GfciDiscreteDataValidation {

    // Skip check for zero variance variables
    protected boolean skipNonzeroVariance;

    public GfciDiscreteDataValidation() {
    }

    public boolean isSkipNonzeroVariance() {
        return skipNonzeroVariance;
    }

    public void setSkipNonzeroVariance(boolean skipNonzeroVariance) {
        this.skipNonzeroVariance = skipNonzeroVariance;
    }
}
