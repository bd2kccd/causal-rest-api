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
package edu.pitt.dbmi.ccd.causal.rest.api.ctrl.data;

import edu.pitt.dbmi.ccd.causal.rest.api.dto.data.DataFileDTO;
import edu.pitt.dbmi.ccd.causal.rest.api.service.data.DataManagementRestService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * Jun 3, 2016 3:36:45 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@RestController
@RequestMapping(value = "/usr/{usr}/data")
public class DataManagementRestController {

    private final DataManagementRestService dataManagementRestService;

    @Autowired
    public DataManagementRestController(DataManagementRestService dataManagementRestService) {
        this.dataManagementRestService = dataManagementRestService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> listUserDataFiles(@PathVariable("usr") String username) {
        List<DataFileDTO> dataFileDTOs = dataManagementRestService.listUserDataFiles(username);

        return new ResponseEntity<>(dataFileDTOs, HttpStatus.OK);
    }

}
