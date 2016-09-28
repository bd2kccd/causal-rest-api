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

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
@XmlRootElement(name = "jobInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class JobInfoDTO {

    @XmlElement
    private Long id;

    @XmlElement
    private String algorithmName;

    @XmlElement
    private int status;
    
    @XmlElement
    private Date addedTime;

    @XmlElement
    private String resultFileName;
    
    @XmlElement
    private String resultJsonFileName;

    @XmlElement
    private String errorResultFileName;
    

    public JobInfoDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getAddedTime() {
        return addedTime;
    }

    public void setAddedTime(Date addedTime) {
        this.addedTime = addedTime;
    }

    public String getResultFileName() {
        return resultFileName;
    }

    public void setResultFileName(String resultFileName) {
        this.resultFileName = resultFileName;
    }

    public String getResultJsonFileName() {
        return resultJsonFileName;
    }

    public void setResultJsonFileName(String resultJsonFileName) {
        this.resultJsonFileName = resultJsonFileName;
    }

    public String getErrorResultFileName() {
        return errorResultFileName;
    }

    public void setErrorResultFileName(String errorResultFileName) {
        this.errorResultFileName = errorResultFileName;
    }

}
