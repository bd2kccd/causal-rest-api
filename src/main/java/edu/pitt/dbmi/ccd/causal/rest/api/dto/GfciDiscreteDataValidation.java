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
public class GfciDiscreteDataValidation extends BasicDataValidation {

    //  Skip 'limit number of categories' check
    private boolean skipCategoryLimit;

    public GfciDiscreteDataValidation() {
    }

    public boolean isSkipCategoryLimit() {
        return skipCategoryLimit;
    }

    public void setSkipCategoryLimit(boolean skipCategoryLimit) {
        this.skipCategoryLimit = skipCategoryLimit;
    }
}
