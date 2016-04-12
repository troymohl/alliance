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
package com.connexta.alliance.nsili.endpoint.requests;

import com.connexta.alliance.nsili.common.CB.Callback;
import com.connexta.alliance.nsili.common.GIAS.DelayEstimate;
import com.connexta.alliance.nsili.common.GIAS.GetRelatedFilesRequestPOA;
import com.connexta.alliance.nsili.common.GIAS.RequestManager;
import com.connexta.alliance.nsili.common.GIAS._RequestManagerStub;
import com.connexta.alliance.nsili.common.UCO.InvalidInputParameter;
import com.connexta.alliance.nsili.common.UCO.NameListHolder;
import com.connexta.alliance.nsili.common.UCO.ProcessingFault;
import com.connexta.alliance.nsili.common.UCO.RequestDescription;
import com.connexta.alliance.nsili.common.UCO.State;
import com.connexta.alliance.nsili.common.UCO.Status;
import com.connexta.alliance.nsili.common.UCO.SystemFault;

public class GetRelatedFilesRequestImpl extends GetRelatedFilesRequestPOA {

    @Override
    public State complete (NameListHolder locations) throws ProcessingFault, SystemFault {
        String[] fileLocations = {};
        locations.value = fileLocations;
        return State.COMPLETED;
    }

    @Override
    public RequestDescription get_request_description() throws ProcessingFault, SystemFault {
        return new RequestDescription();
    }

    @Override
    public void set_user_info(String message)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        return;
    }

    @Override
    public Status get_status() throws ProcessingFault, SystemFault {
        return new Status();
    }

    @Override
    public DelayEstimate get_remaining_delay() throws ProcessingFault, SystemFault {
        return new DelayEstimate();
    }

    @Override
    public void cancel() throws ProcessingFault, SystemFault {
        return;
    }

    @Override
    public String register_callback(Callback acallback)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        return "";
    }

    @Override
    public void free_callback(String id)
            throws InvalidInputParameter, ProcessingFault, SystemFault {
        return;
    }

    @Override
    public RequestManager get_request_manager() throws ProcessingFault, SystemFault {
        return new _RequestManagerStub();
    }
}
