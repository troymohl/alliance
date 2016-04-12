/**
 * Copyright (c) Connexta, LLC
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package com.connexta.alliance.nsili.endpoint;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connexta.alliance.nsili.common.NsiliWeb;

@Path("/")
public class NsiliWebEndpoint implements NsiliWeb {

    private static final Logger LOGGER = LoggerFactory.getLogger(NsiliWebEndpoint.class);

    private NsiliEndpoint nsiliEndpoint;

    public NsiliWebEndpoint(NsiliEndpoint nsiliEndpoint) {
        LOGGER.debug("NSILI Web Endpoint has been constructed");
        this.nsiliEndpoint = nsiliEndpoint;
    }

    public void setNsiliEndpoint(NsiliEndpoint nsiliEndpoint) {
        this.nsiliEndpoint = nsiliEndpoint;
    }

    @Override
    @GET
    @Path("ior.txt")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getIorFile() {
        Response.ResponseBuilder response = Response.ok(nsiliEndpoint.getIorString());
        return response.build();
    }
}
