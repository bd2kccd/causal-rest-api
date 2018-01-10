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
import edu.cmu.tetrad.util.ParamDescription;
import edu.cmu.tetrad.util.ParamDescriptions;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.AlgoInfo;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.AlgorithmDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.AlgorithmParameterDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.BadRequestException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
@Service
public class AlgorithmEndpointService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AlgorithmEndpointService.class);
    
    private final Map<String, AnnotatedClass<Algorithm>> annotatedAlgoClasses;

    private AlgorithmEndpointService() {
        this.annotatedAlgoClasses = AlgorithmAnnotations.getInstance().getAnnotatedClasses().stream()
                .collect(() -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                        (m, e) -> m.put(e.getAnnotation().command(), e),
                        (m, u) -> m.putAll(u));
    
    }

    /**
     * List all the available algorithms
     *
     * @return A list of available algorithms
     */
    public List<AlgorithmDTO> listAlgorithms() {
        List<AlgorithmDTO> algorithms = new LinkedList<>();

        AlgorithmAnnotations algoAnno = AlgorithmAnnotations.getInstance();
        
        List<AnnotatedClass<Algorithm>> algoAnnoList = algoAnno.filterOutExperimental(algoAnno.getAnnotatedClasses());

        algoAnnoList.stream().map((algoAnnoClass) -> algoAnnoClass.getAnnotation()).forEachOrdered((algo) -> {
        
            Class annotatedClass = annotatedAlgoClasses.get(algo.command()).getClazz();
            
            boolean requireTest = requireIndependenceTest(annotatedClass);
            boolean requireScore = requireScore(annotatedClass);
            boolean acceptKnowledge = acceptKnowledge(annotatedClass);
        
            // Use command name as ID
            algorithms.add(new AlgorithmDTO(algo.command(), algo.name(), algo.description(), requireTest, requireScore, acceptKnowledge));
        });

        return algorithms;
    }

    /**
     * List all the parameters of a given algorithm, test, and score
     * @param algoInfo
     * @return 
     */
    public List<AlgorithmParameterDTO> listAlgorithmParameters(AlgoInfo algoInfo) {
        List<AlgorithmParameterDTO> algoParamsDTOs = new LinkedList<>();

        String algoId = algoInfo.getAlgoId();
        String testId = algoInfo.getTestId();
        String scoreId = algoInfo.getScoreId();
        
        if (!annotatedAlgoClasses.containsKey(algoId)) {
            throw new BadRequestException("Invalid 'algoId' value: " + algoId);
        }
        
        Class clazz = annotatedAlgoClasses.get(algoId).getClazz();

        boolean algoRequireTest = AlgorithmAnnotations.getInstance().requireIndependenceTest(clazz);
        boolean algoRequireScore = AlgorithmAnnotations.getInstance().requireScore(clazz);
        
        Map<String, AnnotatedClass<TestOfIndependence>> annotatedTestClasses = TestOfIndependenceAnnotations.getInstance().getAnnotatedClasses().stream()
                .collect(() -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                        (m, e) -> m.put(e.getAnnotation().command(), e),
                        (m, u) -> m.putAll(u));
        
        if (algoRequireTest) {
            if (testId.isEmpty()) {
                throw new BadRequestException("'testId' needs to be specified, it can't be empty.");   
            } else {
                if (!annotatedTestClasses.containsKey(testId)) {
                    throw new BadRequestException("Invalid 'testId' value: " + testId);
                }
            }
        }

        
        Map<String, AnnotatedClass<Score>> annotatedScoreClasses = ScoreAnnotations.getInstance().getAnnotatedClasses().stream()
                .collect(() -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                        (m, e) -> m.put(e.getAnnotation().command(), e),
                        (m, u) -> m.putAll(u));
        
        if (algoRequireScore) {
            if (scoreId.isEmpty()) {
                throw new BadRequestException("'scoreId' needs to be specified, it can't be empty.");   
            } else {
                if (!annotatedScoreClasses.containsKey(scoreId)) {
                    throw new BadRequestException("Invalid 'scoreId' value: " + scoreId);
                }
            }
        }
 
        Class algoClass = annotatedAlgoClasses.get(algoId).getClazz();
        // Test or Score can be empty, so the corresponding class can be null
        Class testClass = (annotatedTestClasses.get(testId) == null) ? null : annotatedTestClasses.get(testId).getClazz();
        Class scoreClass = (annotatedScoreClasses.get(scoreId) == null) ? null : annotatedScoreClasses.get(scoreId).getClazz();
        
        // This is Tetrad Algorithm
        edu.cmu.tetrad.algcomparison.algorithm.Algorithm algorithm = null;
        
        try {
            algorithm = AlgorithmFactory.create(algoClass, testClass, scoreClass);
        } catch (IllegalAccessException | InstantiationException ex) {
            LOGGER.error(String.format("Failed to create Algorithm instance for algoId='%s', testId='%s', scoreId='%s'.", algoId, testId, scoreId));
        }
        
        if (algorithm != null) {
            List<String> algoParams = algorithm.getParameters();
            
            algoParams.forEach((param) -> {
                ParamDescription paramDesc = ParamDescriptions.getInstance().get(param);
                Serializable defaultValue = paramDesc.getDefaultValue();
                String valueType = "";
                if (defaultValue instanceof Integer) {
                    valueType = "Integer";
                }
                
                if (defaultValue instanceof Double) {
                    valueType = "Double";
                }
                
                if (defaultValue instanceof Boolean) {
                    valueType = "Boolean";
                }
                
                algoParamsDTOs.add(new AlgorithmParameterDTO(param, paramDesc.getDescription(), valueType, defaultValue));
            });
        }

        return algoParamsDTOs;
    }
    
    public boolean requireIndependenceTest(Class clazz) {
        return AlgorithmAnnotations.getInstance().requireIndependenceTest(clazz);
    }

    public boolean requireScore(Class clazz) {
        return AlgorithmAnnotations.getInstance().requireScore(clazz);
    }

    public boolean acceptKnowledge(Class clazz) {
        return AlgorithmAnnotations.getInstance().acceptKnowledge(clazz);
    }
    
}
