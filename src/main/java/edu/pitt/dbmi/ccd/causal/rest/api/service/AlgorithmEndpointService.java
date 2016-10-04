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

import edu.pitt.dbmi.ccd.causal.rest.api.dto.AlgorithmDTO;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
@Service
public class AlgorithmEndpointService {

    private static final List<AlgorithmDTO> ALGORITHMS = new LinkedList<>();

    static {
        ALGORITHMS.add(new AlgorithmDTO(1, "fgsc", "FGS continuous"));
        ALGORITHMS.add(new AlgorithmDTO(2, "fgsd", "FGS discrete"));
        ALGORITHMS.add(new AlgorithmDTO(3, "gfcic", "GFCI continuous"));
    }

    /**
     * List all the available algorithms
     *
     * @return A list of available algorithms
     * @throws IOException
     */
    public List<AlgorithmDTO> listAlgorithms() throws IOException {
        return ALGORITHMS;
    }
}
