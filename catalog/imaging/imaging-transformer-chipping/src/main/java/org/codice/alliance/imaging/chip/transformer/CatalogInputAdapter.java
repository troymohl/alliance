/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.imaging.chip.transformer;

import static org.apache.commons.lang3.Validate.notNull;

import ddf.catalog.content.data.ContentItem;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.types.Core;
import ddf.catalog.operation.ResourceRequest;
import ddf.catalog.operation.impl.ResourceRequestById;
import ddf.catalog.operation.impl.ResourceRequestByProductUri;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A class to convert a Metacard to a ResourceRequest object. */
public class CatalogInputAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(CatalogInputAdapter.class);

  /**
   * @param metacard the Metacard containing the relevant information for the ResourceRequest. May
   *     not be null.
   * @return the ResourceRequest object to query the CatalogFramework service.
   */
  public ResourceRequest buildReadRequest(Metacard metacard, String qualifier) {

    if (metacard == null) {
      throw new IllegalArgumentException("method argument 'metacard' may not be null.");
    }

    if (qualifier == null) {
      throw new IllegalArgumentException("method argument 'qualifier' may not be null.");
    }

    Attribute attribute = metacard.getAttribute(Core.RESOURCE_URI);

    if (attribute != null) {
      Serializable value = attribute.getValue();

      if (value == null) {
        throw new IllegalStateException(
            String.format("metacard attribute %s has no value assigned.", Core.RESOURCE_URI));
      }

      Map<String, Serializable> props = new HashMap<>();
      props.put(ContentItem.QUALIFIER_KEYWORD, qualifier);

      try {
        return new ResourceRequestByProductUri(new URI((String) value), props);
      } catch (URISyntaxException e) {
        throw new IllegalArgumentException(
            String.format(
                "The supplied metacard does contains an invalid '%s' attribute.",
                Core.RESOURCE_URI));
      }
    }

    throw new IllegalStateException(
        String.format(
            "The supplied metacard does not contain the '%s' attribute.", Core.RESOURCE_URI));
  }

  /**
   * Construct a resource request for the object represented by the metacard.
   *
   * @param metacard must be non-null
   * @return a resource request than can be passed to the catalog framework
   */
  @SuppressWarnings("unused")
  public ResourceRequest buildReadRequest(Metacard metacard) {
    notNull(metacard, "method argument 'metacard' may not be null.");
    LOGGER.trace("building a framework resource request for id '{}'", metacard.getId());
    return new ResourceRequestById(metacard.getId());
  }

  public String getResourceSiteName(Metacard metacard) {
    return metacard.getSourceId();
  }
}
