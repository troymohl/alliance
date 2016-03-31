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
package org.codice.alliance.nsili.endpoint.managers;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.codice.alliance.nsili.common.BqsConverter;
import org.codice.alliance.nsili.common.GIAS.CatalogMgrPOA;
import org.codice.alliance.nsili.common.GIAS.HitCountRequest;
import org.codice.alliance.nsili.common.GIAS.HitCountRequestHelper;
import org.codice.alliance.nsili.common.GIAS.Library;
import org.codice.alliance.nsili.common.GIAS.Query;
import org.codice.alliance.nsili.common.GIAS.Request;
import org.codice.alliance.nsili.common.GIAS.SortAttribute;
import org.codice.alliance.nsili.common.GIAS.SubmitQueryRequest;
import org.codice.alliance.nsili.common.GIAS.SubmitQueryRequestHelper;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.NameValue;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.endpoint.requests.HitCountRequestImpl;
import org.codice.alliance.nsili.endpoint.requests.SubmitQueryRequestImpl;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.opengis.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.security.Subject;

public class CatalogMgrImpl extends CatalogMgrPOA {

    private static final String ENCODING = "UTF-8";

    private static final int DEFAULT_TIMEOUT = -1;

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogMgrImpl.class);

    private POA poa_;

    private CatalogFramework catalogFramework;

    private long defaultTimeout = DEFAULT_TIMEOUT;

    private BqsConverter bqsConverter;

    private Subject guestSubject;

    public CatalogMgrImpl(POA poa) {
        this.poa_ = poa;
        bqsConverter = new BqsConverter();
    }

    public void setCatalogFramework(CatalogFramework catalogFramework) {
        this.catalogFramework = catalogFramework;
    }

    public void setGuestSubject(Subject guestSubject) {
        this.guestSubject = guestSubject;
    }

    @Override
    public Request[] get_active_requests() throws ProcessingFault, SystemFault {
        return new Request[0];
    }

    @Override
    public int get_default_timeout() throws ProcessingFault, SystemFault {
        return (int)defaultTimeout;
    }

    @Override
    public void set_default_timeout(int new_default)
            throws ProcessingFault, InvalidInputParameter, SystemFault {
        this.defaultTimeout = new_default;
    }

    @Override
    public int get_timeout(Request aRequest)
            throws ProcessingFault, InvalidInputParameter, SystemFault {
        return DEFAULT_TIMEOUT;
    }

    @Override
    public void set_timeout(Request aRequest, int new_lifetime)
            throws ProcessingFault, InvalidInputParameter, SystemFault {
        //Nothing to do here, as the query has already executed
    }

    @Override
    public void delete_request(Request aRequest)
            throws ProcessingFault, InvalidInputParameter, SystemFault {
        //Nothing to do here, we don't keep a list of requests. We work them as they are submitted.
    }

    @Override
    public SubmitQueryRequest submit_query(Query aQuery, String[] result_attributes,
            SortAttribute[] sort_attributes, NameValue[] properties)
            throws ProcessingFault, InvalidInputParameter, SystemFault {

            LOGGER.warn("Query submitted");

        SubmitQueryRequestImpl submitQueryRequest = new SubmitQueryRequestImpl();

        String queryId = UUID.randomUUID().toString();
        try {
            poa_.activate_object_with_id(queryId.getBytes(Charset.forName(ENCODING)),
                    submitQueryRequest);
        } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
            System.out.println("submit_query : Unable to activate submitQueryRequest object.");
        }

        org.omg.CORBA.Object obj =
                poa_.create_reference_with_id(queryId.getBytes(Charset.forName(ENCODING)),
                        SubmitQueryRequestHelper.id());
        SubmitQueryRequest queryRequest = SubmitQueryRequestHelper.narrow(obj);
        submitQueryRequest.setQueryResults(getResults(aQuery));
        return queryRequest;
    }

    @Override
    public HitCountRequest hit_count(Query aQuery, NameValue[] properties)
            throws ProcessingFault, InvalidInputParameter, SystemFault {
        //Force this to be an int per the NSILI API
        int numResults = (int)getResultCount(aQuery);

        HitCountRequestImpl hitCountRequest = new HitCountRequestImpl(numResults);

        try {
            poa_.activate_object_with_id("hit_count".getBytes(Charset.forName(ENCODING)),
                    hitCountRequest);
        } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
            System.out.println("hit_count : Unable to activate hitCountRequest object.");
        }

        org.omg.CORBA.Object obj =
                poa_.create_reference_with_id("hit_count".getBytes(Charset.forName(ENCODING)),
                        HitCountRequestHelper.id());
        HitCountRequest queryRequest = HitCountRequestHelper.narrow(obj);

        return queryRequest;
    }

    @Override
    public String[] get_property_names() throws ProcessingFault, SystemFault {
        //Per the spec throw NO_IMPLEMENT
        throw new NO_IMPLEMENT();
    }

    @Override
    public NameValue[] get_property_values(String[] desired_properties)
            throws ProcessingFault, InvalidInputParameter, SystemFault {
        //Per the spec throw NO_IMPLEMENT
        throw new NO_IMPLEMENT();
    }

    @Override
    public Library[] get_libraries() throws ProcessingFault, SystemFault {
        //Per the spec throw NO_IMPLEMENT
        throw new NO_IMPLEMENT();
    }

    protected long getResultCount(Query aQuery) {
        long resultCount = 0;

        Filter parsedFilter = bqsConverter.convertBQSToDDF(aQuery);

        FilterBuilder filterBuilder = new GeotoolsFilterBuilder();
        Filter queryFilter = filterBuilder.attribute(Metacard.ANY_TEXT)
                .like()
                .text("*");

        //TODO REMOVE
        LOGGER.warn("Filter: "+queryFilter.toString());

        QueryImpl catalogQuery = new QueryImpl(queryFilter);

        if (defaultTimeout > 0) {
            catalogQuery.setTimeoutMillis(defaultTimeout);
        }

        catalogQuery.setPageSize(1);

        QueryRequestImpl catalogQueryRequest = new QueryRequestImpl(catalogQuery);

        try {
            QueryCountCallable queryCallable = new QueryCountCallable(catalogQueryRequest);
            resultCount = guestSubject.execute(queryCallable);

        } catch (Exception e) {
            LOGGER.warn("Unable to query catalog", e);
        }

        //TODO REMOVE
        LOGGER.warn("Total result count: "+resultCount);
        return resultCount;
    }

    protected List<Result> getResults(Query aQuery) {
        List<Result> results = new ArrayList<>();

        Filter parsedFilter = bqsConverter.convertBQSToDDF(aQuery);

        FilterBuilder filterBuilder = new GeotoolsFilterBuilder();
        Filter queryFilter = filterBuilder.attribute(Metacard.ANY_TEXT)
                .like()
                .text("*");

        //TODO REMOVE
        LOGGER.warn("Filter: "+queryFilter.toString());

        QueryImpl catalogQuery = new QueryImpl(queryFilter);

        if (defaultTimeout > 0) {
            catalogQuery.setTimeoutMillis(defaultTimeout);
        }

        QueryRequestImpl catalogQueryRequest = new QueryRequestImpl(catalogQuery);

        try {
            QueryResultsCallable queryCallable = new QueryResultsCallable(catalogQueryRequest);
            results.addAll(guestSubject.execute(queryCallable));

        } catch (Exception e) {
            LOGGER.warn("Unable to query catalog", e);
        }

        //TODO REMOVE
        LOGGER.warn("Return results: "+results.size());
        return results;
    }

    class QueryResultsCallable implements Callable<List<Result>> {
        QueryRequestImpl catalogQueryRequest;

        public QueryResultsCallable(QueryRequestImpl catalogQueryRequest) {
            this.catalogQueryRequest = catalogQueryRequest;
        }

        @Override
        public List<Result> call() throws Exception {
            List<Result> results = new ArrayList<>();
            QueryResponse queryResponse = catalogFramework.query(catalogQueryRequest);
            queryResponse.getHits();
            if (queryResponse.getResults() != null) {

                //TODO REMOVE
                LOGGER.warn("Number of results: "+queryResponse.getResults().size());
                results.addAll(queryResponse.getResults());
            } else {
                //TODO REMOVE
                LOGGER.warn("No results returned");
            }
            return results;
        }
    }

    class QueryCountCallable implements Callable<Long> {
        QueryRequestImpl catalogQueryRequest;

        public QueryCountCallable(QueryRequestImpl catalogQueryRequest) {
            this.catalogQueryRequest = catalogQueryRequest;
        }

        @Override
        public Long call() throws Exception {
            QueryResponse queryResponse = catalogFramework.query(catalogQueryRequest);
            return queryResponse.getHits();
        }
    }
}
