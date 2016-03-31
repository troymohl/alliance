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
package org.codice.alliance.nsili.common.datamodel;

import java.util.ArrayList;
import java.util.List;

import org.codice.alliance.nsili.common.GIAS.AttributeInformation;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.UCO.Cardinality;
import org.codice.alliance.nsili.common.UCO.EntityGraph;
import org.codice.alliance.nsili.common.UCO.EntityNode;
import org.codice.alliance.nsili.common.UCO.EntityRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NsiliDataModel {

    private static EntityGraph allViewGraph;

    private static final Logger LOGGER = LoggerFactory.getLogger(NsiliDataModel.class);

    public NsiliDataModel() {
        init();
    }

    private void init() {
        EntityNode productNode = new EntityNode(0, NsiliConstants.NSIL_PRODUCT);
        EntityNode cardNode = new EntityNode(1, NsiliConstants.NSIL_CARD);
        EntityNode commonNode = new EntityNode(2, NsiliConstants.NSIL_COMMON);
        EntityNode coverageNode = new EntityNode(3, NsiliConstants.NSIL_COVERAGE);
        EntityNode fileNode = new EntityNode(4, NsiliConstants.NSIL_FILE);
        EntityNode gmtiNode = new EntityNode(5, NsiliConstants.NSIL_GMTI);
        EntityNode imageryNode = new EntityNode(6, NsiliConstants.NSIL_IMAGERY);
        EntityNode messageNode = new EntityNode(7, NsiliConstants.NSIL_MESSAGE);
        EntityNode metadataSecurityNode = new EntityNode(8, NsiliConstants.NSIL_METADATA_SECURITY);
        EntityNode partNode = new EntityNode(9, NsiliConstants.NSIL_PART);
        EntityNode relatedFileNode = new EntityNode(10, NsiliConstants.NSIL_RELATED_FILE);
        EntityNode relationNode = new EntityNode(11, NsiliConstants.NSIL_RELATION);
        EntityNode securityNode = new EntityNode(12, NsiliConstants.NSIL_SECURITY);
        EntityNode streamNode = new EntityNode(13, NsiliConstants.NSIL_STREAM);
        EntityNode videoNode = new EntityNode(14, NsiliConstants.NSIL_VIDEO);
        EntityNode approvalNode = new EntityNode(15, NsiliConstants.NSIL_APPROVAL);
        EntityNode exploitationNode = new EntityNode(16, NsiliConstants.NSIL_EXPLOITATION_INFO);
        EntityNode sdsNode = new EntityNode(17, NsiliConstants.NSIL_SDS);
        EntityNode tdlNode = new EntityNode(18, NsiliConstants.NSIL_TDL);
        EntityNode rfiNode = new EntityNode(19, NsiliConstants.NSIL_RFI);
        EntityNode cxpNode = new EntityNode(20, NsiliConstants.NSIL_CXP);
        EntityNode reportNode = new EntityNode(21, NsiliConstants.NSIL_REPORT);
        EntityNode taskNode = new EntityNode(22, NsiliConstants.NSIL_TASK);
        EntityNode sourceNode = new EntityNode(23, NsiliConstants.NSIL_SOURCE);
        EntityNode destinationNode = new EntityNode(24, NsiliConstants.NSIL_DESTINATION);
        EntityNode associationNode = new EntityNode(25, NsiliConstants.NSIL_ASSOCIATION);

        EntityRelationship productAssocationRln = new EntityRelationship(productNode.id, associationNode.id, Cardinality.ONE_TO_ZERO_OR_MORE, Cardinality.ONE_TO_ONE);
        EntityRelationship productApprovalRln = new EntityRelationship(productNode.id, approvalNode.id, Cardinality.ONE_TO_ZERO_OR_ONE, Cardinality.ONE_TO_ONE);
        EntityRelationship productCardRln = new EntityRelationship(productNode.id, cardNode.id, Cardinality.ONE_TO_ONE, Cardinality.ONE_TO_ONE);
        EntityRelationship productFileRln = new EntityRelationship(productNode.id, fileNode.id, Cardinality.ONE_TO_ZERO_OR_ONE, Cardinality.ONE_TO_ONE);
        EntityRelationship productStreamRln = new EntityRelationship(productNode.id, streamNode.id, Cardinality.ONE_TO_ZERO_OR_ONE, Cardinality.ONE_TO_ONE);
        EntityRelationship productMetadataSecurityRln = new EntityRelationship(productNode.id, metadataSecurityNode.id, Cardinality.ONE_TO_ONE, Cardinality.ONE_TO_ONE);
        EntityRelationship productRelatedFileRln = new EntityRelationship(productNode.id, relatedFileNode.id, Cardinality.ONE_TO_ZERO_OR_MORE, Cardinality.ONE_TO_ONE);
        EntityRelationship productSecurityRln = new EntityRelationship(productNode.id, securityNode.id, Cardinality.ONE_TO_ONE, Cardinality.ONE_TO_ONE);
        EntityRelationship productPartRln = new EntityRelationship(productNode.id, partNode.id, Cardinality.ONE_TO_ZERO_OR_MORE, Cardinality.ONE_TO_ONE);
        EntityRelationship partCommonRln = new EntityRelationship(partNode.id, commonNode.id, Cardinality.ONE_TO_ONE, Cardinality.ONE_TO_ONE);
        EntityRelationship partCoverageRln = new EntityRelationship(partNode.id, coverageNode.id, Cardinality.ONE_TO_ZERO_OR_ONE, Cardinality.ONE_TO_ONE);
        EntityRelationship partSecurityRln = new EntityRelationship(partNode.id, coverageNode.id, Cardinality.ONE_TO_ONE, Cardinality.ONE_TO_ZERO_OR_ONE);
        EntityRelationship partExploitationRln = new EntityRelationship(partNode.id, exploitationNode.id, Cardinality.ONE_TO_ZERO_OR_ONE, Cardinality.ONE_TO_ONE);
        EntityRelationship partCxpRln = new EntityRelationship(partNode.id, cxpNode.id, Cardinality.ONE_TO_ZERO_OR_ONE, Cardinality.ONE_TO_ONE);
        EntityRelationship partGmtiRln = new EntityRelationship(partNode.id, gmtiNode.id, Cardinality.ONE_TO_ZERO_OR_ONE, Cardinality.ONE_TO_ONE);
        EntityRelationship partImageryRln = new EntityRelationship(partNode.id, imageryNode.id, Cardinality.ONE_TO_ZERO_OR_ONE, Cardinality.ONE_TO_ONE);
        EntityRelationship partMessageRln = new EntityRelationship(partNode.id, messageNode.id, Cardinality.ONE_TO_ZERO_OR_ONE, Cardinality.ONE_TO_ONE);
        EntityRelationship partReportRln = new EntityRelationship(partNode.id, reportNode.id, Cardinality.ONE_TO_ZERO_OR_ONE, Cardinality.ONE_TO_ONE);
        EntityRelationship partRfiRln = new EntityRelationship(partNode.id, rfiNode.id, Cardinality.ONE_TO_ZERO_OR_ONE, Cardinality.ONE_TO_ONE);
        EntityRelationship partSdsRln = new EntityRelationship(partNode.id, sdsNode.id, Cardinality.ONE_TO_ZERO_OR_ONE, Cardinality.ONE_TO_ONE);
        EntityRelationship partTaskRln = new EntityRelationship(partNode.id, taskNode.id, Cardinality.ONE_TO_ZERO_OR_ONE, Cardinality.ONE_TO_ONE);
        EntityRelationship partTdlRln = new EntityRelationship(partNode.id, tdlNode.id, Cardinality.ONE_TO_ZERO_OR_ONE, Cardinality.ONE_TO_ONE);
        EntityRelationship partVideoRln = new EntityRelationship(partNode.id, videoNode.id, Cardinality.ONE_TO_ZERO_OR_ONE, Cardinality.ONE_TO_ONE);
        EntityRelationship assocCardRln = new EntityRelationship(associationNode.id, cardNode.id, Cardinality.ONE_TO_ONE, Cardinality.ONE_TO_ZERO_OR_MORE);
        EntityRelationship assocSourceRln = new EntityRelationship(associationNode.id, sourceNode.id, Cardinality.ONE_TO_ONE, Cardinality.ONE_TO_ZERO_OR_MORE);
        EntityRelationship assocDestRln = new EntityRelationship(associationNode.id, destinationNode.id, Cardinality.ONE_TO_ONE_OR_MORE, Cardinality.ONE_TO_ZERO_OR_MORE);
        EntityRelationship assocRelationRln = new EntityRelationship(associationNode.id, relationNode.id, Cardinality.ONE_TO_ONE, Cardinality.ONE_TO_ONE);
        EntityRelationship sourceCardRln = new EntityRelationship(sourceNode.id, cardNode.id, Cardinality.ONE_TO_ONE, Cardinality.ONE_TO_ONE);
        EntityRelationship destCardRln = new EntityRelationship(destinationNode.id, cardNode.id, Cardinality.ONE_TO_ONE, Cardinality.ONE_TO_ONE);

        EntityNode allViewNodes[] =
                new EntityNode[] {productNode, cardNode, commonNode, coverageNode, fileNode,
                        gmtiNode, imageryNode, messageNode, metadataSecurityNode, partNode,
                        relatedFileNode, relationNode, securityNode, streamNode, videoNode,
                        approvalNode, exploitationNode, sdsNode, tdlNode, rfiNode, cxpNode,
                        reportNode, taskNode, sourceNode, destinationNode, associationNode,};

        EntityRelationship allViewRelationships[] =
                new EntityRelationship[] {productAssocationRln, productApprovalRln, productCardRln,
                        productFileRln, productStreamRln, productMetadataSecurityRln,
                        productRelatedFileRln, productSecurityRln, productPartRln, partCommonRln,
                        partCoverageRln, partSecurityRln, partExploitationRln, partCxpRln,
                        partGmtiRln, partImageryRln, partMessageRln, partReportRln, partRfiRln,
                        partSdsRln, partTaskRln, partTdlRln, partVideoRln, assocCardRln,
                        assocSourceRln, assocDestRln, assocRelationRln, sourceCardRln,
                        destCardRln,};

        allViewGraph = new EntityGraph(allViewNodes, allViewRelationships);

    }

    public List<AttributeInformation> getAttributeInformation(String entityName) {
        List<AttributeInformation> attributes = new ArrayList<>();

        switch (entityName) {
        case NsiliConstants.NSIL_PRODUCT:
            attributes = new ArrayList<>();
            break;
        case NsiliConstants.NSIL_CARD:
            attributes = NsiliAttributesGenerator.getNsilCardAttributes();
            break;
        case NsiliConstants.NSIL_COMMON:
            attributes = NsiliAttributesGenerator.getNsilCommonAttributes();
            break;
        case NsiliConstants.NSIL_COVERAGE:
            attributes = NsiliAttributesGenerator.getNsilCoverageAttributes();
            break;
        case NsiliConstants.NSIL_FILE:
            attributes = NsiliAttributesGenerator.getNsilFileAttributes();
            break;
        case NsiliConstants.NSIL_GMTI:
            attributes = NsiliAttributesGenerator.getNsilGmtiAttributes();
            break;
        case NsiliConstants.NSIL_IMAGERY:
            attributes = NsiliAttributesGenerator.getNsilImageryAttributes();
            break;
        case NsiliConstants.NSIL_MESSAGE:
            attributes = NsiliAttributesGenerator.getNsilMessageAttributes();
            break;
        case NsiliConstants.NSIL_METADATA_SECURITY:
            attributes = NsiliAttributesGenerator.getNsilMetadataSecurityAttributes();
            break;
        case NsiliConstants.NSIL_PART:
            attributes = NsiliAttributesGenerator.getNsilPartAttributes();
            break;
        case NsiliConstants.NSIL_RELATED_FILE:
            attributes = NsiliAttributesGenerator.getNsilRelatedFileAttributes();
            break;
        case NsiliConstants.NSIL_RELATION:
            attributes = NsiliAttributesGenerator.getNsilRelationAttributes();
            break;
        case NsiliConstants.NSIL_SECURITY:
            attributes = NsiliAttributesGenerator.getNsilSecurityAttributes();
            break;
        case NsiliConstants.NSIL_STREAM:
            attributes = NsiliAttributesGenerator.getNsilStreamAttributes();
            break;
        case NsiliConstants.NSIL_VIDEO:
            attributes = NsiliAttributesGenerator.getNsilVideoAttributes();
            break;
        case NsiliConstants.NSIL_APPROVAL:
            attributes = NsiliAttributesGenerator.getNsilApprovalAttributes();
            break;
        case NsiliConstants.NSIL_EXPLOITATION_INFO:
            attributes = NsiliAttributesGenerator.getNsilExploitationInfoAttributes();
            break;
        case NsiliConstants.NSIL_SDS:
            attributes = NsiliAttributesGenerator.getNsilSdsAttributes();
            break;
        case NsiliConstants.NSIL_TDL:
            attributes = NsiliAttributesGenerator.getNsilTdlAttributes();
            break;
        case NsiliConstants.NSIL_RFI:
            attributes = NsiliAttributesGenerator.getNsilRfiAttributes();
            break;
        case NsiliConstants.NSIL_CXP:
            attributes = NsiliAttributesGenerator.getNsilCxpAttributes();
            break;
        case NsiliConstants.NSIL_REPORT:
            attributes = NsiliAttributesGenerator.getNsilReportAttributes();
            break;
        case NsiliConstants.NSIL_TASK:
            attributes = NsiliAttributesGenerator.getNsilTaskAttributes();
            break;
        case NsiliConstants.NSIL_ASSOCIATION:
            attributes = NsiliAttributesGenerator.getNsilAssocationAttributes();
            break;
        case NsiliConstants.NSIL_SOURCE:
            attributes = NsiliAttributesGenerator.getNsilSourceAttributes();
            break;
        case NsiliConstants.NSIL_DESTINATION:
            attributes = NsiliAttributesGenerator.getNsilDestinationAttributes();
            break;
        default:
            break;
        }

        return attributes;
    }

    public EntityGraph getEntityGraph(String viewName) {
        EntityGraph graph = null;

        switch (viewName) {
        case NsiliConstants.NSIL_ALL_VIEW:
            graph = allViewGraph;
            break;
        default:
            break;
        }

        return graph;
    }

    public List<AttributeInformation> getAttributesForView(String viewName) {
        List<AttributeInformation> attributeInformation = new ArrayList<>();

        EntityGraph graph = getEntityGraph(viewName);
        for (EntityNode node : graph.nodes) {
            List<AttributeInformation> nodeAttrs = getAttributeInformation(node.entity_name);
            if (nodeAttrs != null) {
                attributeInformation.addAll(nodeAttrs);
            }
        }

        return attributeInformation;
    }
}
