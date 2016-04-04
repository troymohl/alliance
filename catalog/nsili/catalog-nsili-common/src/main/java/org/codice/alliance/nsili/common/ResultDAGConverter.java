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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.codice.alliance.nsili.common.UCO.AbsTime;
import org.codice.alliance.nsili.common.UCO.AbsTimeHelper;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.codice.alliance.nsili.common.UCO.Edge;
import org.codice.alliance.nsili.common.UCO.Node;
import org.codice.alliance.nsili.common.UCO.NodeType;
import org.codice.alliance.nsili.common.UCO.Time;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;

public class ResultDAGConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultDAGConverter.class);

    public static DAG convertResult(Result result, ORB orb) {
        Double distanceInMeters = result.getDistanceInMeters();
        Double resultScore = result.getRelevanceScore();
        Metacard metacard = result.getMetacard();

        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode(orb);
        graph.addVertex(productNode);

        addCardNodeWithAttributes(graph, productNode, metacard, orb);
        addFileNodeWithAttributes(graph, productNode, metacard, orb);
        addParts(graph, productNode, metacard, orb);

        graph.addVertex(productNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        return dag;
    }

    public static void addCardNodeWithAttributes(DirectedAcyclicGraph<Node, Edge> graph,
            Node productNode, Metacard metacard, ORB orb) {
        Any any = orb.create_any();
        Node cardNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_CARD, any);
        graph.addVertex(cardNode);
        graph.addEdge(productNode, cardNode);

        if (metacard.getId() != null) {
            addStringAttribute(graph, cardNode, NsiliConstants.IDENTIFIER, metacard.getId(), orb);
        }

        if (metacard.getCreatedDate() != null) {
            addDateAttribute(graph,
                    cardNode,
                    NsiliConstants.SOURCE_DATE_TIME_MODIFIED,
                    metacard.getCreatedDate(),
                    orb);
            addDateAttribute(graph,
                    cardNode,
                    NsiliConstants.DATE_TIME_MODIFIED,
                    metacard.getCreatedDate(),
                    orb);
        }
    }

    public static void addFileNodeWithAttributes(DirectedAcyclicGraph<Node, Edge> graph,
            Node productNode, Metacard metacard, ORB orb) {
        Any any = orb.create_any();
        Node fileNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_FILE, any);
        graph.addVertex(fileNode);
        graph.addEdge(productNode, fileNode);

        addBooleanAttribute(graph, fileNode, NsiliConstants.ARCHIVED, false, orb);

        Attribute pocAttr = metacard.getAttribute(Metacard.POINT_OF_CONTACT);
        if (pocAttr != null) {
            String pocString = String.valueOf(pocAttr.getValue());
            if (pocString != null) {
                addStringAttribute(graph, fileNode, NsiliConstants.CREATOR, pocString, orb);
            }
        }

        if (metacard.getResourceSize() != null) {
            try {
                Double resSize = Double.valueOf(metacard.getResourceSize());
                addDoubleAttribute(graph,
                        fileNode,
                        NsiliConstants.EXTENT,
                        resSize,
                        orb);
            } catch (NumberFormatException nfe) {
                LOGGER.warn("Couldn't convert the resource size to double: "+metacard.getResourceSize());
            }
        }

        if (metacard.getContentTypeName() != null) {
            addStringAttribute(graph,
                    fileNode,
                    NsiliConstants.FORMAT,
                    metacard.getContentTypeName(),
                    orb);
        }

        if (metacard.getContentTypeVersion() != null) {
            addStringAttribute(graph,
                    fileNode,
                    NsiliConstants.FORMAT_VERSION,
                    metacard.getContentTypeVersion(),
                    orb);
        }

        if (metacard.getResourceURI() != null) {
            addStringAttribute(graph,
                    fileNode,
                    NsiliConstants.PRODUCT_URL,
                    metacard.getResourceURI().toASCIIString(),
                    orb);
        }

        if (metacard.getTitle() != null ) {
            addStringAttribute(graph, fileNode, NsiliConstants.TITLE, metacard.getTitle(), orb);
        }
    }

    public static void addParts(DirectedAcyclicGraph<Node, Edge> graph,
            Node productNode, Metacard metacard, ORB orb) {
        Any any = orb.create_any();

        //Determine if more than one part specific view is associated with data in this metacard
        boolean partAdded = false;

        if (metacardContainsImageryData(metacard)) {

        }

        if (metacardContainsGmtiData(metacard)) {
            if (partAdded) {

            } else {

            }
        }

        if (metacardContainsMessageData(metacard)) {
            if (partAdded) {

            } else {

            }
        }

        if (metacardContainsReportData(metacard)) {
            if (partAdded) {

            } else {

            }
        }

        if (metacardContainsTdlData(metacard)) {
            if (partAdded) {

            } else {

            }
        }

        if (metacardContainsVideoData(metacard)) {
            if (partAdded) {

            } else {

            }
        }


    }

    public static Node createRootNode(ORB orb) {
        return new Node(0, NodeType.ROOT_NODE, NsiliConstants.NSIL_PRODUCT, orb.create_any());
    }

    public static void addStringAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, String value, ORB orb) {
        Any any = orb.create_any();
        any.insert_wstring(value);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    public static void addIntegerAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, Integer integer, ORB orb) {
        Any any = orb.create_any();
        any.insert_ulong(integer);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    public static void addShortAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, Short shortVal, ORB orb) {
        Any any = orb.create_any();
        any.insert_short(shortVal);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    public static void addDoubleAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, Double doubleVal, ORB orb) {
        Any any = orb.create_any();
        any.insert_double(doubleVal);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    public static void addBooleanAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, Boolean boolVal, ORB orb) {
        Any any = orb.create_any();
        any.insert_boolean(boolVal);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    public static void addAnyAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, Any any, ORB orb) {
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    public static void addDateAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, Date date, ORB orb) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        Any any = orb.create_any();

        AbsTime absTime = new AbsTime(new org.codice.alliance.nsili.common.UCO.Date((short) cal.get(
                Calendar.YEAR),
                (short) (cal.get(Calendar.MONTH) + 1),
                (short) cal.get(Calendar.DAY_OF_MONTH)),
                new Time((short) cal.get(Calendar.HOUR_OF_DAY),
                        (short) cal.get(Calendar.MINUTE),
                        (short) cal.get(Calendar.SECOND)));
        AbsTimeHelper.insert(any, absTime);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    private static boolean metacardContainsImageryData(Metacard metacard) {
        //TODO Implement
        return false;
    }

    private static boolean metacardContainsGmtiData(Metacard metacard) {
        //TODO implement
        return false;
    }

    private static boolean metacardContainsMessageData(Metacard metacard) {
        //TODO implement
        return false;
    }

    private static boolean metacardContainsReportData(Metacard metacard) {
        //TODO implement
        return false;
    }

    private static boolean metacardContainsTdlData(Metacard metacard) {
        //TODO implement
        return false;
    }

    private static boolean metacardContainsVideoData(Metacard metacard) {
        //TODO implement
        return false;
    }
}
