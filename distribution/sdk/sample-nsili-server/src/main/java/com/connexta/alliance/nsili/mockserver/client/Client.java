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
package com.connexta.alliance.nsili.mockserver.client;

import com.connexta.alliance.nsili.common.GIAS.PackageElement;
import com.connexta.alliance.nsili.common.GIAS.Query;
import com.connexta.alliance.nsili.common.UCO.DAG;
import com.connexta.alliance.nsili.common.UID.Product;

public class Client {

    public static void main(String args[]) throws Exception {

        if (args.length != 1) {
            System.out.println("Unable to obtain IOR File :  Must specify URL to IOR file.");
        }

        String iorURL = args[0];

        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);

        NsiliClient nsiliClient = new NsiliClient(orb);

        // Get IOR File
        String iorFile = nsiliClient.getIorTextFile(iorURL);

        // Initialize Corba Library
        nsiliClient.initLibrary(iorFile);

        // Get the Managers from the Library
        String[] managers = nsiliClient.getManagerTypes();
        nsiliClient.initManagers(managers);

        // CatalogMgr
        Query query = new Query("NSIL_ALL_VIEW", "NSIL_CARD.identifier like '%'");
        int hitCount = nsiliClient.getHitCount(query);
        if (hitCount > 0) {
            DAG[] results = nsiliClient.submit_query(query);
            if (results != null) {
                nsiliClient.processAndPrintResults(results);
            }
            else {
                System.out.println("No results from query");
            }
        }

        // OrderMgr
        nsiliClient.validate_order(orb);
        PackageElement[] packageElements = nsiliClient.order(orb);

        // ProductMgr
        // For each packageElement in the order response, get the parameters and
        // related files for the product.
        if (packageElements != null) {
            for (PackageElement packageElement : packageElements) {
                Product product = packageElement.prod;
                nsiliClient.get_parameters(orb, product);
                nsiliClient.get_related_file_types(product);
                nsiliClient.get_related_files(orb, product);
            }
        }
        else {
            System.out.println("Order does not have any package elements");
        }

        orb.shutdown(true);
        System.out.println("Done. ");
        System.exit(0);
    }
}
