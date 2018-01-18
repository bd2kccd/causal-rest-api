/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.service;

import edu.cmu.tetrad.data.DataType;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author Zhou Yuan <zhy19@pitt.edu>
 */
@Service
public class DataTypeEndpointService {
    /**
     * List all the available algorithms: Continuous, Discrete, Mixed, Graph, Covariance
     *
     * @return A list of available algorithms
     */
    public List<String> listDataTypes() {
        List<String> dataTypes = new LinkedList<>();
        // Convert Tetrad's DataType enum to a list of strings
        List<DataType> dt = new LinkedList<>(Arrays.asList(DataType.values()));
        
        // Exclude Graph and Covariance
        dt.forEach((dataType) -> {
            if (dataType != DataType.Graph && dataType != DataType.Covariance) {
                dataTypes.add(dataType.name().toLowerCase());
            }
        });

        return dataTypes;
    }
}
