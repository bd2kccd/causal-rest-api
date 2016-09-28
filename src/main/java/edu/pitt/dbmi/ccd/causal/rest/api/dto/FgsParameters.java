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
package edu.pitt.dbmi.ccd.causal.rest.api.dto;

import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
public abstract class FgsParameters {

    protected int maxDegree;

    @Value("true")
    protected boolean faithfulnessAssumed;

    @Value("true")
    protected boolean verbose;

    public FgsParameters() {
    }

    public int getMaxDegree() {
        return maxDegree;
    }

    public void setDepth(int maxDegree) {
        this.maxDegree = maxDegree;
    }

    public boolean isFaithfulnessAssumed() {
        return faithfulnessAssumed;
    }

    public void setFaithfulnessAssumed(boolean faithfulnessAssumed) {
        this.faithfulnessAssumed = faithfulnessAssumed;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

}
