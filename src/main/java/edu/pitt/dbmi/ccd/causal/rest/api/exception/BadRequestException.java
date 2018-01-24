/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.causal.rest.api.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 *
 * @author Zhou Yuan <zhy19@pitt.edu>
 */
public class BadRequestException extends WebApplicationException {

    private static final long serialVersionUID = 307417604685672217L;
    
    public BadRequestException(String message) {
        super(message, Response.Status.BAD_REQUEST);
    }
}
