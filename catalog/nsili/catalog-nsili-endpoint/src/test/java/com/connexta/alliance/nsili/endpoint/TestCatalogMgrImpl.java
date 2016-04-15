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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import com.connexta.alliance.nsili.common.GIAS.HitCountRequest;
import com.connexta.alliance.nsili.common.GIAS.Query;
import com.connexta.alliance.nsili.common.GIAS.Request;
import com.connexta.alliance.nsili.common.GIAS.SortAttribute;
import com.connexta.alliance.nsili.common.GIAS.SubmitQueryRequest;
import com.connexta.alliance.nsili.common.NsiliConstants;
import com.connexta.alliance.nsili.common.UCO.DAGListHolder;
import com.connexta.alliance.nsili.common.UCO.InvalidInputParameter;
import com.connexta.alliance.nsili.common.UCO.ProcessingFault;
import com.connexta.alliance.nsili.common.UCO.SystemFault;
import com.connexta.alliance.nsili.endpoint.managers.CatalogMgrImpl;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Result;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.ResultImpl;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.impl.QueryResponseImpl;
import ddf.security.Subject;
import ddf.security.service.SecurityServiceException;

public class TestCatalogMgrImpl extends NsiliCommonTest {

    private ORB orb = null;

    private LibraryImpl library;

    private POA rootPOA;

    private Thread orbRunThread = null;

    private CatalogFramework mockCatalogFramework = mock(CatalogFramework.class);

    private CatalogMgrImpl catalogMgr;

    private Query testQuery;

    private String bqsQuery = "NSIL_CARD.identifier like '%'";

    @Before
    public void setup() throws Exception {
        setupCommonMocks();
        setupCatalogMgrMocks();
        try {
            setupOrb();
            orbRunThread = new Thread(() -> orb.run());
            orbRunThread.start();
        } catch (InvalidName | AdapterInactive | WrongPolicy | ServantNotActive e) {
            System.err.println("Unable to start the CORBA server. " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Unable to generate the IOR file." + e.getMessage());
        } catch (SecurityServiceException e) {
            System.err.println("Unable to setup guest security credentials" + e.getMessage());
        }

        testQuery = new Query(NsiliConstants.NSIL_ALL_VIEW, bqsQuery);

        catalogMgr = new CatalogMgrImpl(rootPOA, new GeotoolsFilterBuilder());
        catalogMgr.setGuestSubject(mockSubject);
        catalogMgr.setCatalogFramework(mockCatalogFramework);
    }

    @Test
    public void testTimeoutSettings() throws InvalidInputParameter, SystemFault, ProcessingFault {
        int timeoutSec = 500;
        catalogMgr.set_default_timeout(timeoutSec);
        int timeout = catalogMgr.get_default_timeout();
        assertThat(timeoutSec, is(timeout));
    }

    @Test
    public void testRequests() throws SystemFault, ProcessingFault, InvalidInputParameter {
        Request[] activeReqs = catalogMgr.get_active_requests();
        assertThat(activeReqs.length, is(0));

        catalogMgr.delete_request(null);
    }

    @Test
    public void testHitCount() throws InvalidInputParameter, SystemFault, ProcessingFault {
        HitCountRequest hitCountRequest = catalogMgr.hit_count(testQuery, null);
        IntHolder hitHolder = new IntHolder();
        assertThat(hitCountRequest, notNullValue());

        hitCountRequest.complete(hitHolder);
        assertThat(hitHolder.value, greaterThan(0));
    }

    @Test
    public void testQuery() throws InvalidInputParameter, SystemFault, ProcessingFault {
        String[] resultAttributes = null;
        SortAttribute[] sortAttributes = null;
        SubmitQueryRequest submitQueryRequest = catalogMgr.submit_query(testQuery,
                resultAttributes,
                sortAttributes,
                null);
        assertThat(submitQueryRequest, notNullValue());

        DAGListHolder dagListHolder = new DAGListHolder();
        submitQueryRequest.complete_DAG_results(dagListHolder);
        assertThat(dagListHolder.value, notNullValue());
        assertThat(dagListHolder.value.length, greaterThan(0));
    }

    private void setupCatalogMgrMocks() throws Exception {
        int testTotalHits = 5;
        List<Result> results = new ArrayList<>(testTotalHits);
        MetacardImpl testMetacard = new MetacardImpl();
        testMetacard.setId(UUID.randomUUID()
                .toString());
        Result testResult = new ResultImpl(testMetacard);
        results.add(testResult);
        QueryResponse testResponse = new QueryResponseImpl(null, results, testTotalHits);
        when(mockCatalogFramework.query(any(QueryRequest.class))).thenReturn(testResponse);

    }

    private void setupOrb()
            throws InvalidName, AdapterInactive, WrongPolicy, ServantNotActive, IOException,
            SecurityServiceException {

        //Let the system find the next avail port and use that
        int port = 0;
        java.util.Properties props = new java.util.Properties();
        props.put("org.omg.CORBA.ORBInitialPort", port);
        orb = ORB.init(new String[0], props);

        rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

        rootPOA.the_POAManager()
                .activate();

        library = new LibraryImpl(rootPOA);
        library.setCatalogFramework(mockCatalogFramework);
        Subject guestSubject = mockSubject;
        library.setGuestSubject(guestSubject);
        library.setFilterBuilder(new GeotoolsFilterBuilder());

        org.omg.CORBA.Object objref = rootPOA.servant_to_reference(library);

        rootPOA.the_POAManager()
                .activate();
    }

    @After
    public void shutdown() {
        if (orb != null) {
            orb.destroy();
        }

        if (orbRunThread != null) {
            orbRunThread.interrupt();
            orbRunThread = null;
        }

        library = null;
    }
}
