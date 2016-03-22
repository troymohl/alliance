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
package org.codice.alliance.nsili.source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codice.alliance.nsili.common.GIAS.AttributeInformation;
import org.codice.alliance.nsili.common.GIAS.AttributeType;
import org.codice.alliance.nsili.common.NsiliAttributeMap;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import ddf.catalog.data.Metacard;

public class NsiliFilterFactory {

    public static final String TYPE = "type";

    public static final String LP = "(";

    public static final String RP = ")";

    public static final String LIKE = " like ";

    public static final String OR = " or ";

    public static final String AND = " and ";

    public static final String EXISTS = " exists";

    public static final String NOT = "not ";

    public static final String EQ = " = ";

    public static final String LT = " < ";

    public static final String GT = " > ";

    public static final String LTE = " <= ";

    public static final String GTE = " >= ";

    public static final String BTW = " <> ";

    public static final String COMMA = ",";

    public static final String INTERSECT = " intersect ";

    public static final String OUTSIDE = " outside ";

    public static final String INSIDE = " inside ";

    public static final String WITHIN = " within ";

    public static final String BEYOND = " beyond ";

    public static final String METERS_OF = " meters of ";

    private static final Logger LOGGER = LoggerFactory.getLogger(NsiliFilterFactory.class);

    private Map<String, List<AttributeInformation>> queryableAttributes;

    private String view;

    public NsiliFilterFactory(Map<String, List<AttributeInformation>> queryableAttributes,
            String view) {
        this.queryableAttributes = queryableAttributes;
        this.view = view;
    }

    public String buildPropertyIsLike(String value) {
        if (queryableAttributes == null) {
            return NsiliFilterDelegate.EMPTY_STRING;
        }

        List<AttributeInformation> attributeInformationList = queryableAttributes.get(view);

        if (attributeInformationList == null) {
            return NsiliFilterDelegate.EMPTY_STRING;
        }

        List<String> filters = new ArrayList<>();

        // Replace * with %, since * is not a valid wildcard in BQS
        value = value.replaceAll("\\*", "%");
        value = value.replaceAll("\\?", "%");

        for (AttributeInformation attributeInformation : attributeInformationList) {
            if (isTextAttributeType(attributeInformation)) {
                filters.add(LP + attributeInformation.attribute_name + LIKE + NsiliFilterDelegate.SQ
                        + value + NsiliFilterDelegate.SQ + RP);
            }
        }
        return buildOrFilter(filters);
    }

    public boolean isTextAttributeType(AttributeInformation attributeInformation) {
        return attributeInformation.attribute_type.equals(AttributeType.TEXT)
                && !attributeInformation.attribute_name.equals(TYPE);
    }

    public String buildPropertyIsEqualTo(String property, String value) {
        List<String> propertyList = mapToNsilQuery(property);
        List<String> filters = new ArrayList<>();
        for (String ddfProperty : propertyList) {
            String filter = LP + ddfProperty + EQ + value + RP;
            filters.add(filter);
        }
        return buildOrFilter(filters);
    }

    public String buildPropertyIsNotEqualTo(String property, String value) {
        return buildNotFilter(buildPropertyIsEqualTo(property, value));
    }

    public String buildPropertyIsGreaterThan(String property, String value) {
        List<String> propertyList = mapToNsilQuery(property);
        List<String> filters = new ArrayList<>();
        for (String ddfProperty : propertyList) {
            String filter = LP + ddfProperty + GT + value + RP;
            filters.add(filter);
        }
        return buildOrFilter(filters);
    }

    public String buildPropertyIsGreaterThanOrEqual(String property, String value) {
        List<String> propertyList = mapToNsilQuery(property);
        List<String> filters = new ArrayList<>();
        for (String ddfProperty : propertyList) {
            String filter = LP + ddfProperty + GTE + value + RP;
            filters.add(filter);
        }
        return buildOrFilter(filters);
    }

    public String buildPropertyIsLessThan(String property, String value) {
        List<String> propertyList = mapToNsilQuery(property);
        List<String> filters = new ArrayList<>();
        for (String ddfProperty : propertyList) {
            String filter = LP + ddfProperty + LT + value + RP;
            filters.add(filter);
        }
        return buildOrFilter(filters);
    }

    public String buildPropertyIsLessThanOrEqual(String property, String value) {
        List<String> propertyList = mapToNsilQuery(property);
        List<String> filters = new ArrayList<>();
        for (String ddfProperty : propertyList) {
            String filter = LP + ddfProperty + LTE + value + RP;
            filters.add(filter);
        }
        return buildOrFilter(filters);
    }

    public String buildPropertyIsBetween(String property, String lowerBound, String upperBound) {
        List<String> propertyList = mapToNsilQuery(property);
        List<String> filters = new ArrayList<>();
        for (String ddfProperty : propertyList) {
            String filter = LP + ddfProperty + BTW + lowerBound + COMMA + upperBound + RP;
            filters.add(filter);
        }
        return buildOrFilter(filters);
    }

    public String buildOrFilter(List<String> filters) {
        if (filters.size() == 1) {
            return filters.get(0);
        }

        StringBuilder stringBuilder = new StringBuilder(LP);

        for (String filter : filters) {
            if (StringUtils.isNotBlank(filter)) {
                stringBuilder.append(filter);
                stringBuilder.append(OR);
            }
        }

        String result = stringBuilder.toString();
        if (result.length() > 1) {
            return result.substring(0, result.length() - OR.length()) + RP;
        }
        return NsiliFilterDelegate.EMPTY_STRING;
    }

    public String buildAndFilter(List<String> filters) {
        if (filters.size() == 1) {
            return filters.get(0);
        }

        StringBuilder stringBuilder = new StringBuilder();

        for (String filter : filters) {
            if (StringUtils.isNotBlank(filter)) {
                stringBuilder.append(filter);
                stringBuilder.append(AND);
            }
        }
        String result = stringBuilder.toString();
        if (result.length() > 1) {
            return result.substring(0, result.length() - AND.length());
        }
        return NsiliFilterDelegate.EMPTY_STRING;
    }

    public String buildPropertyIsNull(String property) {
        return buildNotFilter(LP + property + EXISTS + RP);
    }

    public String buildNotFilter(String filter) {
        return NOT + filter;
    }

    public String buildIntersectsFilter(String propertyName, String wkt) {
        List<String> filters = new ArrayList<>();
        String bqsGeo = convertWktToBqs(wkt);
        String filter = NsiliFilterDelegate.EMPTY_STRING;
        if (StringUtils.isNotBlank(bqsGeo)) {
            List<String> propertyList = mapToNsilQuery(propertyName);
            for (String ddfProperty : propertyList) {
                filter = LP + ddfProperty + INTERSECT + convertWktToBqs(wkt) + RP;
                filters.add(filter);
            }
        }
        return buildOrFilter(filters);
    }

    public String buildDisjointFilter(String propertyName, String wkt) {
        List<String> filters = new ArrayList<>();
        String bqsGeo = convertWktToBqs(wkt);
        String filter = NsiliFilterDelegate.EMPTY_STRING;
        if (StringUtils.isNotBlank(bqsGeo)) {
            List<String> propertyList = mapToNsilQuery(propertyName);
            for (String ddfProperty : propertyList) {
                filter = LP + ddfProperty + OUTSIDE + convertWktToBqs(wkt) + RP;
                filters.add(filter);
            }
        }
        return buildOrFilter(filters);
    }

    public String buildWithinFilter(String propertyName, String wkt) {
        List<String> filters = new ArrayList<>();
        String bqsGeo = convertWktToBqs(wkt);
        String filter = NsiliFilterDelegate.EMPTY_STRING;
        if (StringUtils.isNotBlank(bqsGeo)) {
            List<String> propertyList = mapToNsilQuery(propertyName);
            for (String ddfProperty : propertyList) {
                filter = LP + ddfProperty + INSIDE + convertWktToBqs(wkt) + RP;
                filters.add(filter);
            }
        }
        return buildOrFilter(filters);
    }

    public String buildDWithinFilter(String propertyName, String wkt, double distance) {
        List<String> filters = new ArrayList<>();
        String bqsGeo = convertWktToBqs(wkt);
        String filter = NsiliFilterDelegate.EMPTY_STRING;
        if (StringUtils.isNotBlank(bqsGeo)) {
            List<String> propertyList = mapToNsilQuery(propertyName);
            for (String ddfProperty : propertyList) {
                filter = LP + ddfProperty + WITHIN + distance + METERS_OF + convertWktToBqs(wkt)
                        + RP;
                filters.add(filter);
            }
        }
        return buildOrFilter(filters);
    }

    public String buildBeyondFilter(String propertyName, String wkt, double distance) {
        List<String> filters = new ArrayList<>();
        String bqsGeo = convertWktToBqs(wkt);
        String filter = NsiliFilterDelegate.EMPTY_STRING;
        if (StringUtils.isNotBlank(bqsGeo)) {
            List<String> propertyList = mapToNsilQuery(propertyName);
            for (String ddfProperty : propertyList) {
                filter = LP + ddfProperty + BEYOND + distance + METERS_OF + convertWktToBqs(wkt)
                        + RP;
                filters.add(filter);
            }
        }
        return filter;
    }

    public static List<String> mapToNsil(String attribute) {
        List<String> nsiliProperties = NsiliAttributeMap.getNsiliAttributeForDdf(attribute);
        if (nsiliProperties == null) {
            nsiliProperties = new ArrayList<>();
            nsiliProperties.add(attribute);
            LOGGER.warn("Unable to map " + attribute + " to NSILI attribute, using attribute name");
        }
        return nsiliProperties;
    }

    public static List<String> mapToNsilQuery(String attribute) {
        if (attribute.equals(Metacard.ANY_GEO)) {
            return Arrays.asList(new String[] {NsiliConstants.SPATIAL_GEOGRAPHIC_REF_BOX});
        } else {
            return mapToNsil(attribute);
        }
    }

    public String convertWktToBqs(String wkt) {
        WKTReader wktReader = new WKTReader();
        Geometry geometry;
        try {
            geometry = wktReader.read(wkt);
        } catch (ParseException e) {
            LOGGER.warn("Unable to parse WKT String {}", wkt, e);
            return NsiliFilterDelegate.EMPTY_STRING;
        }

        if (geometry.getCoordinates() == null || StringUtils.isBlank(geometry.getGeometryType())) {
            return NsiliFilterDelegate.EMPTY_STRING;
        }

        StringBuilder result = new StringBuilder(geometry.getGeometryType()
                .toUpperCase() + LP);
        Coordinate[] coordinates = geometry.getCoordinates();
        for (Coordinate coordinate : coordinates) {
            result.append(coordinate.y + COMMA + coordinate.x + COMMA);
        }
        return result.toString()
                .substring(0,
                        result.toString()
                                .length() - 1) + RP;
    }
}
