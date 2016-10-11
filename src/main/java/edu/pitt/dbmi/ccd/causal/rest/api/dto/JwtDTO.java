/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.dto;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author Zhou Yuan (zhy19@pitt.edu)
 */
@XmlRootElement(name = "JWT")
@XmlAccessorType(XmlAccessType.FIELD)
public class JwtDTO {

    @XmlElement
    private Long userId;

    @XmlElement
    private String jwt;

    @XmlElement
    private Date issuedTime;

    @XmlElement
    @Value("${ccd.jwt.lifetime}")
    private long lifetime;

    @XmlElement
    private Date expireTime;

    public JwtDTO() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public Date getIssuedTime() {
        return issuedTime;
    }

    public void setIssuedTime(Date issuedTime) {
        this.issuedTime = issuedTime;
    }

    public long getLifetime() {
        return lifetime;
    }

    public void setLifetime(long lifetime) {
        this.lifetime = lifetime;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

}
