/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.dto;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Zhou Yuan <zhy19@pitt.edu>
 */
@XmlRootElement(name = "AlgorithmParameters")
@XmlAccessorType(XmlAccessType.FIELD)
public class AlgorithmParameterDTO {
    @XmlElement
    private String name;

    @XmlElement
    private String description;
    
    @XmlElement
    private Serializable defaultValue;
    
    @XmlElement
    private String valueType;

    public AlgorithmParameterDTO() {
    }

    public AlgorithmParameterDTO(String name, String description, Serializable defaultValue, String valueType) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.valueType = valueType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Serializable getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Serializable defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }
    
    
}
