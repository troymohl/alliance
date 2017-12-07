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
package org.codice.alliance.security.banner.marking;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/** Classification enumeration */
public enum ClassificationLevel {
  UNCLASSIFIED("UNCLASSIFIED", "U"),
  RESTRICTED("RESTRICTED", "R"),
  CONFIDENTIAL("CONFIDENTIAL", "C"),
  SECRET("SECRET", "S"),
  TOP_SECRET("TOP SECRET", "TS");

  private String name;

  private String shortName;

  private static final Map<String, ClassificationLevel> LOOKUP_MAP =
      Arrays.stream(ClassificationLevel.values())
          .collect(Collectors.toMap(cl -> cl.name, cl -> cl));

  private static final Map<String, ClassificationLevel> SHORTNAME_LOOKUP =
      Arrays.stream(ClassificationLevel.values())
          .collect(Collectors.toMap(cl -> cl.shortName, cl -> cl));

  ClassificationLevel(String name, String shortName) {
    this.name = name;
    this.shortName = shortName;
  }

  /**
   * Returns the long name of the classification.
   *
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the short name of the classification.
   *
   * @return
   */
  public String getShortName() {
    return shortName;
  }

  /**
   * Get enum value from long name string.
   *
   * @param name
   * @return
   */
  public static ClassificationLevel lookup(String name) {
    return LOOKUP_MAP.get(name);
  }

  /**
   * Get enum value from short name string.
   *
   * @param name
   * @return
   */
  public static ClassificationLevel lookupByShortname(String name) {
    return SHORTNAME_LOOKUP.get(name);
  }
}
