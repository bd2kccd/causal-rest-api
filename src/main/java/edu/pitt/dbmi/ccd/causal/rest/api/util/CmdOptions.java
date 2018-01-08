/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.util;

/**
 *
 * @author Zhou Yuan <zhy19@pitt.edu>
 */
public class CmdOptions {

    private CmdOptions() {
    }
    
    public static final String DATASET = "--dataset";
    public static final String DATATYPE = "--data-type";
    public static final String INDEPENDENCE_TEST = "--test";
    public static final String SCORE = "--score";
    public static final String KNOWLEDGE = "--knowledge"; 
    public static final String DELIMITER = "--delimiter";
    public static final String JSON_GRAPH = "--json-graph";
    public static final String OUTPUT_PREFIX = "--prefix";
    public static final String SKIP_VALIDATION = "--skip-validation";
}
