/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.service;

import edu.cmu.tetrad.annotation.AnnotatedClass;
import edu.cmu.tetrad.annotation.TestOfIndependence;
import edu.cmu.tetrad.annotation.TestOfIndependenceAnnotations;
import edu.cmu.tetrad.data.DataType;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.IndependenceTestDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.BadRequestException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;

/**
 *
 * @author Zhou Yuan <zhy19@pitt.edu>
 */
@Service
public class IndependenceTestEndpointService {
    private final Map<String, AnnotatedClass<TestOfIndependence>> annotatedClasses;
    
    private final DataTypeEndpointService dataTypeEndpointService;

    public IndependenceTestEndpointService(DataTypeEndpointService dataTypeEndpointService) {
        // Exclude tests that only support Graph or Covariance data type
        this.annotatedClasses = TestOfIndependenceAnnotations.getInstance().getAnnotatedClasses().stream()
                // If a test supports graph, then it doesn't support other data types
                .filter(e -> !Arrays.asList(e.getAnnotation().dataType()).contains(DataType.Graph))
                // Different from graph, if a test supports covariance data, it may also support continuous or discrete data at the same time
                .filter(e -> !((Arrays.asList(e.getAnnotation().dataType()).size() == 1) && Arrays.asList(e.getAnnotation().dataType()).contains(DataType.Covariance)))
                .collect(() -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                        (m, e) -> m.put(e.getAnnotation().command(), e),
                        (m, u) -> m.putAll(u));
                
        this.dataTypeEndpointService = dataTypeEndpointService;
    }
    
    /**
     * List all the available independence tests
     *
     * @return A list of available independence tests
     */
    public List<IndependenceTestDTO> listAllIndependenceTests() {
        List<IndependenceTestDTO> testDTOs = new LinkedList<>();

        annotatedClasses.values().forEach(annoClass -> {
            List<String> supportedDataTypes = new LinkedList<>();
            
            for (DataType dataType : annoClass.getAnnotation().dataType()) {
                // Hide Covariance from output
                if (dataType != DataType.Covariance) {
                    supportedDataTypes.add(dataType.name().toLowerCase());
                }
            }
            // Use command name as ID
            testDTOs.add(new IndependenceTestDTO(annoClass.getAnnotation().command(), annoClass.getAnnotation().name(), supportedDataTypes));
        });


        return testDTOs;
    }
    
    /**
     * List all the available independence tests based on the given data type
     * 
     * @param dataType
     * @return 
     */
    public List<IndependenceTestDTO> listIndependenceTests(String dataType) {
        if (!dataTypeEndpointService.listDataTypes().contains(dataType.toLowerCase())) {
            throw new BadRequestException("Unrecognized data type: " + dataType);
        }
        
        List<IndependenceTestDTO> testDTOs = new LinkedList<>();

        annotatedClasses.values().forEach(annoClass -> {
            List<String> supportedDataTypes = new LinkedList<>();
            
            // Normalize dataType to match the Tetrad enum value
            String normalizedDataType = dataType.substring(0, 1).toUpperCase() + dataType.substring(1).toLowerCase();
            
            // Only return the tests that support the givien dataType
            if (Arrays.asList(annoClass.getAnnotation().dataType()).contains(DataType.valueOf(normalizedDataType))) {
                for (DataType dt : annoClass.getAnnotation().dataType()) {
                    // Hide Covariance from output
                    if (dt != DataType.Covariance) {
                        supportedDataTypes.add(dt.name().toLowerCase());
                    }
                }
                // Use command name as ID
                testDTOs.add(new IndependenceTestDTO(annoClass.getAnnotation().command(), annoClass.getAnnotation().name(), supportedDataTypes));
            }
        }); 

        return testDTOs;
    }
    
}
