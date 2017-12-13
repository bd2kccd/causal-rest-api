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

import edu.cmu.tetrad.annotation.Algorithm;
import edu.cmu.tetrad.annotation.AlgorithmAnnotations;
import edu.cmu.tetrad.annotation.AnnotatedClass;
import edu.pitt.dbmi.ccd.causal.rest.api.dto.AlgorithmDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.prop.CausalRestProperties;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
@Service
public class AlgorithmEndpointService {

    private final CausalRestProperties causalRestProperties;

    @Autowired
    public AlgorithmEndpointService(CausalRestProperties causalRestProperties) {
        this.causalRestProperties = causalRestProperties;
    }

    /**
     * List all the available algorithms
     *
     * @return A list of available algorithms
     * @throws IOException
     */
    public List<AlgorithmDTO> listAlgorithms() throws IOException {
        List<AlgorithmDTO> ALGORITHMS = new LinkedList<>();

        
        AlgorithmAnnotations algoAnno = AlgorithmAnnotations.getInstance();
        
        List<AnnotatedClass<Algorithm>> algoAnnoList = algoAnno.filterOutExperimental(algoAnno.getAnnotatedClasses());

        for (AnnotatedClass<Algorithm> algoAnnoClass: algoAnnoList) {
            Algorithm algo = algoAnnoClass.getAnnotation();
            
            // Use command name as ID
            ALGORITHMS.add(new AlgorithmDTO(algo.command(), algo.name(), algo.description()));
        }
        
        // Get the actual algorithm short name from the properties file
//        ALGORITHMS.add(new AlgorithmDTO(1, causalRestProperties.getAlgoFgesCont(), "FGES continuous"));
//        ALGORITHMS.add(new AlgorithmDTO(2, causalRestProperties.getAlgoFgesDisc(), "FGES discrete"));
//        ALGORITHMS.add(new AlgorithmDTO(3, causalRestProperties.getAlgoGfciCont(), "GFCI continuous"));
//        ALGORITHMS.add(new AlgorithmDTO(4, causalRestProperties.getAlgoGfciDisc(), "GFCI discrete"));

        return ALGORITHMS;
    }
}
