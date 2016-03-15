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
import org.codice.alliance.nsili.endpoint.BQSConverter;
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
import ddf.catalog.data.Result;
import ddf.catalog.federation.FederationException;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;

public class CatalogMgrImpl extends CatalogMgrPOA {

    private static final String ENCODING = "UTF-8";

    private static final int DEFAULT_TIMEOUT = -1;

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogMgrImpl.class);

    private POA poa_;

    private CatalogFramework catalogFramework;

    private long defaultTimeout = DEFAULT_TIMEOUT;

    public CatalogMgrImpl(POA poa) {
        this.poa_ = poa;
    }

    public void setCatalogFramework(CatalogFramework catalogFramework) {
        this.catalogFramework = catalogFramework;
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
        HitCountRequestImpl hitCountRequest = new HitCountRequestImpl();

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

    protected List<Result> getResults(Query aQuery) {
        List<Result> results = new ArrayList<>();

        Filter parsedFilter = BQSConverter.convertBQSToDDF(aQuery);

        FilterBuilder filterBuilder = new GeotoolsFilterBuilder();
        Filter queryFilter = filterBuilder.attribute("id")
                .like()
                .text("*");
        QueryImpl catalogQuery = new QueryImpl(queryFilter);

        if (defaultTimeout > 0) {
            catalogQuery.setTimeoutMillis(defaultTimeout);
        }

        QueryRequestImpl catalogQueryRequest = new QueryRequestImpl(catalogQuery);

        try {
            QueryResponse queryResponse = catalogFramework.query(catalogQueryRequest);
            if (queryResponse.getResults() != null) {
                results.addAll(queryResponse.getResults());
            }
        } catch (UnsupportedQueryException | SourceUnavailableException | FederationException e) {
            LOGGER.warn("Unable to query catalog", e);
        }
        return results;
    }
}
