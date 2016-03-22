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

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.codice.alliance.nsili.common.GIAS.AccessCriteria;
import org.codice.alliance.nsili.common.GIAS.CatalogMgrHelper;
import org.codice.alliance.nsili.common.GIAS.DataModelMgrHelper;
import org.codice.alliance.nsili.common.GIAS.LibraryDescription;
import org.codice.alliance.nsili.common.GIAS.LibraryManager;
import org.codice.alliance.nsili.common.GIAS.LibraryManagerHelper;
import org.codice.alliance.nsili.common.GIAS.LibraryPOA;
import org.codice.alliance.nsili.common.GIAS.OrderMgrHelper;
import org.codice.alliance.nsili.common.GIAS.ProductMgrHelper;
import org.codice.alliance.nsili.common.NsiliManagerType;
import org.codice.alliance.nsili.common.UCO.InvalidInputParameter;
import org.codice.alliance.nsili.common.UCO.ProcessingFault;
import org.codice.alliance.nsili.common.UCO.SystemFault;
import org.codice.alliance.nsili.common.UCO.exception_details;
import org.codice.alliance.nsili.endpoint.managers.CatalogMgrImpl;
import org.codice.alliance.nsili.endpoint.managers.DataModelMgrImpl;
import org.codice.alliance.nsili.endpoint.managers.OrderMgrImpl;
import org.codice.alliance.nsili.endpoint.managers.ProductMgrImpl;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.slf4j.LoggerFactory;

import ddf.catalog.CatalogFramework;

public class LibraryImpl extends LibraryPOA {

    private static final String LIBRARY_VERSION = "NSILI|1.0";

        private List<String> managers = Arrays.asList(
//                "OrderMgr",
                NsiliManagerType.CATALOG_MGR.getSpecName()
//                "ProductMgr",
//                "DataModelMgr"
                /* Optional :
                "QueryOrderMgr",
                "StandingQueryMgr",
                "CreationMgr",
                "UpdateMgr" */);

    private static final String ENCODING = "UTF-8";

    private POA poa;

    private CatalogFramework catalogFramework;

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LibraryImpl.class);

    public LibraryImpl(POA poa) {
        this.poa = poa;
    }

    public void setCatalogFramework(CatalogFramework catalogFramework) {
        this.catalogFramework = catalogFramework;
    }

    @Override
    public String[] get_manager_types() throws ProcessingFault, SystemFault {
         return (String[]) managers.toArray();
    }

    @Override
    public LibraryManager get_manager(String manager_type, AccessCriteria access_criteria)
            throws ProcessingFault, InvalidInputParameter, SystemFault {

        org.omg.CORBA.Object obj;

        if (manager_type.equals(NsiliManagerType.CATALOG_MGR.getSpecName())) {
            CatalogMgrImpl catalogMgr = new CatalogMgrImpl(poa);
            catalogMgr.setCatalogFramework(catalogFramework);
            try {
                poa.activate_object_with_id(manager_type.getBytes(Charset.forName(ENCODING)),
                        catalogMgr);
            } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
                LOGGER.error("Error activating CatalogMgr", e);
            }

            obj = poa.create_reference_with_id(manager_type.getBytes(Charset.forName(ENCODING)),
                    CatalogMgrHelper.id());
        } else if (manager_type.equals(NsiliManagerType.ORDER_MGR.getSpecName())) {
            OrderMgrImpl orderMgr = new OrderMgrImpl();
            try {
                poa.activate_object_with_id(manager_type.getBytes(Charset.forName(ENCODING)),
                        orderMgr);
            } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
                LOGGER.error("Error activating OrderMgr", e);
            }

            obj = poa.create_reference_with_id(manager_type.getBytes(Charset.forName(ENCODING)),
                    OrderMgrHelper.id());
        } else if (manager_type.equals(NsiliManagerType.PRODUCT_MGR.getSpecName())) {
            ProductMgrImpl productMgr = new ProductMgrImpl();
            try {
                poa.activate_object_with_id(manager_type.getBytes(Charset.forName(ENCODING)),
                        productMgr);
            } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
                LOGGER.error("Error activating ProductMgr", e);
            }

            obj = poa.create_reference_with_id(manager_type.getBytes(Charset.forName(ENCODING)),
                    ProductMgrHelper.id());
        } else if (manager_type.equals(NsiliManagerType.DATA_MODEL_MGR.getSpecName())) {
            DataModelMgrImpl dataModelMgr = new DataModelMgrImpl();
            try {
                poa.activate_object_with_id(manager_type.getBytes(Charset.forName(ENCODING)),
                        dataModelMgr);
            } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
                LOGGER.error("Error activating DataModelMgr", e);
            }

            obj = poa.create_reference_with_id(manager_type.getBytes(Charset.forName(ENCODING)),
                    DataModelMgrHelper.id());
        } else {
            String[] bad_params = {manager_type};
            throw new InvalidInputParameter("UnknownMangerType",
                    new exception_details("UnknownMangerType", true, manager_type),
                    bad_params);

        }

        LibraryManager libraryManager = LibraryManagerHelper.narrow(obj);
        return libraryManager;
    }

    @Override
    public LibraryDescription get_library_description() throws ProcessingFault, SystemFault {
        String host = System.getProperty("org.codice.ddf.system.hostname");
        String country = System.getProperty("user.country");
        String organization = System.getProperty("org.codice.ddf.system.organization");
        String libraryDescr = country + "|" + organization;

        return new LibraryDescription(host, libraryDescr, LIBRARY_VERSION);
    }

    @Override
    public LibraryDescription[] get_other_libraries(AccessCriteria access_criteria)
            throws ProcessingFault, InvalidInputParameter, SystemFault {
        throw new NO_IMPLEMENT();
    }

}
