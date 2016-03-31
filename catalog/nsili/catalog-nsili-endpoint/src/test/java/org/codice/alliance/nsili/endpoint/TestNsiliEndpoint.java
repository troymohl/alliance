/**
 * Copyright (c) Codice Foundation
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
package org.codice.alliance.nsili.endpoint;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import ddf.security.service.SecurityManager;
import ddf.security.Subject;
import ddf.security.service.SecurityServiceException;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.ExecutionException;
import org.apache.shiro.subject.PrincipalCollection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestNsiliEndpoint extends NsiliCommonTest {
    public static final int TEST_CORBA_PORT = 20010;

    private NsiliEndpoint nsiliEndpoint;

    @Before
    public void setup() throws SecurityServiceException {
        createEndpoint();
        setupCommonMocks();
    }

    @Test
    public void testIORString() {
        String iorString = nsiliEndpoint.getIorString();
        assertThat(iorString, notNullValue());
    }

    @Test
    public void testEndpointManagers() {
        //This should be empty until managers are implements and added to the endpoint

    }

    @After
    public void tearDown() {
        nsiliEndpoint.shutdown();
    }

    private void createEndpoint() {
        nsiliEndpoint = new NsiliEndpoint();
        nsiliEndpoint.setSecurityManager(securityManager);
        nsiliEndpoint.setCorbaPort(TEST_CORBA_PORT);
    }
}
