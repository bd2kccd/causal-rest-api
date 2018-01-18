/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.dto;

import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author Zhou Yuan <zhy19@pitt.edu>
 */
public class AlgoInfo {
    @NotEmpty
    protected String algoId;

    // Can be empty since some algorithms don't require test
    protected String testId;

    // Can be empty since some algorithms don't require score
    protected String scoreId;

    public AlgoInfo() {
    }
    
    public String getAlgoId() {
        return algoId;
    }

    public void setAlgoId(String algoId) {
        this.algoId = algoId;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getScoreId() {
        return scoreId;
    }

    public void setScoreId(String scoreId) {
        this.scoreId = scoreId;
    }
    
    
}
