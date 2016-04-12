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
package com.connexta.alliance.nsili.mockserver.data;

import com.connexta.alliance.nsili.common.GIAS.DeliveryManifest;
import com.connexta.alliance.nsili.common.GIAS.PackageElement;
import com.connexta.alliance.nsili.common.UID._ProductStub;

public class DeliveryManifestGenerator {

    public static DeliveryManifest getManifest() {
        String package_name = "myPackage";
        String[] files = {};
        PackageElement packageElement = new PackageElement(new _ProductStub(), files);
        PackageElement elements[] = {packageElement};
        DeliveryManifest deliveryManifest = new DeliveryManifest(package_name, elements);
        return deliveryManifest;
    }
}
