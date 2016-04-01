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
package org.codice.alliance.nsili.common;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.opengis.filter.Filter;

import com.sun.media.jfxmedia.logging.Logger;

import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;

public class TestBqsConverter {

    private static final String BQS_SIMPLE_ID = "(identifierUUID like 'Test')";

    private static final String BQS_OR_TEST =
            "identifierUUID like 'Test' or targetNumber like 'Test'";

    private static final String BQS_AND_TEST =
            "(identifierUUID like 'Test' or targetNumber like 'Test') and dateTimeModified >= '2016/03/14 06:58:31' and x > 15.3";

    private static final String BQS_MULTIPLE_AND_OR_TEST =
            "(identifierUUID like 'Test' or targetNumber like 'Test') and (dateTimeModified >= '2016/03/14 06:58:31') and (x > -15.3)";

    private static final String BQS_TEST_SHORT_DATE =
            "(identifierUUID like 'Test' or targetNumber like 'Test') and (dateTimeModified >= '2016/03/14')";

    private static final String BQS_POLYGON =
            "identifierMission like 'Test' and (spatialGeographicReferenceBox intersect POLYGON(46.155441760892586,81.76504326406543,48.16459468926409,161.75538233465647,2.8040686823204646,146.30988701631455,-0.4877657735999418,92.31643605259531,46.155441760892586,81.76504326406543))";

    private static final String BQS_RECTANGLE =
            "identifierMission like 'Test' and (spatialGeographicReferenceBox intersect RECTANGLE(46.155441760892586,81.76504326406543,-0.4877657735999418,92.31643605259531))";

    private static final String BQS_LINE =
            "identifierMission like 'Test' and (spatialGeographicReferenceBox intersect LINE(46.155441760892586,81.76504326406543,48.16459468926409,161.75538233465647,2.8040686823204646,146.30988701631455,-0.4877657735999418,92.31643605259531,46.155441760892586,81.76504326406543))";

    private static final String BQS_POINT =
            "identifierMission like 'Test' and (spatialGeographicReferenceBox intersect POINT(46.155441760892586,81.76504326406543))";

    private static final String BQS_CIRCLE =
            "identifierMission like 'Test' and (spatialGeographicReferenceBox intersect CIRCLE(46.155441760892586,81.76504326406543,25.6 meters))";

    private static final String BQS_ELLIPSE =
            "identifierMission like 'Test' and (spatialGeographicReferenceBox intersect ELLIPSE(46.155441760892586,81.76504326406543,122500.5 meters,65000.3 meters,33))";

    private static final String BQS_POINT_DMS =
            "identifierMission like 'Test' and (spatialGeographicReferenceBox intersect POINT(81:45:33.2N,146:25:01.8W))";

    private static final String BQS_RELATIVE_WITHIN =
            "identifierMission like 'Test' and (spatialGeographicReferenceBox within 6000 meters of POINT(46.155441760892586,81.76504326406543))";

    private static final String BQS_RELATIVE_BEYOND =
            "identifierMission like 'Test' and (spatialGeographicReferenceBox beyond 6 statute miles of CIRCLE(46.155441760892586,81.76504326406543,25.6 meters))";

    private BqsConverter bqsConverter = new BqsConverter(new GeotoolsFilterBuilder());

    @Before
    public void setup() {
        Logger.setLevel(Logger.DEBUG);
    }

    @Test
    public void testSimpleIdConversion() {
        Filter filter = bqsConverter.convertBQSToDDF(BQS_SIMPLE_ID);
        assertThat(filter, notNullValue());
        //TODO -- add query verification
    }
}
