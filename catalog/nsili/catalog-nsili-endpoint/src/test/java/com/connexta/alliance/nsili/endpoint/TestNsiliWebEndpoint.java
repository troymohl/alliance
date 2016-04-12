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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import ddf.security.service.SecurityServiceException;

public class TestNsiliWebEndpoint extends NsiliCommonTest {

    private static final int TEST_CORBA_PORT = 20011;

    private NsiliEndpoint nsiliEndpoint;

    private NsiliWebEndpoint nsiliWebEndpoint;

    @Before
    public void setup() throws SecurityServiceException {
        setupCommonMocks();
        createNsiliEndpoint();
        createWebEndpoint();
    }

    @Test
    public void testIORFile() {
        Response response = nsiliWebEndpoint.getIorFile();
        assertThat(response.getEntity(), notNullValue());

        String responseStr = response.getEntity().toString();
        assertThat(responseStr, is(nsiliEndpoint.getIorString()));
    }

    @After
    public void tearDown() {
        nsiliEndpoint.shutdown();
    }

    private void createNsiliEndpoint() {
        nsiliEndpoint = new NsiliEndpoint();
        nsiliEndpoint.setSecurityManager(securityManager);
        nsiliEndpoint.setCorbaPort(TEST_CORBA_PORT);
    }

    private void createWebEndpoint() {
        nsiliWebEndpoint = new NsiliWebEndpoint(nsiliEndpoint);
    }
}
