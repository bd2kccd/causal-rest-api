/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.service;

import edu.cmu.tetrad.annotation.AnnotatedClass;
import edu.cmu.tetrad.annotation.Score;
import edu.cmu.tetrad.annotation.ScoreAnnotations;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.ScoreDTO;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Zhou Yuan <zhy19@pitt.edu>
 */
public class ScoreEndpointService {
    /**
     * List all the available scores
     *
     * @return A list of available scores
     * @throws IOException
     */
    public List<ScoreDTO> listScores() {
        List<ScoreDTO> scores = new LinkedList<>();

        ScoreAnnotations scoreAnno = ScoreAnnotations.getInstance();
        
        List<AnnotatedClass<Score>> scoreAnnoList = scoreAnno.filterOutExperimental(scoreAnno.getAnnotatedClasses());

        scoreAnnoList.stream().map((scoreAnnoClass) -> scoreAnnoClass.getAnnotation()).forEachOrdered((score) -> {
            // Use command name as ID
            scores.add(new ScoreDTO(score.command(), score.name(), score.dataType().toString()));
        });

        return scores;
    }
}
