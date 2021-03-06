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
import edu.cmu.tetrad.annotation.AlgType;
import edu.cmu.tetrad.annotation.Algorithm;
import edu.cmu.tetrad.annotation.AlgorithmAnnotations;
import edu.cmu.tetrad.annotation.AnnotatedClass;
import edu.cmu.tetrad.annotation.Score;
import edu.cmu.tetrad.annotation.ScoreAnnotations;
import edu.cmu.tetrad.annotation.TestOfIndependence;
import edu.cmu.tetrad.annotation.TestOfIndependenceAnnotations;
import edu.cmu.tetrad.data.DataType;
import edu.cmu.tetrad.util.ParamDescription;
import edu.cmu.tetrad.util.ParamDescriptions;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.AlgoInfo;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.AlgorithmDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.AlgorithmParameterDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.exception.BadRequestException;
import java.io.Serializable;
import java.util.Arrays;
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
    
    private final Map<String, AnnotatedClass<TestOfIndependence>> annotatedTestClasses;
    
    private final Map<String, AnnotatedClass<Score>> annotatedScoreClasses;

    private AlgorithmEndpointService() {
        // Exclude pairwise algorithms since they take Graph as input
        this.annotatedAlgoClasses = AlgorithmAnnotations.getInstance().getAnnotatedClasses().stream()
                .filter(e -> (e.getAnnotation().algoType() != AlgType.orient_pairwise))
                .collect(() -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                        (m, e) -> m.put(e.getAnnotation().command(), e),
                        (m, u) -> m.putAll(u));
        
        // Exclude tests that only support Graph or Covariance data type
        this.annotatedTestClasses = TestOfIndependenceAnnotations.getInstance().getAnnotatedClasses().stream()
                // If a test supports graph, then it doesn't support other data types
                .filter(e -> !Arrays.asList(e.getAnnotation().dataType()).contains(DataType.Graph))
                // Different from graph, if a test supports covariance data, it may also support continuous or discrete data at the same time
                .filter(e -> !((Arrays.asList(e.getAnnotation().dataType()).size() == 1) && Arrays.asList(e.getAnnotation().dataType()).contains(DataType.Covariance)))
                .collect(() -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                        (m, e) -> m.put(e.getAnnotation().command(), e),
                        (m, u) -> m.putAll(u));

        // Exclude tests that only support Graph or Covariance data type
        this.annotatedScoreClasses = ScoreAnnotations.getInstance().getAnnotatedClasses().stream()
                // If a score supports graph, then it doesn't support other data types
                .filter(e -> !Arrays.asList(e.getAnnotation().dataType()).contains(DataType.Graph))
                // Different from graph, if a score supports covariance data, it may also support continuous or discrete data at the same time
                .filter(e -> !((Arrays.asList(e.getAnnotation().dataType()).size() == 1) && Arrays.asList(e.getAnnotation().dataType()).contains(DataType.Covariance)))
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

        annotatedAlgoClasses.values().forEach(annoClass -> {
            Class annotatedClass = annoClass.getClazz();
            
            Algorithm algoAnno = annoClass.getAnnotation();
            
            boolean requireTest = requireIndependenceTest(annotatedClass);
            boolean requireScore = requireScore(annotatedClass);
            boolean acceptKnowledge = acceptKnowledge(annotatedClass);

            // Use command name as ID
            algorithms.add(new AlgorithmDTO(algoAnno.command(), algoAnno.name(), algoAnno.description(), requireTest, requireScore, acceptKnowledge));
        });
  
        return algorithms;
    }

    /**
     * Get details of a given algorithm by ID
     * 
     * @param algoId
     * @return 
     */
    public AlgorithmDTO getAlgorithmDetail(String algoId) {
        AnnotatedClass<Algorithm> algoAnnoClass = annotatedAlgoClasses.get(algoId);
        
        Algorithm algoAnno = algoAnnoClass.getAnnotation();
        
        Class annotatedClass = algoAnnoClass.getClazz();
        
        boolean requireTest = requireIndependenceTest(annotatedClass);
        boolean requireScore = requireScore(annotatedClass);
        boolean acceptKnowledge = acceptKnowledge(annotatedClass);

        // Use command name as ID
        AlgorithmDTO algoDTO = new AlgorithmDTO(algoId, algoAnno.name(), algoAnno.description(), requireTest, requireScore, acceptKnowledge);
        
        return algoDTO;
    }
    
    /**
     * List all the parameters of a given algorithm, test, and score
     * @param algoInfo
     * @return 
     */
    public List<AlgorithmParameterDTO> listAlgorithmParameters(AlgoInfo algoInfo) {
        List<AlgorithmParameterDTO> algoParamsDTOs = new LinkedList<>();

        String algoId = algoInfo.getAlgoId();
        String testId = (algoInfo.getTestId() != null) ? algoInfo.getTestId() : null;
        String scoreId = (algoInfo.getScoreId() != null) ? algoInfo.getScoreId() : null;
        
        // Validate the algoId
        validateUserProvidedAlgorithm(algoId);
        
        Class clazz = getAlgorithmClass(algoId);

        boolean algoRequireTest = AlgorithmAnnotations.getInstance().requireIndependenceTest(clazz);
        boolean algoRequireScore = AlgorithmAnnotations.getInstance().requireScore(clazz);
       
        // Validate and throws exception
        validateUserProvidedTest(algoRequireTest, testId);
        validateUserProvidedScore(algoRequireScore, scoreId);

        // Get the parameters
        List<String> algoParams = getAlgoParameters(algoId, testId, scoreId);

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


        return algoParamsDTOs;
    }

    /**
     * Get a list of algorithm parameters based on the algoId, testId, and scoreID
     * 
     * @param algoId
     * @param testId
     * @param scoreId
     * @return 
     */
    public List<String> getAlgoParameters(String algoId, String testId, String scoreId) {
        Class algoClass = annotatedAlgoClasses.get(algoId).getClazz();
        // Test or Score can be empty, so the corresponding class can be null
        Class testClass = (testId == null) ? null : annotatedTestClasses.get(testId).getClazz();
        Class scoreClass = (scoreId == null) ? null : annotatedScoreClasses.get(scoreId).getClazz();
        
        // This is Tetrad Algorithm
        edu.cmu.tetrad.algcomparison.algorithm.Algorithm algorithm = null;
        
        try {
            algorithm = AlgorithmFactory.create(algoClass, testClass, scoreClass);
        } catch (IllegalAccessException | InstantiationException ex) {
            LOGGER.error(String.format("Failed to create Algorithm instance for algoId='%s', testId='%s', scoreId='%s'.", algoId, testId, scoreId));
        }
        
        return (algorithm != null) ? algorithm.getParameters() : null;
    }
    
    public void validateUserProvidedAlgorithm(String algoId) {
        if (!annotatedAlgoClasses.containsKey(algoId)) {
            throw new BadRequestException("Invalid 'algoId' value: " + algoId);
        }
    }
    
    public void validateUserProvidedTest(boolean algoRequireTest, String testId) {
        if (algoRequireTest) {
            if (testId == null) {
                throw new BadRequestException("Missing 'testId', this algorithm requires an Indenpendent Test.");   
            } else {
                if (testId.isEmpty()) {
                    throw new BadRequestException("The value of 'testId' can't be empty.");   
                } else {
                    if (!annotatedTestClasses.containsKey(testId)) {
                        throw new BadRequestException("Invalid 'testId' value: " + testId);
                    }
                }
            }
        } else {
            if (testId != null) {
                throw new BadRequestException("Unrecognized option 'testId', this algorithm doesn't use an Indenpendent Test.");   
            }
        }
    }
    
    public void validateUserProvidedScore(boolean algoRequireScore, String scoreId) {
        if (algoRequireScore) {
            if (scoreId == null) {
                throw new BadRequestException("Missing 'scoreId', this algorithm requires a Score.");   
            } else {
                if (scoreId.isEmpty()) {
                    throw new BadRequestException("The value of 'scoreId' can't be empty.");   
                } else {
                    if (!annotatedScoreClasses.containsKey(scoreId)) {
                        throw new BadRequestException("Invalid 'scoreId' value: " + scoreId);
                    }
                }
            }
        } else {
            if (scoreId != null) {
                throw new BadRequestException("Unrecognized option 'scoreId', this algorithm doesn't use a Score.");   
            }
        }
    }
    
    public Class getAlgorithmClass(String algoId) {
        return annotatedAlgoClasses.get(algoId).getClazz();
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
