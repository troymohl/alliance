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
 **/
package org.codice.alliance.nsili.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import ddf.catalog.data.ContentType;
import ddf.catalog.data.impl.ContentTypeImpl;

public class NsiliConstants {

    public static final String STANAG_VERSION = "STANAG 4559";

    public static final Set<String> CONTENT_STRINGS = new HashSet<>(CollectionUtils.collect(
            Arrays.asList(NsiliProductType.values()), (Object object) -> {
                NsiliProductType type = (NsiliProductType) object;
                return type.getSpecName();
            }));

    public static final Set<ContentType> CONTENT_TYPES = new HashSet<>(CollectionUtils.collect(
            Arrays.asList(NsiliProductType.values()), (Object object) -> {
                NsiliProductType type = (NsiliProductType) object;
                return new ContentTypeImpl(type.getSpecName(), STANAG_VERSION);
            }));

    // Entity Fields
    public static final String NSIL_PRODUCT = "NSIL_PRODUCT";

    public static final String NSIL_ALL_VIEW = "NSIL_ALL_VIEW";

    public static final String NSIL_DESTINATION = "NSIL_DESTINATION";

    public static final String NSIL_ASSOCIATION = "NSIL_ASSOCIATION";

    public static final String NSIL_RELATION = "NSIL_RELATION";

    public static final String NSIL_SOURCE = "NSIL_SOURCE";

    public static final String NSIL_VIDEO = "NSIL_VIDEO";

    public static final String NSIL_MESSAGE = "NSIL_MESSAGE";

    public static final String NSIL_IMAGERY = "NSIL_IMAGERY";

    public static final String NSIL_GMTI = "NSIL_GMTI";

    public static final String NSIL_COMMON = "NSIL_COMMON";

    public static final String NSIL_PART = "NSIL_PART";

    public static final String NSIL_RELATED_FILE = "NSIL_RELATED_FILE";

    public static final String NSIL_STREAM = "NSIL_STREAM";

    public static final String NSIL_SECURITY = "NSIL_SECURITY";

    public static final String NSIL_METADATA_SECURITY = "NSIL_METADATA_SECURITY";

    public static final String NSIL_FILE = "NSIL_FILE";

    public static final String NSIL_CARD = "NSIL_CARD";

    public static final String NSIL_APPROVAL = "NSIL_APPROVAL";

    public static final String NSIL_COVERAGE = "NSIL_COVERAGE";

    public static final String NSIL_CXP = "NSIL_CXP";

    public static final String NSIL_EXPLOITATION_INFO = "EXPLOITATION_INFO";

    public static final String NSIL_REPORT = "NSIL_REPORT";

    public static final String NSIL_IR = "NSIL_IR";

    public static final String NSIL_RFI = "NSIL_RFI";

    public static final String NSIL_SDS = "NSIL_SDS";

    public static final String NSIL_TASK = "NSIL_TASK";

    public static final String NSIL_TDL = "NSIL_TDL";

    // Attribute Fields
    public static final String IDENTIFIER_UUID = "identifierUUID";

    public static final String TYPE = "type";

    public static final String IDENTIFIER_JOB = "identifierJob";

    public static final String NUMBER_OF_TARGET_REPORTS = "numberOfTargetReports";

    public static final String CATEGORY = "category";

    public static final String RECIPIENT = "recipient";

    public static final String SUBJECT = "subject";

    public static final String DECOMPRESSION_TECHNIQUE = "decompressionTechnique";

    public static final String NUMBER_OF_BANDS = "numberOfBands";

    public static final String IDENTIFIER = "identifier";

    public static final String MESSAGE_BODY = "messageBody";

    public static final String MESSAGE_TYPE = "messageType";

    public static final String ENCODING_SCHEME = "encodingScheme";

    public static final String PART_IDENTIFIER = "partIdentifier";

    public static final String DATE_TIME_DECLARED = "dateTimeDeclared";

    public static final String PRODUCT_URL = "productURL";

    public static final String CLASSIFICATION = "classification";

    public static final String POLICY = "policy";

    public static final String RELEASABILITY = "releasability";

    public static final String CREATOR = "creator";

    public static final String DESCRIPTION_ABSTRACT = "descriptionAbstract";

    public static final String IDENTIFIER_MISSION = "identifierMission";

    public static final String IDENTIFIER_JC3IEDM = "identifierJC3IEDM";

    public static final String LANGUAGE = "language";

    public static final String SOURCE = "source";

    public static final String SUBJECT_CATEGORY_TARGET = "subjectCategoryTarget";

    public static final String TARGET_NUMBER = "targetNumber";

    public static final String SOURCE_DATE_TIME_MODIFIED = "sourceDateTimeModified";

    public static final String DATE_TIME_MODIFIED = "dateTimeModified";

    public static final String PUBLISHER = "publisher";

    public static final String SOURCE_LIBRARY = "sourceLibrary";

    public static final String SPATIAL_COUNTRY_CODE = "spatialCountryCode";

    public static final String SPATIAL_GEOGRAPHIC_REF_BOX = "spatialGeographicReferenceBox";

    public static final String TEMPORAL_START = "temporalStart";

    public static final String TEMPORAL_END = "temporalEnd";

    public static final String ARCHIVED = "archived";

    public static final String ARCHIVE_INFORMATION = "archiveInformation";

    public static final String EXTENT = "extent";

    public static final String FORMAT = "format";

    public static final String FORMAT_VERSION = "formatVersion";

    public static final String TITLE = "title";

    public static final String CLOUD_COVER_PCT = "cloudCoverPercentage";

    public static final String COMMENTS = "comments";

    public static final String NIIRS = "NIIRS";

    public static final String NUMBER_OF_ROWS = "numberOfRows";

    public static final String NUMBER_OF_COLS = "numberOfColumns";

    public static final String STANDARD = "standard";

    public static final String STANDARD_VERSION = "standardVersion";

    public static final String SOURCE_URL = "sourceURL";

    public static final String PROGRAM_ID = "programID";

    public static final String AVG_BIT_RATE = "averageBitRate";

    public static final String FRAME_RATE = "frameRate";

    public static final String METADATA_ENC_SCHEME = "metadataEncodingScheme";

    public static final String MISM_LEVEL = "MISMLevel";

    public static final String SCANNING_MODE = "scanningMode";

    public static final String STATUS = "status";

    public static final String FOR_ACTION = "forAction";

    public static final String FOR_INFORMATION = "forInformation";

    public static final String SERIAL_NUMBER = "serialNumber";

    public static final String WORKFLOW_STATUS = "workflowStatus";

    public static final String DESCRIPTION = "description";

    public static final String LEVEL = "level";

    public static final String AUTO_GENERATED = "autoGenerated";

    public static final String SUBJ_QUALITY_CODE = "subjectiveQualityCode";

    public static final String OPERATIONAL_STATUS = "operationalStatus";

    public static final String ACTIVITY = "activity";

    public static final String MESSAGE_NUM = "messageNumber";

    public static final String PLATFORM = "platform";

    public static final String TRACK_NUM = "trackNumber";

    public static final String APPROVED_BY = "approvedBy";

    public static final String ORIGINATORS_REQ_SERIAL_NUM = "originatorsRequestSerialNumber";

    public static final String PRIORITY = "priority";

}