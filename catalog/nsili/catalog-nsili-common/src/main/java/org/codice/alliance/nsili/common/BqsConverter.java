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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import org.codice.alliance.nsili.common.GIAS.Query;
import org.codice.alliance.nsili.common.grammer.BqsLexer;
import org.codice.alliance.nsili.common.grammer.BqsListener;
import org.codice.alliance.nsili.common.grammer.BqsParser;
import org.opengis.filter.Filter;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;

public class BqsConverter {
    private static final String BASIC_BQS_1 = "(identifierUUID like 'Test')";

    private static final String BASIC_BQS_2 =
            "identifierUUID like 'Test' or targetNumber like 'Test'";

    private static final String BASIC_BQS_3 =
            "(identifierUUID like 'Test' or targetNumber like 'Test') and dateTimeModified >= '2016/03/14 06:58:31'";

    private static final String BASIC_BQS_3a =
            "(identifierUUID like 'Test' or targetNumber like 'Test') and (dateTimeModified >= '2016/03/14 06:58:31')";

    private static final String BQS_TEST_STRING =
            "((identifierUUID like 'Test') or (targetNumber like 'Test') or (source like 'Test') or "
                    + "(identifierMission like 'Test') or (category like 'Test') or "
                    + "(decompressionTechnique like 'Test') or (identifier like 'Test') or "
                    + "(recipient like 'Test') or (messageBody like 'Test') or "
                    + "(messageType like 'Test') or (classification like 'Test') or (policy like 'Test') or "
                    + "(releasability like 'Test') or (partIdentifier like 'Test') or (creator like 'Test') or "
                    + "(encodingScheme like 'Test')) and (dateTimeModified >= '2016/03/14 06:58:31')";

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
        //TODO TROY REMOVE

//        System.out.println(tree.toStringTree(parser));

        ParseTreeWalker.DEFAULT.walk(bqsListener, tree);


        //        filterBuilder.anyOf()
//        Filter filterA = filterBuilder.anyOf(filterBuilder.allOf(filterBuilder.attribute("asdf").is().empty(), filterBuilder.attribute("adsf").notEqualTo().text("blah")));
//        System.out.println(filterA.toString());

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
        converter.convertBQSToDDF(BASIC_BQS_3a);
    }

    class BqsTreeWalkerListener implements BqsListener {
        private static final String BQS_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
        private Filter currFilter = null;
        private FilterBuilder filterBuilder;
        private boolean firstTerm = true;
        private String printPrevText;
        private String currText = "";
        private String attribute = "";
        private boolean likeOper = false;
        private boolean existsOper = false;
        private boolean ofOper = false;
        private Stack<String> nestedOperatorStack = new Stack<>();
        private Map<String, List<Filter>> filterBy = new HashMap<>();
        private Stack<BqsOperator> bqsOperatorStack = new Stack<>();
        private HashFunction hashFunction;
        private Calendar calendar = null;
        private String dateStr = "";

        public BqsTreeWalkerListener(FilterBuilder filterBuilder) {
            this.filterBuilder = filterBuilder;
            hashFunction = Hashing.sha512();
        }

        public Filter getFilter() {
            return currFilter;
        }

        @Override
        public void enterQuery(BqsParser.QueryContext ctx) {
            if (!ctx.OR().isEmpty()) {
                bqsOperatorStack.push(BqsOperator.OR);
                print("OR: ");

                String currOperHash = hashFunction.hashBytes(ctx.getText().getBytes()).toString();
                nestedOperatorStack.push(currOperHash);
                filterBy.put(currOperHash, new ArrayList<>());
            }
        }

        @Override
        public void exitQuery(BqsParser.QueryContext ctx) {
            if (!ctx.OR().isEmpty()) {
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
            if (!ctx.AND().isEmpty()) {
                bqsOperatorStack.push(BqsOperator.AND);
                print("AND");

                String currOperHash = hashFunction.hashBytes(ctx.getText().getBytes()).toString();
                nestedOperatorStack.push(currOperHash);
                filterBy.put(currOperHash, new ArrayList<>());
            }
        }

        @Override
        public void exitTerm(BqsParser.TermContext ctx) {
            if (!ctx.AND().isEmpty()) {
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
            if (ctx.LIKE()!=null) {
                print("LIKE");
                bqsOperatorStack.push(BqsOperator.LIKE);
            }
            else if (ctx.EXISTS()!=null) {
                print("EXISTS");
                bqsOperatorStack.push(BqsOperator.EXISTS);
            }
            else if (ctx.OF()!=null) {
                print("OF");
                bqsOperatorStack.push(BqsOperator.OF);
            }
        }

        @Override
        public void exitPrimary(BqsParser.PrimaryContext ctx) {
            if (ctx.LIKE()!=null) {
                bqsOperatorStack.pop();
            }
            else if (ctx.EXISTS()!=null) {
                bqsOperatorStack.pop();
            }
            else if (ctx.OF()!=null) {
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
            if (ctx.EQUAL()!=null) {
                bqsOperatorStack.push(BqsOperator.EQUAL);
            } else if (ctx.NOT()!=null) {
                bqsOperatorStack.push(BqsOperator.NOT);
            } else if (ctx.GT()!=null) {
                bqsOperatorStack.push(BqsOperator.GT);
            } else if (ctx.GTE()!=null) {
                bqsOperatorStack.push(BqsOperator.GTE);
            } else if (ctx.LT()!=null) {
                bqsOperatorStack.push(BqsOperator.LT);
            } else if (ctx.LTE()!=null) {
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

        }

        @Override
        public void enterDate(BqsParser.DateContext ctx) {
            print(ctx.getText());
            dateStr = "";
        }

        @Override
        public void exitDate(BqsParser.DateContext ctx) {
            BqsOperator bqsOperator = bqsOperatorStack.pop();

            Filter filter = null;
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(BQS_DATE_FORMAT);
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date date = dateFormat.parse(dateStr);

                if (bqsOperator==BqsOperator.GTE) {
                    filter = filterBuilder.anyOf(filterBuilder.attribute(attribute).after().date(date), filterBuilder.attribute(attribute).equalTo().date(date));
                } else if (bqsOperator==BqsOperator.GT) {
                    filter = filterBuilder.attribute(attribute).after().date(date);
                } else if (bqsOperator==BqsOperator.LT) {
                    filter = filterBuilder.attribute(attribute).before().date(date);
                } else if (bqsOperator==BqsOperator.LTE) {
                    filter = filterBuilder.anyOf(filterBuilder.attribute(attribute).before().date(date), filterBuilder.attribute(attribute).equalTo().date(date));
                } else if (bqsOperator==BqsOperator.NOT) {
                    filter = filterBuilder.attribute(attribute).notEqualTo().date(date);
                } else if (bqsOperator == BqsOperator.EQUAL) {
                    filter = filterBuilder.attribute(attribute).equalTo().date(date);
                }

                if (filter != null) {
                    List<Filter> filters = filterBy.get(nestedOperatorStack.peek());
                    filters.add(filter);
                }
            } catch (ParseException e) {
                //TODO TROY -- Log this
            }

            attribute = "";
        }

        @Override
        public void enterYear(BqsParser.YearContext ctx) {
            dateStr = dateStr  + ctx.getText();
        }

        @Override
        public void exitYear(BqsParser.YearContext ctx) {

        }

        @Override
        public void enterMonth(BqsParser.MonthContext ctx) {
            dateStr = dateStr + "/" + ctx.getText();
        }

        @Override
        public void exitMonth(BqsParser.MonthContext ctx) {

        }

        @Override
        public void enterDay(BqsParser.DayContext ctx) {
            dateStr = dateStr + "/" + ctx.getText();
        }

        @Override
        public void exitDay(BqsParser.DayContext ctx) {

        }

        @Override
        public void enterHour(BqsParser.HourContext ctx) {
            dateStr = dateStr + " " + ctx.getText();
        }

        @Override
        public void exitHour(BqsParser.HourContext ctx) {

        }

        @Override
        public void enterMinute(BqsParser.MinuteContext ctx) {
            dateStr = dateStr + ":" + ctx.getText();
        }

        @Override
        public void exitMinute(BqsParser.MinuteContext ctx) {

        }

        @Override
        public void enterSecond(BqsParser.SecondContext ctx) {
            dateStr = dateStr + ":" + ctx.getText();
        }

        @Override
        public void exitSecond(BqsParser.SecondContext ctx) {

        }

        @Override
        public void enterGeo_op(BqsParser.Geo_opContext ctx) {
            print(ctx.getText());
        }

        @Override
        public void exitGeo_op(BqsParser.Geo_opContext ctx) {

        }

        @Override
        public void enterRel_geo_op(BqsParser.Rel_geo_opContext ctx) {
            print(ctx.getText());
        }

        @Override
        public void exitRel_geo_op(BqsParser.Rel_geo_opContext ctx) {

        }

        @Override
        public void enterDist_units(BqsParser.Dist_unitsContext ctx) {
            print(ctx.getText());
        }

        @Override
        public void exitDist_units(BqsParser.Dist_unitsContext ctx) {

        }

        @Override
        public void enterGeo_element(BqsParser.Geo_elementContext ctx) {
            print(ctx.getText());
        }

        @Override
        public void exitGeo_element(BqsParser.Geo_elementContext ctx) {

        }

        @Override
        public void enterSign(BqsParser.SignContext ctx) {
            print(ctx.getText());
        }

        @Override
        public void exitSign(BqsParser.SignContext ctx) {

        }

        @Override
        public void enterNumber(BqsParser.NumberContext ctx) {
            currText = currText + ctx.getText();
        }

        @Override
        public void exitNumber(BqsParser.NumberContext ctx) {
            print(currText);
        }

        @Override
        public void enterDigit_seq(BqsParser.Digit_seqContext ctx) {
            print(ctx.getText());
        }

        @Override
        public void exitDigit_seq(BqsParser.Digit_seqContext ctx) {

        }

        @Override
        public void enterQuoted_string(BqsParser.Quoted_stringContext ctx) {
            print(ctx.getText());

        }

        @Override
        public void exitQuoted_string(BqsParser.Quoted_stringContext ctx) {
            BqsOperator bqsOperator = bqsOperatorStack.peek();

            if (bqsOperator==BqsOperator.LIKE) {
                Filter filter = filterBuilder.attribute(attribute).like().text(ctx.getText());

                List<Filter> filters = filterBy.get(nestedOperatorStack.peek());
                filters.add(filter);

                print("FILTER: "+filter);
            }

            attribute = "";
        }

        @Override
        public void enterLatitude(BqsParser.LatitudeContext ctx) {

        }

        @Override
        public void exitLatitude(BqsParser.LatitudeContext ctx) {

        }

        @Override
        public void enterLongitude(BqsParser.LongitudeContext ctx) {

        }

        @Override
        public void exitLongitude(BqsParser.LongitudeContext ctx) {

        }

        @Override
        public void enterDms(BqsParser.DmsContext ctx) {

        }

        @Override
        public void exitDms(BqsParser.DmsContext ctx) {

        }

        @Override
        public void enterLatitude_DMS(BqsParser.Latitude_DMSContext ctx) {

        }

        @Override
        public void exitLatitude_DMS(BqsParser.Latitude_DMSContext ctx) {

        }

        @Override
        public void enterLongitude_DMS(BqsParser.Longitude_DMSContext ctx) {

        }

        @Override
        public void exitLongitude_DMS(BqsParser.Longitude_DMSContext ctx) {

        }

        @Override
        public void enterLatlon(BqsParser.LatlonContext ctx) {

        }

        @Override
        public void exitLatlon(BqsParser.LatlonContext ctx) {

        }

        @Override
        public void enterCoordinate(BqsParser.CoordinateContext ctx) {

        }

        @Override
        public void exitCoordinate(BqsParser.CoordinateContext ctx) {

        }

        @Override
        public void enterPoint(BqsParser.PointContext ctx) {

        }

        @Override
        public void exitPoint(BqsParser.PointContext ctx) {

        }

        @Override
        public void enterPolygon(BqsParser.PolygonContext ctx) {

        }

        @Override
        public void exitPolygon(BqsParser.PolygonContext ctx) {

        }

        @Override
        public void enterRectangle(BqsParser.RectangleContext ctx) {

        }

        @Override
        public void exitRectangle(BqsParser.RectangleContext ctx) {

        }

        @Override
        public void enterUpper_left(BqsParser.Upper_leftContext ctx) {

        }

        @Override
        public void exitUpper_left(BqsParser.Upper_leftContext ctx) {

        }

        @Override
        public void enterLower_right(BqsParser.Lower_rightContext ctx) {

        }

        @Override
        public void exitLower_right(BqsParser.Lower_rightContext ctx) {

        }

        @Override
        public void enterCircle(BqsParser.CircleContext ctx) {

        }

        @Override
        public void exitCircle(BqsParser.CircleContext ctx) {

        }

        @Override
        public void enterRadius(BqsParser.RadiusContext ctx) {

        }

        @Override
        public void exitRadius(BqsParser.RadiusContext ctx) {

        }

        @Override
        public void enterEllipse(BqsParser.EllipseContext ctx) {

        }

        @Override
        public void exitEllipse(BqsParser.EllipseContext ctx) {

        }

        @Override
        public void enterMajor_axis_len(BqsParser.Major_axis_lenContext ctx) {

        }

        @Override
        public void exitMajor_axis_len(BqsParser.Major_axis_lenContext ctx) {

        }

        @Override
        public void enterMinor_axis_len(BqsParser.Minor_axis_lenContext ctx) {

        }

        @Override
        public void exitMinor_axis_len(BqsParser.Minor_axis_lenContext ctx) {

        }

        @Override
        public void enterNorth_angle(BqsParser.North_angleContext ctx) {

        }

        @Override
        public void exitNorth_angle(BqsParser.North_angleContext ctx) {

        }

        @Override
        public void enterLine(BqsParser.LineContext ctx) {

        }

        @Override
        public void exitLine(BqsParser.LineContext ctx) {

        }

        @Override
        public void enterPolygon_set(BqsParser.Polygon_setContext ctx) {

        }

        @Override
        public void exitPolygon_set(BqsParser.Polygon_setContext ctx) {

        }

        @Override
        public void enterHemi(BqsParser.HemiContext ctx) {

        }

        @Override
        public void exitHemi(BqsParser.HemiContext ctx) {

        }

        @Override
        public void visitTerminal(TerminalNode terminalNode) {

        }

        @Override
        public void visitErrorNode(ErrorNode errorNode) {

        }

        @Override
        public void enterEveryRule(ParserRuleContext parserRuleContext) {

        }

        @Override
        public void exitEveryRule(ParserRuleContext parserRuleContext) {

        }

        private void print(String text) {
            for (int i=0; i<bqsOperatorStack.size(); i++) {
                System.out.print("   ");
            }
            System.out.print(text);
            System.out.print("\n");
        }


    }
}
