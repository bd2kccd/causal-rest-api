/*
 * Copyright (C) 2016 University of Pittsburgh.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package edu.pitt.dbmi.ccd.causal.rest.api.service;

import edu.cmu.tetrad.algcomparison.algorithm.AlgorithmFactory;
import edu.cmu.tetrad.annotation.Algorithm;
import edu.cmu.tetrad.annotation.AlgorithmAnnotations;
import edu.cmu.tetrad.annotation.AnnotatedClass;
import edu.cmu.tetrad.annotation.Score;
import edu.cmu.tetrad.annotation.ScoreAnnotations;
import edu.cmu.tetrad.annotation.TestOfIndependence;
import edu.cmu.tetrad.annotation.TestOfIndependenceAnnotations;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.AlgorithmDTO;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
@Service
public class AlgorithmEndpointService {

    private final Map<String, AnnotatedClass<Algorithm>> annotatedAlgoClasses;
    private final Map<String, AnnotatedClass<TestOfIndependence>> annotatedTestClasses;
    private final Map<String, AnnotatedClass<Score>> annotatedScoreClasses;

    @Autowired
    public AlgorithmEndpointService(Map<String, AnnotatedClass<Algorithm>> annotatedAlgoClasses,
            Map<String, AnnotatedClass<TestOfIndependence>> annotatedTestClasses,
            Map<String, AnnotatedClass<Score>> annotatedScoreClasses) {
        this.annotatedAlgoClasses = AlgorithmAnnotations.getInstance().getAnnotatedClasses().stream()
                .collect(() -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                        (m, e) -> m.put(e.getAnnotation().command(), e),
                        (m, u) -> m.putAll(u));
        
        this.annotatedTestClasses = TestOfIndependenceAnnotations.getInstance().getAnnotatedClasses().stream()
                .collect(() -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                        (m, e) -> m.put(e.getAnnotation().command(), e),
                        (m, u) -> m.putAll(u));
        
        this.annotatedScoreClasses = ScoreAnnotations.getInstance().getAnnotatedClasses().stream()
                .collect(() -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                        (m, e) -> m.put(e.getAnnotation().command(), e),
                        (m, u) -> m.putAll(u));
    }
    
    /**
     * List all the available algorithms
     *
     * @return A list of available algorithms
     * @throws IOException
     */
    public List<AlgorithmDTO> listAlgorithms() throws IOException {
        List<AlgorithmDTO> algorithms = new LinkedList<>();

        AlgorithmAnnotations algoAnno = AlgorithmAnnotations.getInstance();
        
        List<AnnotatedClass<Algorithm>> algoAnnoList = algoAnno.filterOutExperimental(algoAnno.getAnnotatedClasses());

        algoAnnoList.stream().map((algoAnnoClass) -> algoAnnoClass.getAnnotation()).forEachOrdered((algo) -> {
            // Use command name as ID
            algorithms.add(new AlgorithmDTO(algo.command(), algo.name(), algo.description()));
        });

        return algorithms;
    }
    
    /**
     * List all the parameters of a given algorithm
     * 
     * @param algoId
     * @param testId
     * @param scoreId
     * @return A list of available parameters
     */
    public List<String> listAlgorithmParameters(String algoId, String testId, String scoreId) {
        List<String> parameters = new LinkedList<>();

        Class algoClass = getAlgorithmClass(algoId);
        Class testClass = getIndenpendenceTestClass(testId);
        Class scoreClass = getScoreClass(scoreId);
        
        // This is Tetrad Algorithm
        edu.cmu.tetrad.algcomparison.algorithm.Algorithm algorithm = null;
        
        try {
            algorithm = AlgorithmFactory.create(algoClass, testClass, scoreClass);
        } catch (IllegalAccessException | InstantiationException ex) {
            Logger.getLogger(AlgorithmEndpointService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        parameters = algorithm.getParameters();
        
        return parameters;
    }
    
    private Class getAlgorithmClass(String command) {
        if (command == null) {
            return null;
        }

        AnnotatedClass<Algorithm> annotatedClass = annotatedAlgoClasses.get(command);

        return (annotatedClass == null) ? null : annotatedClass.getClazz();
    }
    
    private Class getIndenpendenceTestClass(String command) {
        if (command == null) {
            return null;
        }

        AnnotatedClass<TestOfIndependence> annotatedClass = annotatedTestClasses.get(command);

        return (annotatedClass == null) ? null : annotatedClass.getClazz();
    }
    
    private Class getScoreClass(String command) {
        if (command == null) {
            return null;
        }

        AnnotatedClass<Score> annotatedClass = annotatedScoreClasses.get(command);

        return (annotatedClass == null) ? null : annotatedClass.getClazz();
    }
}
