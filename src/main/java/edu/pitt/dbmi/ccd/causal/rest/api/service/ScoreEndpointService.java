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
import org.springframework.stereotype.Service;

/**
 *
 * @author Zhou Yuan <zhy19@pitt.edu>
 */
@Service
public class ScoreEndpointService {
    /**
     * List all the available scores
     *
     * @return A list of available scores
     */
    public List<ScoreDTO> listAllScores() {
        List<ScoreDTO> scores = new LinkedList<>();

        ScoreAnnotations scoreAnno = ScoreAnnotations.getInstance();
        
        List<AnnotatedClass<Score>> scoreAnnoList = scoreAnno.filterOutExperimental(scoreAnno.getAnnotatedClasses());

        scoreAnnoList.stream().map((scoreAnnoClass) -> scoreAnnoClass.getAnnotation()).forEachOrdered((score) -> {
            List<String> supportedDataTypes = new LinkedList<>();
            
            for (DataType dataType : score.dataType()) {
                supportedDataTypes.add(dataType.name());
            }
            // Use command name as ID
            scores.add(new ScoreDTO(score.command(), score.name(), supportedDataTypes));
        });

        return scores;
    }
    
    /**
     * List all the available scores based on the given data type
     * 
     * @param dataType
     * @return 
     */
    public List<ScoreDTO> listScores(String dataType) {
        List<ScoreDTO> scores = new LinkedList<>();

        ScoreAnnotations scoreAnno = ScoreAnnotations.getInstance();
        
        List<AnnotatedClass<Score>> scoreAnnoList = scoreAnno.filterOutExperimental(scoreAnno.getAnnotatedClasses());

        scoreAnnoList.stream().map((scoreAnnoClass) -> scoreAnnoClass.getAnnotation()).forEachOrdered((score) -> {
            // Only return the tests that support the givien dataType
            if (Arrays.asList(score.dataType()).contains(DataType.valueOf(dataType))) {
                List<String> supportedDataTypes = new LinkedList<>();

                for (DataType dt : score.dataType()) {
                    supportedDataTypes.add(dt.name());
                }
                // Use command name as ID
                scores.add(new ScoreDTO(score.command(), score.name(), supportedDataTypes));
            }     
        });

        return scores;
    }
}
