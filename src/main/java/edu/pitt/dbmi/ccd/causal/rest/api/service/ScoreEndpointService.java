/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.service;

import edu.cmu.tetrad.annotation.AnnotatedClass;
import edu.cmu.tetrad.annotation.Score;
import edu.cmu.tetrad.annotation.ScoreAnnotations;
import edu.cmu.tetrad.data.DataType;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.ScoreDTO;
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
public class ScoreEndpointService {
    
    private final Map<String, AnnotatedClass<Score>> annotatedClasses;

    public ScoreEndpointService() {
        // Exclude scores that only support Graph data type
        this.annotatedClasses = ScoreAnnotations.getInstance().getAnnotatedClasses().stream()
                .filter(e -> !Arrays.asList(e.getAnnotation().dataType()).contains(DataType.Graph))
                .collect(() -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                        (m, e) -> m.put(e.getAnnotation().command(), e),
                        (m, u) -> m.putAll(u));
    }
    
    /**
     * List all the available scores
     *
     * @return A list of available scores
     */
    public List<ScoreDTO> listAllScores() {
        List<ScoreDTO> scoreDTOs = new LinkedList<>();

        annotatedClasses.values().forEach((annoClass) -> {
            List<String> supportedDataTypes = new LinkedList<>();
            
            for (DataType dataType : annoClass.getAnnotation().dataType()) {
                supportedDataTypes.add(dataType.name().toLowerCase());
            }
            // Use command name as ID
            scoreDTOs.add(new ScoreDTO(annoClass.getAnnotation().command(), annoClass.getAnnotation().name(), supportedDataTypes));
        });
        
        return scoreDTOs;
    }
    
    /**
     * List all the available scores based on the given data type
     * 
     * @param dataType
     * @return 
     */
    public List<ScoreDTO> listScores(String dataType) {
        List<ScoreDTO> scoreDTOs = new LinkedList<>();

        annotatedClasses.values().forEach((annoClass) -> {
            List<String> supportedDataTypes = new LinkedList<>();
            
            // Normalize dataType to match the Tetrad enum value
            String normalizedDataType = dataType.substring(0, 1).toUpperCase() + dataType.substring(1).toLowerCase();
            
            // Only return the tests that support the givien dataType
            if (Arrays.asList(annoClass.getAnnotation().dataType()).contains(DataType.valueOf(normalizedDataType))) {
                for (DataType dt : annoClass.getAnnotation().dataType()) {
                    supportedDataTypes.add(dt.name().toLowerCase());
                }
                // Use command name as ID
                scoreDTOs.add(new ScoreDTO(annoClass.getAnnotation().command(), annoClass.getAnnotation().name(), supportedDataTypes));
            }
        }); 
        
        return scoreDTOs;
    }
    
}
