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
package com.connexta.alliance.nsili.endpoint.managers;

import org.slf4j.LoggerFactory;

import com.connexta.alliance.nsili.common.GIAS.Event;
import com.connexta.alliance.nsili.common.GIAS.Library;
import com.connexta.alliance.nsili.common.GIAS.Query;
import com.connexta.alliance.nsili.common.GIAS.QueryLifeSpan;
import com.connexta.alliance.nsili.common.GIAS.Request;
import com.connexta.alliance.nsili.common.GIAS.SortAttribute;
import com.connexta.alliance.nsili.common.GIAS.StandingQueryMgrPOA;
import com.connexta.alliance.nsili.common.GIAS.SubmitStandingQueryRequest;
import com.connexta.alliance.nsili.common.UCO.InvalidInputParameter;
import com.connexta.alliance.nsili.common.UCO.NameValue;
import com.connexta.alliance.nsili.common.UCO.ProcessingFault;
import com.connexta.alliance.nsili.common.UCO.SystemFault;

public class StandingQueryMgrImpl extends StandingQueryMgrPOA {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StandingQueryMgrImpl.class);

    @Override
    public Event[] get_event_descriptions() throws ProcessingFault, SystemFault {
        return new Event[0];
    }

    @Override
    public SubmitStandingQueryRequest submit_standing_query(Query aQuery,
            String[] result_attributes, SortAttribute[] sort_attributes, QueryLifeSpan lifespan,
            NameValue[] properties) throws InvalidInputParameter, ProcessingFault, SystemFault {
        LOGGER.error("StandingQueryMgr.submit_standing_query");

        return null;
    }

    @Override
    public String[] get_property_names() throws ProcessingFault, SystemFault {
        LOGGER.error("StandingQueryMgr.get_property_names");
        return new String[0];
    }

    @Override
    public NameValue[] get_property_values(String[] desired_properties)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        LOGGER.error("StandingQueryMgr.get_property_values");
        return new NameValue[0];
    }

    @Override
    public Library[] get_libraries() throws ProcessingFault, SystemFault {
        LOGGER.error("StandingQueryMgr.get_libraries");
        return new Library[0];
    }

    @Override
    public Request[] get_active_requests() throws ProcessingFault, SystemFault {
        LOGGER.error("StandingQueryMgr.get_active_requests");
        return new Request[0];
    }

    @Override
    public int get_default_timeout() throws ProcessingFault, SystemFault {
        LOGGER.error("StandingQueryMgr.get_default_timeout");
        return 0;
    }

    @Override
    public void set_default_timeout(int new_default)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        LOGGER.error("StandingQueryMgr.set_default_timeout");

    }

    @Override
    public int get_timeout(Request aRequest)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        LOGGER.error("StandingQueryMgr.get_timeout");
        return 0;
    }

    @Override
    public void set_timeout(Request aRequest, int new_lifetime)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        LOGGER.error("StandingQueryMgr.set_timeout");
    }

    @Override
    public void delete_request(Request aRequest)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        LOGGER.error("StandingQueryMgr.delete_request");
    }
}
