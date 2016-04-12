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
package com.connexta.alliance.nsili.mockserver.impl;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import com.connexta.alliance.nsili.common.GIAS.AccessCriteria;
import com.connexta.alliance.nsili.common.GIAS.CatalogMgrHelper;
import com.connexta.alliance.nsili.common.GIAS.DataModelMgrHelper;
import com.connexta.alliance.nsili.common.GIAS.LibraryDescription;
import com.connexta.alliance.nsili.common.GIAS.LibraryManager;
import com.connexta.alliance.nsili.common.GIAS.LibraryManagerHelper;
import com.connexta.alliance.nsili.common.GIAS.LibraryPOA;
import com.connexta.alliance.nsili.common.GIAS.OrderMgrHelper;
import com.connexta.alliance.nsili.common.GIAS.ProductMgrHelper;
import com.connexta.alliance.nsili.common.UCO.InvalidInputParameter;
import com.connexta.alliance.nsili.common.UCO.ProcessingFault;
import com.connexta.alliance.nsili.common.UCO.SystemFault;
import com.connexta.alliance.nsili.common.UCO.exception_details;
import com.connexta.alliance.nsili.mockserver.impl.managers.OrderMgrImpl;

import com.connexta.alliance.nsili.mockserver.impl.managers.CatalogMgrImpl;
import com.connexta.alliance.nsili.mockserver.impl.managers.DataModelMgrImpl;
import com.connexta.alliance.nsili.mockserver.impl.managers.ProductMgrImpl;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.PortableServer.POA;

public class LibraryImpl extends LibraryPOA {

    private LibraryDescription libraryDescription = new LibraryDescription("localhost",
            "US|AFRL/IFEC",
            "NSILI|1.0");

    private List<String> manager = Arrays.asList("OrderMgr",
            "CatalogMgr",
            "ProductMgr",
            "DataModelMgr"
            /* Optional :
            "QueryOrderMgr",
            "StandingQueryMgr",
            "CreationMgr",
            "UpdateMgr" */);

    private static final String ENCODING = "UTF-8";

    private POA poa_;

    public LibraryImpl(POA poa_) {
        this.poa_ = poa_;
    }

    @Override
    public String[] get_manager_types() throws ProcessingFault, SystemFault {
        return (String[]) manager.toArray();
    }

    @Override
    public LibraryManager get_manager(String manager_type, AccessCriteria access_criteria)
            throws ProcessingFault, InvalidInputParameter, SystemFault {

        org.omg.CORBA.Object obj;

        switch (manager_type) {
        case "CatalogMgr":
            CatalogMgrImpl catalogMgr = new CatalogMgrImpl(poa_);
            try {
                poa_.activate_object_with_id(manager_type.getBytes(Charset.forName(ENCODING)), catalogMgr);
            } catch (Exception e) {
                // Ignore
            }

            obj = poa_.create_reference_with_id(manager_type.getBytes(Charset.forName(ENCODING)), CatalogMgrHelper.id());
            break;

        case "OrderMgr":
            OrderMgrImpl orderMgr = new OrderMgrImpl();
            try {
                poa_.activate_object_with_id(manager_type.getBytes(Charset.forName(ENCODING)), orderMgr);
            } catch (Exception e) {
                // Ignore
            }

            obj = poa_.create_reference_with_id(manager_type.getBytes(Charset.forName(ENCODING)), OrderMgrHelper.id());
            break;

        case "ProductMgr":
            ProductMgrImpl productMgr = new ProductMgrImpl();
            try {
                poa_.activate_object_with_id(manager_type.getBytes(Charset.forName(ENCODING)), productMgr);
            } catch (Exception e) {
                // Ignore
            }

            obj = poa_.create_reference_with_id(manager_type.getBytes(Charset.forName(ENCODING)), ProductMgrHelper.id());
            break;

        case "DataModelMgr":
            DataModelMgrImpl dataModelMgr = new DataModelMgrImpl();
            try {
                poa_.activate_object_with_id(manager_type.getBytes(Charset.forName(ENCODING)), dataModelMgr);
            } catch (Exception e) {
                // Ignore
            }

            obj = poa_.create_reference_with_id(manager_type.getBytes(Charset.forName(ENCODING)), DataModelMgrHelper.id());
            break;

        default:
            String[] bad_params = {manager_type};
            throw new InvalidInputParameter("UnknownMangerType", new exception_details(
                    "UnknownMangerType",
                    true,
                    manager_type), bad_params);

        }

        LibraryManager libraryManager = LibraryManagerHelper.narrow(obj);
        return libraryManager;
    }

    @Override
    public LibraryDescription get_library_description()
            throws ProcessingFault, SystemFault {
        return libraryDescription;
    }

    @Override
    public LibraryDescription[] get_other_libraries(AccessCriteria access_criteria)
            throws ProcessingFault, InvalidInputParameter, SystemFault {
        throw new NO_IMPLEMENT();
    }

}
