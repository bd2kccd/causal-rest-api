/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.dto;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Zhou Yuan <zhy19@pitt.edu>
 */
@XmlRootElement(name = "Score")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScoreDTO {
    @XmlElement
    private String id;
    
    @XmlElement
    private String name;
    
    @XmlElement
    private List<String> supportedDataTypes;

    public ScoreDTO() {
    }

    public ScoreDTO(String id, String name, List<String> supportedDataTypes) {
        this.id = id;
        this.name = name;
        this.supportedDataTypes = supportedDataTypes;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSupportedDataTypes() {
        return supportedDataTypes;
    }

    public void setSupportedDataTypes(List<String> supportedDataTypes) {
        this.supportedDataTypes = supportedDataTypes;
    }

}
