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
import java.util.LinkedList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author Zhou Yuan <zhy19@pitt.edu>
 */
@Service
public class IndependenceTestEndpointService {
    /**
     * List all the available independence tests
     *
     * @return A list of available independence tests
     */
    public List<IndependenceTestDTO> listIndependenceTests() {
        List<IndependenceTestDTO> tests = new LinkedList<>();

        TestOfIndependenceAnnotations testAnno = TestOfIndependenceAnnotations.getInstance();
        
        List<AnnotatedClass<TestOfIndependence>> testAnnoList = testAnno.filterOutExperimental(testAnno.getAnnotatedClasses());

        testAnnoList.stream().map((scoreAnnoClass) -> scoreAnnoClass.getAnnotation()).forEachOrdered((test) -> {
            List<String> supportedDataTypes = new LinkedList<>();
            
            for (DataType dataType : test.dataType()) {
                supportedDataTypes.add(dataType.name());
            }
            // Use command name as ID
            tests.add(new IndependenceTestDTO(test.command(), test.name(), supportedDataTypes));
        });

        return tests;
    }
}
