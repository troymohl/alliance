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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TimeZone;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.math.NumberUtils;
import org.codice.alliance.nsili.common.GIAS.Query;
import org.codice.alliance.nsili.common.grammer.BqsLexer;
import org.codice.alliance.nsili.common.grammer.BqsListener;
import org.codice.alliance.nsili.common.grammer.BqsParser;
import org.codice.ddf.libs.geo.coordinate.Latitude;
import org.codice.ddf.libs.geo.coordinate.Longitude;
import org.opengis.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.measure.Distance;

public class BqsConverter {
    private static final String BASIC_BQS_1 = "(identifierUUID like 'Test')";

    private static final String BASIC_BQS_2 =
            "identifierUUID like 'Test' or targetNumber like 'Test'";

    private static final String BASIC_BQS_3 =
            "(identifierUUID like 'Test' or targetNumber like 'Test') and dateTimeModified >= '2016/03/14 06:58:31' and x > 15.3";

    private static final String BASIC_BQS_3a =
            "(identifierUUID like 'Test' or targetNumber like 'Test') and (dateTimeModified >= '2016/03/14 06:58:31') and (x > -15.3)";

    private static final String BASIC_BQS_4 =
            "(identifierUUID like 'Test' or targetNumber like 'Test') and (dateTimeModified >= '2016/03/14')";

    private static final String BASIC_BQS_5 =
            "identifierMission like 'Test' and (spatialGeographicReferenceBox intersect POLYGON(46.155441760892586,81.76504326406543,48.16459468926409,161.75538233465647,2.8040686823204646,146.30988701631455,-0.4877657735999418,92.31643605259531,46.155441760892586,81.76504326406543))";

    private static final String BASIC_BQS_5a =
            "identifierMission like 'Test' and (spatialGeographicReferenceBox intersect RECTANGLE(46.155441760892586,81.76504326406543,-0.4877657735999418,92.31643605259531))";

    private static final String BASIC_BQS_5b =
            "identifierMission like 'Test' and (spatialGeographicReferenceBox intersect LINE(46.155441760892586,81.76504326406543,48.16459468926409,161.75538233465647,2.8040686823204646,146.30988701631455,-0.4877657735999418,92.31643605259531,46.155441760892586,81.76504326406543))";

    private static final String BASIC_BQS_5c =
            "identifierMission like 'Test' and (spatialGeographicReferenceBox intersect POINT(46.155441760892586,81.76504326406543))";

    private static final String BASIC_BQS_5d =
            "identifierMission like 'Test' and (spatialGeographicReferenceBox intersect CIRCLE(46.155441760892586,81.76504326406543,25.6 meters))";

    private static final String BASIC_BQS_5e =
            "identifierMission like 'Test' and (spatialGeographicReferenceBox intersect ELLIPSE(46.155441760892586,81.76504326406543,122500.5 meters,65000.3 meters,33))";

    private static final String BASIC_BQS_5f =
            "identifierMission like 'Test' and (spatialGeographicReferenceBox intersect POINT(81:45:33.2N,146:25:01.8W))";

    private static final String BASIC_BQS_5g =
            "identifierMission like 'Test' and (spatialGeographicReferenceBox within 6000 meters of POINT(46.155441760892586,81.76504326406543))";

    private static final String BASIC_BQS_5h =
            "identifierMission like 'Test' and (spatialGeographicReferenceBox beyond 6 statute miles of CIRCLE(46.155441760892586,81.76504326406543,25.6 meters))";

    private static final String BQS_TEST_STRING =
            "((identifierUUID like 'Test') or (targetNumber like 'Test') or (source like 'Test') or "
                    + "(identifierMission like 'Test') or (category like 'Test') or "
                    + "(decompressionTechnique like 'Test') or (identifier like 'Test') or "
                    + "(recipient like 'Test') or (messageBody like 'Test') or "
                    + "(messageType like 'Test') or (classification like 'Test') or (policy like 'Test') or "
                    + "(releasability like 'Test') or (partIdentifier like 'Test') or (creator like 'Test') or "
                    + "(encodingScheme like 'Test')) and (dateTimeModified >= '2016/03/14 06:58:31')";

    private static final Logger LOGGER = LoggerFactory.getLogger(BqsConverter.class);

    public BqsConverter() {

    }

    public Filter convertBQSToDDF(Query query) {
        String bqsQuery = query.bqs_query;
        return convertBQSToDDF(bqsQuery);
    }

    public Filter convertBQSToDDF(String query) {
        System.out.println("ORIG Query: " + query);

        ANTLRInputStream inputStream = new ANTLRInputStream(query);
        BqsLexer lex = new BqsLexer(inputStream); // transforms characters into tokens
        CommonTokenStream tokens = new CommonTokenStream(lex); // a token stream
        BqsParser parser = new BqsParser(tokens); // transforms tokens into parse trees
        FilterBuilder filterBuilder = new GeotoolsFilterBuilder();
        BqsTreeWalkerListener bqsListener = new BqsTreeWalkerListener(filterBuilder);

        ParseTree tree = parser.query();
        ParseTreeWalker.DEFAULT.walk(bqsListener, tree);

        Filter filter = bqsListener.getFilter();
        if (filter != null) {
            System.out.println("PARSED: " + filter.toString());
        } else {
            System.out.println("NO FILTER PARSED");
        }

        return filter;
    }

    public static void main(String[] args) {
        BqsConverter converter = new BqsConverter();
        converter.convertBQSToDDF(BASIC_BQS_5h);
    }

    class BqsTreeWalkerListener implements BqsListener {
        private static final String BQS_FULL_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

        private static final String BQS_SHORT_DATE_FORMAT = "yyyy/MM/dd";

        private Filter currFilter = null;

        private FilterBuilder filterBuilder;

        private String attribute = "";

        private Stack<String> nestedOperatorStack = new Stack<>();

        private Map<String, List<Filter>> filterBy = new HashMap<>();

        private Stack<BqsOperator> bqsOperatorStack = new Stack<>();

        private HashFunction hashFunction;

        private String dateStr = "";

        private String quotedStr = "";

        private String numberStr = "";

        private String builtWkt = "";

        private String upperLeftLatLon[] = new String[2];

        private BqsShape buildingShape = null;

        private double latDecimalDeg = 0.0;

        private double lonDecimalDeg = 0.0;

        private double radiusInMeters;

        private Distance.LinearUnit distanceUnit;

        private double ellipseMajorLenMeters;

        private double ellipseMinorLenMeters;

        private int northAngle;

        private Latitude dmsBasedLat;

        private Longitude dmsBasedLon;

        double relativeDistInMeters;

        public BqsTreeWalkerListener(FilterBuilder filterBuilder) {
            this.filterBuilder = filterBuilder;
            hashFunction = Hashing.sha512();
        }

        public Filter getFilter() {
            return currFilter;
        }

        @Override
        public void enterQuery(BqsParser.QueryContext ctx) {
            if (!ctx.OR()
                    .isEmpty()) {
                bqsOperatorStack.push(BqsOperator.OR);
                print("OR: ");

                String currOperHash = hashFunction.hashBytes(ctx.getText()
                        .getBytes())
                        .toString();
                nestedOperatorStack.push(currOperHash);
                filterBy.put(currOperHash, new ArrayList<>());
            }
        }

        @Override
        public void exitQuery(BqsParser.QueryContext ctx) {
            if (!ctx.OR()
                    .isEmpty()) {
                bqsOperatorStack.pop();

                String operHash = nestedOperatorStack.pop();
                List<Filter> filters = filterBy.get(operHash);

                if (currFilter == null) {
                    currFilter = filterBuilder.anyOf(filters);
                } else {
                    filters.add(currFilter);
                    currFilter = filterBuilder.anyOf(filters);
                }
            }
        }

        @Override
        public void enterTerm(BqsParser.TermContext ctx) {
            if (!ctx.AND()
                    .isEmpty()) {
                bqsOperatorStack.push(BqsOperator.AND);
                print("AND");

                String currOperHash = hashFunction.hashBytes(ctx.getText()
                        .getBytes())
                        .toString();
                nestedOperatorStack.push(currOperHash);
                filterBy.put(currOperHash, new ArrayList<>());
            }
        }

        @Override
        public void exitTerm(BqsParser.TermContext ctx) {
            if (!ctx.AND()
                    .isEmpty()) {
                bqsOperatorStack.pop();

                String operHash = nestedOperatorStack.pop();
                List<Filter> filters = filterBy.get(operHash);
                if (currFilter == null) {
                    currFilter = filterBuilder.allOf(filters);
                } else {
                    filters.add(currFilter);
                    currFilter = filterBuilder.allOf(filters);
                }
            }
        }

        @Override
        public void enterFactor(BqsParser.FactorContext ctx) {

        }

        @Override
        public void exitFactor(BqsParser.FactorContext ctx) {

        }

        @Override
        public void enterPrimary(BqsParser.PrimaryContext ctx) {
            if (ctx.LIKE() != null) {
                print("LIKE");
                bqsOperatorStack.push(BqsOperator.LIKE);
            } else if (ctx.EXISTS() != null) {
                print("EXISTS");
                bqsOperatorStack.push(BqsOperator.EXISTS);
            } else if (ctx.OF() != null) {
                print("OF");
                bqsOperatorStack.push(BqsOperator.OF);
            }
        }

        @Override
        public void exitPrimary(BqsParser.PrimaryContext ctx) {
            if (ctx.LIKE() != null) {
                bqsOperatorStack.pop();
            } else if (ctx.EXISTS() != null) {
                bqsOperatorStack.pop();
            } else if (ctx.OF() != null) {
                bqsOperatorStack.pop();
            }
        }

        @Override
        public void enterAttribute_name(BqsParser.Attribute_nameContext ctx) {
            print(ctx.getText());
        }

        @Override
        public void exitAttribute_name(BqsParser.Attribute_nameContext ctx) {
            attribute = ctx.getText();
        }

        @Override
        public void enterSimple_attribute_name(BqsParser.Simple_attribute_nameContext ctx) {
            print(ctx.getText());
        }

        @Override
        public void exitSimple_attribute_name(BqsParser.Simple_attribute_nameContext ctx) {
            attribute = ctx.getText();
        }

        @Override
        public void enterComp_op(BqsParser.Comp_opContext ctx) {
            print(ctx.getText());
            if (ctx.EQUAL() != null) {
                bqsOperatorStack.push(BqsOperator.EQUAL);
            } else if (ctx.NOT() != null) {
                bqsOperatorStack.push(BqsOperator.NOT);
            } else if (ctx.GT() != null) {
                bqsOperatorStack.push(BqsOperator.GT);
            } else if (ctx.GTE() != null) {
                bqsOperatorStack.push(BqsOperator.GTE);
            } else if (ctx.LT() != null) {
                bqsOperatorStack.push(BqsOperator.LT);
            } else if (ctx.LTE() != null) {
                bqsOperatorStack.push(BqsOperator.LTE);
            }
        }

        @Override
        public void exitComp_op(BqsParser.Comp_opContext ctx) {

        }

        @Override
        public void enterConstant_expression(BqsParser.Constant_expressionContext ctx) {

        }

        @Override
        public void exitConstant_expression(BqsParser.Constant_expressionContext ctx) {
            BqsOperator bqsOperator = bqsOperatorStack.pop();

            if (!dateStr.isEmpty()) {
                Filter filter = null;
                try {

                    Date date;
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat(BQS_FULL_DATE_FORMAT);
                        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                        date = dateFormat.parse(dateStr);
                    } catch (ParseException pe) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat(BQS_SHORT_DATE_FORMAT);
                        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                        date = dateFormat.parse(dateStr);
                    }

                    if (date != null) {
                        if (bqsOperator == BqsOperator.GTE) {
                            filter = filterBuilder.anyOf(filterBuilder.attribute(attribute)
                                            .after()
                                            .date(date),
                                    filterBuilder.attribute(attribute)
                                            .equalTo()
                                            .date(date));
                        } else if (bqsOperator == BqsOperator.GT) {
                            filter = filterBuilder.attribute(attribute)
                                    .after()
                                    .date(date);
                        } else if (bqsOperator == BqsOperator.LT) {
                            filter = filterBuilder.attribute(attribute)
                                    .before()
                                    .date(date);
                        } else if (bqsOperator == BqsOperator.LTE) {
                            filter = filterBuilder.anyOf(filterBuilder.attribute(attribute)
                                            .before()
                                            .date(date),
                                    filterBuilder.attribute(attribute)
                                            .equalTo()
                                            .date(date));
                        } else if (bqsOperator == BqsOperator.NOT) {
                            filter = filterBuilder.attribute(attribute)
                                    .notEqualTo()
                                    .date(date);
                        } else if (bqsOperator == BqsOperator.EQUAL) {
                            filter = filterBuilder.attribute(attribute)
                                    .equalTo()
                                    .date(date);
                        }

                        if (filter != null) {
                            List<Filter> filters = filterBy.get(nestedOperatorStack.peek());
                            filters.add(filter);
                        }
                    }
                } catch (ParseException e) {
                    LOGGER.warn("Unable to parse date from: " + dateStr);
                }
            } else if (!numberStr.isEmpty()) {
                Filter filter = null;
                try {
                    Number number = getNumber(numberStr);
                    if (number != null) {
                        if (number instanceof Short) {
                            if (bqsOperator == BqsOperator.GTE) {
                                filter = filterBuilder.anyOf(filterBuilder.attribute(attribute)
                                        .greaterThanOrEqualTo()
                                        .number((short) number));
                            } else if (bqsOperator == BqsOperator.GT) {
                                filter = filterBuilder.attribute(attribute)
                                        .greaterThan()
                                        .number((short) number);
                            } else if (bqsOperator == BqsOperator.LT) {
                                filter = filterBuilder.attribute(attribute)
                                        .lessThan()
                                        .number((short) number);
                            } else if (bqsOperator == BqsOperator.LTE) {
                                filter = filterBuilder.anyOf(filterBuilder.attribute(attribute)
                                        .lessThanOrEqualTo()
                                        .number((short) number));
                            } else if (bqsOperator == BqsOperator.NOT) {
                                filter = filterBuilder.attribute(attribute)
                                        .notEqualTo()
                                        .number((short) number);
                            } else if (bqsOperator == BqsOperator.EQUAL) {
                                filter = filterBuilder.attribute(attribute)
                                        .equalTo()
                                        .number((short) number);
                            }
                        } else if (number instanceof Long) {
                            if (bqsOperator == BqsOperator.GTE) {
                                filter = filterBuilder.anyOf(filterBuilder.attribute(attribute)
                                        .greaterThanOrEqualTo()
                                        .number((long) number));
                            } else if (bqsOperator == BqsOperator.GT) {
                                filter = filterBuilder.attribute(attribute)
                                        .greaterThan()
                                        .number((long) number);
                            } else if (bqsOperator == BqsOperator.LT) {
                                filter = filterBuilder.attribute(attribute)
                                        .lessThan()
                                        .number((long) number);
                            } else if (bqsOperator == BqsOperator.LTE) {
                                filter = filterBuilder.anyOf(filterBuilder.attribute(attribute)
                                        .lessThanOrEqualTo()
                                        .number((long) number));
                            } else if (bqsOperator == BqsOperator.NOT) {
                                filter = filterBuilder.attribute(attribute)
                                        .notEqualTo()
                                        .number((long) number);
                            } else if (bqsOperator == BqsOperator.EQUAL) {
                                filter = filterBuilder.attribute(attribute)
                                        .equalTo()
                                        .number((long) number);
                            }
                        } else if (number instanceof Integer) {
                            if (bqsOperator == BqsOperator.GTE) {
                                filter = filterBuilder.anyOf(filterBuilder.attribute(attribute)
                                        .greaterThanOrEqualTo()
                                        .number((int) number));
                            } else if (bqsOperator == BqsOperator.GT) {
                                filter = filterBuilder.attribute(attribute)
                                        .greaterThan()
                                        .number((int) number);
                            } else if (bqsOperator == BqsOperator.LT) {
                                filter = filterBuilder.attribute(attribute)
                                        .lessThan()
                                        .number((int) number);
                            } else if (bqsOperator == BqsOperator.LTE) {
                                filter = filterBuilder.anyOf(filterBuilder.attribute(attribute)
                                        .lessThanOrEqualTo()
                                        .number((int) number));
                            } else if (bqsOperator == BqsOperator.NOT) {
                                filter = filterBuilder.attribute(attribute)
                                        .notEqualTo()
                                        .number((int) number);
                            } else if (bqsOperator == BqsOperator.EQUAL) {
                                filter = filterBuilder.attribute(attribute)
                                        .equalTo()
                                        .number((int) number);
                            }
                        } else if (number instanceof Float) {
                            if (bqsOperator == BqsOperator.GTE) {
                                filter = filterBuilder.anyOf(filterBuilder.attribute(attribute)
                                        .greaterThanOrEqualTo()
                                        .number((float) number));
                            } else if (bqsOperator == BqsOperator.GT) {
                                filter = filterBuilder.attribute(attribute)
                                        .greaterThan()
                                        .number((float) number);
                            } else if (bqsOperator == BqsOperator.LT) {
                                filter = filterBuilder.attribute(attribute)
                                        .lessThan()
                                        .number((float) number);
                            } else if (bqsOperator == BqsOperator.LTE) {
                                filter = filterBuilder.anyOf(filterBuilder.attribute(attribute)
                                        .lessThanOrEqualTo()
                                        .number((float) number));
                            } else if (bqsOperator == BqsOperator.NOT) {
                                filter = filterBuilder.attribute(attribute)
                                        .notEqualTo()
                                        .number((float) number);
                            } else if (bqsOperator == BqsOperator.EQUAL) {
                                filter = filterBuilder.attribute(attribute)
                                        .equalTo()
                                        .number((float) number);
                            }
                        } else if (number instanceof Double) {
                            if (bqsOperator == BqsOperator.GTE) {
                                filter = filterBuilder.anyOf(filterBuilder.attribute(attribute)
                                        .greaterThanOrEqualTo()
                                        .number((double) number));
                            } else if (bqsOperator == BqsOperator.GT) {
                                filter = filterBuilder.attribute(attribute)
                                        .greaterThan()
                                        .number((double) number);
                            } else if (bqsOperator == BqsOperator.LT) {
                                filter = filterBuilder.attribute(attribute)
                                        .lessThan()
                                        .number((double) number);
                            } else if (bqsOperator == BqsOperator.LTE) {
                                filter = filterBuilder.anyOf(filterBuilder.attribute(attribute)
                                        .lessThanOrEqualTo()
                                        .number((double) number));
                            } else if (bqsOperator == BqsOperator.NOT) {
                                filter = filterBuilder.attribute(attribute)
                                        .notEqualTo()
                                        .number((double) number);
                            } else if (bqsOperator == BqsOperator.EQUAL) {
                                filter = filterBuilder.attribute(attribute)
                                        .equalTo()
                                        .number((double) number);
                            }
                        } else {
                            LOGGER.warn("Number type not handled by filter builder: "
                                    + number.getClass());
                        }
                    }

                    if (filter != null) {
                        List<Filter> filters = filterBy.get(nestedOperatorStack.peek());
                        filters.add(filter);
                    }
                } catch (NumberFormatException e) {
                    LOGGER.info("Unable to convert to a number: " + numberStr);
                }

            } else if (!quotedStr.isEmpty()) {
                Filter filter = null;
                if (bqsOperator == BqsOperator.NOT) {
                    filter = filterBuilder.attribute(attribute)
                            .notEqualTo()
                            .text(quotedStr);
                } else if (bqsOperator == BqsOperator.EQUAL) {
                    filter = filterBuilder.attribute(attribute)
                            .equalTo()
                            .text(quotedStr);
                }

                if (filter != null) {
                    List<Filter> filters = filterBy.get(nestedOperatorStack.peek());
                    filters.add(filter);
                }
            }

            //Reset local variables
            attribute = "";
            dateStr = "";
            numberStr = "";
            quotedStr = "";
        }

        @Override
        public void enterDate(BqsParser.DateContext ctx) {
            print(ctx.getText());
            dateStr = "";
        }

        @Override
        public void exitDate(BqsParser.DateContext ctx) {
            //No-Op
        }

        @Override
        public void enterYear(BqsParser.YearContext ctx) {
            dateStr = dateStr + ctx.getText();
        }

        @Override
        public void exitYear(BqsParser.YearContext ctx) {
            //No-Op
        }

        @Override
        public void enterMonth(BqsParser.MonthContext ctx) {
            dateStr = dateStr + "/" + ctx.getText();
        }

        @Override
        public void exitMonth(BqsParser.MonthContext ctx) {
            //No-Op
        }

        @Override
        public void enterDay(BqsParser.DayContext ctx) {
            dateStr = dateStr + "/" + ctx.getText();
        }

        @Override
        public void exitDay(BqsParser.DayContext ctx) {
            //No-Op
        }

        @Override
        public void enterHour(BqsParser.HourContext ctx) {
            dateStr = dateStr + " " + ctx.getText();
        }

        @Override
        public void exitHour(BqsParser.HourContext ctx) {
            //No-Op
        }

        @Override
        public void enterMinute(BqsParser.MinuteContext ctx) {
            dateStr = dateStr + ":" + ctx.getText();
        }

        @Override
        public void exitMinute(BqsParser.MinuteContext ctx) {
            //No-Op
        }

        @Override
        public void enterSecond(BqsParser.SecondContext ctx) {
            dateStr = dateStr + ":" + ctx.getText();
        }

        @Override
        public void exitSecond(BqsParser.SecondContext ctx) {
            //No-Op
        }

        @Override
        public void enterGeo_op(BqsParser.Geo_opContext ctx) {
            if (ctx.INTERSECT() != null) {
                bqsOperatorStack.push(BqsOperator.INTERSECT);
            } else if (ctx.INSIDE() != null) {
                bqsOperatorStack.push(BqsOperator.INSIDE);
            } else if (ctx.OUTSIDE() != null) {
                bqsOperatorStack.push(BqsOperator.OUTSIDE);
            }
        }

        @Override
        public void exitGeo_op(BqsParser.Geo_opContext ctx) {
            //No-Op
        }

        @Override
        public void enterRel_geo_op(BqsParser.Rel_geo_opContext ctx) {
            if (ctx.BEYOND() != null) {
                bqsOperatorStack.push(BqsOperator.BEYOND);
            } else if (ctx.WITHIN() != null) {
                bqsOperatorStack.push(BqsOperator.WITHIN);
            }
        }

        @Override
        public void exitRel_geo_op(BqsParser.Rel_geo_opContext ctx) {
            //No-Op
        }

        @Override
        public void enterDist_units(BqsParser.Dist_unitsContext ctx) {
            print(ctx.getText());

            if (ctx.FEET() != null || ctx.FEET_UPPER() != null) {
                distanceUnit = Distance.LinearUnit.FOOT_U_S;
            } else if (ctx.METERS() != null || ctx.METERS_UPPER() != null) {
                distanceUnit = Distance.LinearUnit.METER;
            } else if (ctx.KILOMETER() != null) {
                distanceUnit = Distance.LinearUnit.KILOMETER;
            } else if (ctx.NAUTICAL_MI() != null) {
                distanceUnit = Distance.LinearUnit.NAUTICAL_MILE;
            } else if (ctx.STATUTE_MI() != null) {
                distanceUnit = Distance.LinearUnit.MILE;
            }

            //We need to parse this if we are doing relative geo queries
            if (bqsOperatorStack.peek() == BqsOperator.WITHIN ||
                    bqsOperatorStack.peek() == BqsOperator.BEYOND) {
                double parsedDist = Double.parseDouble(numberStr);
                Distance distance = new Distance(parsedDist, distanceUnit);
                relativeDistInMeters = distance.getAs(Distance.LinearUnit.METER);
            }
        }

        @Override
        public void exitDist_units(BqsParser.Dist_unitsContext ctx) {
            //No-Op
        }

        @Override
        public void enterGeo_element(BqsParser.Geo_elementContext ctx) {
            print(ctx.getText());
        }

        @Override
        public void exitGeo_element(BqsParser.Geo_elementContext ctx) {
            BqsOperator operator = bqsOperatorStack.pop();

            Filter filter = null;

            if (!builtWkt.isEmpty()) {
                //Geo Operators
                if (operator == BqsOperator.INSIDE ||
                        operator == BqsOperator.INTERSECT ||
                        operator == BqsOperator.OUTSIDE) {
                    if (buildingShape == BqsShape.CIRCLE) {
                        if (operator == BqsOperator.INSIDE) {
                            filter = filterBuilder.attribute(attribute)
                                    .withinBuffer()
                                    .wkt(builtWkt, radiusInMeters);
                        } else if (operator == BqsOperator.INTERSECT) {
                            filter = filterBuilder.attribute(attribute)
                                    .withinBuffer()
                                    .wkt(builtWkt, radiusInMeters);
                        } else if (operator == BqsOperator.OUTSIDE) {
                            filter = filterBuilder.attribute(attribute)
                                    .beyond()
                                    .wkt(builtWkt, radiusInMeters);
                        }
                    } else {
                        if (operator == BqsOperator.INSIDE) {
                            filter = filterBuilder.attribute(attribute)
                                    .within()
                                    .wkt(builtWkt);
                        } else if (operator == BqsOperator.INTERSECT) {
                            filter = filterBuilder.attribute(attribute)
                                    .intersecting()
                                    .wkt(builtWkt);
                        } else if (operator == BqsOperator.OUTSIDE) {
                            filter = filterBuilder.attribute(attribute)
                                    .beyond()
                                    .wkt(builtWkt);
                        }
                    }
                } else if (operator == BqsOperator.WITHIN || operator == BqsOperator.BEYOND) {
                    //Relative Geo Operators
                    if (operator == BqsOperator.WITHIN) {
                        filter = filterBuilder.attribute(attribute)
                                .withinBuffer()
                                .wkt(builtWkt, relativeDistInMeters);
                    } else if (operator == BqsOperator.BEYOND) {
                        filter = filterBuilder.attribute(attribute)
                                .beyond()
                                .wkt(builtWkt, relativeDistInMeters);
                    }
                }

                if (filter != null) {
                    List<Filter> filters = filterBy.get(nestedOperatorStack.peek());
                    filters.add(filter);
                }
            }

            builtWkt = "";
            upperLeftLatLon = new String[2];
            buildingShape = null;
            radiusInMeters = 0.0;
            latDecimalDeg = 0.0;
            lonDecimalDeg = 0.0;
        }

        @Override
        public void enterSign(BqsParser.SignContext ctx) {
            //No-Op
        }

        @Override
        public void exitSign(BqsParser.SignContext ctx) {
            //No-Op
        }

        @Override
        public void enterNumber(BqsParser.NumberContext ctx) {
            numberStr = numberStr + ctx.getText();
            print(ctx.getText());
        }

        @Override
        public void exitNumber(BqsParser.NumberContext ctx) {
            //No-Op
        }

        @Override
        public void enterDigit_seq(BqsParser.Digit_seqContext ctx) {
            //No-Op
        }

        @Override
        public void exitDigit_seq(BqsParser.Digit_seqContext ctx) {
            //No-Op
        }

        @Override
        public void enterQuoted_string(BqsParser.Quoted_stringContext ctx) {
            print(ctx.getText());
        }

        @Override
        public void exitQuoted_string(BqsParser.Quoted_stringContext ctx) {
            BqsOperator bqsOperator = bqsOperatorStack.peek();

            if (bqsOperator == BqsOperator.LIKE) {
                Filter filter = filterBuilder.attribute(attribute)
                        .like()
                        .text(ctx.getText());

                if (!nestedOperatorStack.isEmpty()) {
                    List<Filter> filters = filterBy.get(nestedOperatorStack.peek());
                    filters.add(filter);
                } else {
                    currFilter = filter;
                }

                print("FILTER: " + filter);
            }

            attribute = "";
        }

        @Override
        public void enterLatitude(BqsParser.LatitudeContext ctx) {
            //No-Op
        }

        @Override
        public void exitLatitude(BqsParser.LatitudeContext ctx) {
            latDecimalDeg = Double.parseDouble(ctx.getText());
        }

        @Override
        public void enterLongitude(BqsParser.LongitudeContext ctx) {
            //No-Op
        }

        @Override
        public void exitLongitude(BqsParser.LongitudeContext ctx) {
            lonDecimalDeg = Double.parseDouble(ctx.getText());
        }

        @Override
        public void enterDms(BqsParser.DmsContext ctx) {
            //No-Op
        }

        @Override
        public void exitDms(BqsParser.DmsContext ctx) {
            //No-Op
        }

        @Override
        public void enterLatitude_DMS(BqsParser.Latitude_DMSContext ctx) {
            System.out.println("LAT DMS: " + ctx.getText());
            //TODO Call Latitude parsing from DDF once implemented
        }

        @Override
        public void exitLatitude_DMS(BqsParser.Latitude_DMSContext ctx) {
            //No-Op
        }

        @Override
        public void enterLongitude_DMS(BqsParser.Longitude_DMSContext ctx) {
            System.out.println("LON DMS: " + ctx.getText());
            //TODO Call Longitude parsing from DDF once implemented
        }

        @Override
        public void exitLongitude_DMS(BqsParser.Longitude_DMSContext ctx) {
            //No-Op
        }

        @Override
        public void enterLatlon(BqsParser.LatlonContext ctx) {
            //No-Op
        }

        @Override
        public void exitLatlon(BqsParser.LatlonContext ctx) {
            //No-Op
        }

        @Override
        public void enterCoordinate(BqsParser.CoordinateContext ctx) {
            //No-Op
        }

        @Override
        public void exitCoordinate(BqsParser.CoordinateContext ctx) {
            //Not all shapes are parsed this way, some we get the coordinate from higher level method
            if (buildingShape == BqsShape.POLYGON ||
                    buildingShape == BqsShape.LINE ||
                    buildingShape == BqsShape.POINT ||
                    buildingShape == BqsShape.CIRCLE) {
                if (!builtWkt.isEmpty() && builtWkt.charAt(builtWkt.length() - 1) != '(') {
                    builtWkt = builtWkt + "," + lonDecimalDeg + " " + latDecimalDeg;
                } else {
                    builtWkt = builtWkt + lonDecimalDeg + " " + latDecimalDeg;
                }
            } else if (buildingShape == BqsShape.ELLIPSE) {
                String[] latLonCoord = ctx.getText()
                        .split(",");
                latDecimalDeg = Double.parseDouble(latLonCoord[0]);
                lonDecimalDeg = Double.parseDouble(latLonCoord[1]);
            }
        }

        @Override
        public void enterPoint(BqsParser.PointContext ctx) {
            builtWkt = "POINT(";
            buildingShape = BqsShape.POINT;
        }

        @Override
        public void exitPoint(BqsParser.PointContext ctx) {
            builtWkt = builtWkt + ")";
        }

        @Override
        public void enterPolygon(BqsParser.PolygonContext ctx) {
            builtWkt = "POLYGON((";
            buildingShape = BqsShape.POLYGON;
        }

        @Override
        public void exitPolygon(BqsParser.PolygonContext ctx) {
            builtWkt = builtWkt + "))";
        }

        @Override
        public void enterRectangle(BqsParser.RectangleContext ctx) {
            builtWkt = "POLYGON((";
            buildingShape = BqsShape.RECTANGLE;
        }

        @Override
        public void exitRectangle(BqsParser.RectangleContext ctx) {
            builtWkt = builtWkt + "))";
        }

        @Override
        public void enterUpper_left(BqsParser.Upper_leftContext ctx) {
            //No-Op
        }

        @Override
        public void exitUpper_left(BqsParser.Upper_leftContext ctx) {
            upperLeftLatLon = ctx.getText()
                    .split(",");
        }

        @Override
        public void enterLower_right(BqsParser.Lower_rightContext ctx) {
            //No-Op
        }

        @Override
        public void exitLower_right(BqsParser.Lower_rightContext ctx) {
            String[] lowerLeftLatLon = ctx.getText()
                    .split(",");

            builtWkt = builtWkt + upperLeftLatLon[1] + " " + upperLeftLatLon[0] +
                    "," + lowerLeftLatLon[1] + " " + upperLeftLatLon[0] +
                    "," + lowerLeftLatLon[1] + " " + lowerLeftLatLon[0] +
                    "," + upperLeftLatLon[1] + " " + lowerLeftLatLon[0] +
                    "," + upperLeftLatLon[1] + " " + upperLeftLatLon[0];
        }

        @Override
        public void enterCircle(BqsParser.CircleContext ctx) {
            builtWkt = "POINT(";
            buildingShape = BqsShape.CIRCLE;
        }

        @Override
        public void exitCircle(BqsParser.CircleContext ctx) {
            builtWkt = builtWkt + ")";
        }

        @Override
        public void enterRadius(BqsParser.RadiusContext ctx) {
            numberStr = "";
        }

        @Override
        public void exitRadius(BqsParser.RadiusContext ctx) {
            System.out.println("Converting Number Str: " + numberStr);

            double radius = Double.parseDouble(numberStr);
            Distance distance = new Distance(radius, distanceUnit);
            radiusInMeters = distance.getAs(Distance.LinearUnit.METER);
        }

        @Override
        public void enterEllipse(BqsParser.EllipseContext ctx) {
            builtWkt = "POLYGON(";
            buildingShape = BqsShape.ELLIPSE;
        }

        @Override
        public void exitEllipse(BqsParser.EllipseContext ctx) {
            Coordinate ellipseCenter = new Coordinate(lonDecimalDeg, latDecimalDeg);
            GeometricShapeFactory gsf = new GeometricShapeFactory();
            gsf.setCentre(ellipseCenter);
            gsf.setWidth(ellipseMajorLenMeters / GeoUtils.getLongLengthAtLatitude(latDecimalDeg));
            gsf.setHeight(ellipseMinorLenMeters / GeoUtils.getLatLengthAtLatitude(latDecimalDeg));
            gsf.setNumPoints(30);
            gsf.setRotation(northAngle);
            Polygon ellipse = gsf.createEllipse();
            WKTWriter writer = new WKTWriter();
            builtWkt = writer.write(ellipse);
        }

        @Override
        public void enterMajor_axis_len(BqsParser.Major_axis_lenContext ctx) {
            numberStr = "";
        }

        @Override
        public void exitMajor_axis_len(BqsParser.Major_axis_lenContext ctx) {
            double length = Double.parseDouble(numberStr);
            Distance distance = new Distance(length, distanceUnit);
            ellipseMajorLenMeters = distance.getAs(Distance.LinearUnit.METER);
        }

        @Override
        public void enterMinor_axis_len(BqsParser.Minor_axis_lenContext ctx) {
            numberStr = "";
        }

        @Override
        public void exitMinor_axis_len(BqsParser.Minor_axis_lenContext ctx) {
            double length = Double.parseDouble(numberStr);
            Distance distance = new Distance(length, distanceUnit);
            ellipseMinorLenMeters = distance.getAs(Distance.LinearUnit.METER);
        }

        @Override
        public void enterNorth_angle(BqsParser.North_angleContext ctx) {
            numberStr = "";
        }

        @Override
        public void exitNorth_angle(BqsParser.North_angleContext ctx) {
            northAngle = Integer.parseInt(numberStr);
        }

        @Override
        public void enterLine(BqsParser.LineContext ctx) {
            builtWkt = "LINESTRING(";
            buildingShape = BqsShape.LINE;
        }

        @Override
        public void exitLine(BqsParser.LineContext ctx) {
            builtWkt = builtWkt + ")";
        }

        @Override
        public void enterPolygon_set(BqsParser.Polygon_setContext ctx) {
            //No-Op
        }

        @Override
        public void exitPolygon_set(BqsParser.Polygon_setContext ctx) {
            //No-Op
        }

        @Override
        public void enterHemi(BqsParser.HemiContext ctx) {
            //No-Op
        }

        @Override
        public void exitHemi(BqsParser.HemiContext ctx) {
            //No-Op
        }

        @Override
        public void visitTerminal(TerminalNode terminalNode) {
            //No-Op
        }

        @Override
        public void visitErrorNode(ErrorNode errorNode) {
            //No-Op
        }

        @Override
        public void enterEveryRule(ParserRuleContext parserRuleContext) {
            //No-Op
        }

        @Override
        public void exitEveryRule(ParserRuleContext parserRuleContext) {
            //No-Op
        }

        private void print(String text) {
            for (int i = 0; i < bqsOperatorStack.size(); i++) {
                System.out.print("   ");
            }
            System.out.print(text);
            System.out.print("\n");
        }

        private Number getNumber(String numberStr) {
            return NumberUtils.createNumber(numberStr);
        }
    }
}
