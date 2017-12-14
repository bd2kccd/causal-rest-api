/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.service;

import edu.cmu.tetrad.data.DataType;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.DataTypeDTO;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Zhou Yuan <zhy19@pitt.edu>
 */
public class DataTypeEndpointService {
    /**
     * List all the available algorithms: Continuous, Discrete, Mixed, Graph, Covariance
     *
     * @return A list of available algorithms
     * @throws IOException
     */
    public List<DataTypeDTO> listDataTypes() {
        List<DataTypeDTO> dataTypes = new LinkedList<>();
        // Convert Tetrad's DataType enum to a list of strings
        List<DataType> dt = new LinkedList<DataType>(Arrays.asList(DataType.values()));
        
        for (DataType dataType : dt) {
            dataTypes.add(new DataTypeDTO(dataType.name()));
        }

        return dataTypes;
    }
}
