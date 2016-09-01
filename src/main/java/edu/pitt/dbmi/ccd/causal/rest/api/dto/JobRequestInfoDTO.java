package edu.pitt.dbmi.ccd.causal.rest.api.dto;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Aug 30, 2016 12:21:48 AM
 * 
 * @author Chirayu (Kong) Wongchokprasitti, PhD
 * 
 */
@XmlRootElement(name = "JobRequestInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class JobRequestInfoDTO {

    @XmlElement
    private Long id;

    @XmlElement
    private String algorithmName;

    @XmlElement
    private Date addedTime;

    @XmlElement
    private String resultFileName;

    @XmlElement
    private String errorResultFileName;

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

    public String getErrorResultFileName() {
	return errorResultFileName;
    }

    public void setErrorResultFileName(String errorResultFileName) {
	this.errorResultFileName = errorResultFileName;
    }
    
}
