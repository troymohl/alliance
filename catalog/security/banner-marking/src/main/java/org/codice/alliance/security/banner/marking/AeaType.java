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

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Atomic Energy Act Markings */
public enum AeaType {
  RD(new String[] {"RESTRICTED DATA", "RD"}, new String[] {"RD"}),
  FRD(new String[] {"FORMERLY RESTRICTED DATA", "FRD"}, new String[] {"FRD"}),
  DOD_UCNI(
      new String[] {"DOD UNCLASSIFIED CONTROLLED NUCLEAR INFORMATION", "DOD UCNI"},
      new String[] {"DCNI"}),
  DOE_UCNI(
      new String[] {"DOE UNCLASSIFIED CONTROLLED NUCLEAR INFORMATION", "DOE UCNI"},
      new String[] {"UCNI"}),
  TFNI(new String[] {"TRANSCLASSIFIED FOREIGN NUCLEAR INFORMATION", "TFNI"}, new String[] {"TFNI"});

  private String name;

  private List<String> bannerNames;

  private List<String> portionNames;

  private static Map<String, AeaType> aeaTypeMap = new HashMap<>();

  static {
    for (AeaType type : AeaType.values()) {
      for (String banner : type.bannerNames) {
        aeaTypeMap.put(banner, type);
      }
      for (String portion : type.portionNames) {
        aeaTypeMap.put(portion, type);
      }
    }
  }

  AeaType(String[] bannerNames, String[] portionNames) {
    this.bannerNames = ImmutableList.copyOf(bannerNames);
    this.portionNames = ImmutableList.copyOf(portionNames);
    this.name = bannerNames[0];
  }

  /**
   * Gets the name for the type.
   *
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * Looks up AEA Type name from full AEA info (e.g. FRD-XXXX returns AeaType.FRD)
   *
   * @param type AEA type info
   * @return AeaType
   */
  public static AeaType lookupType(String type) {
    for (Map.Entry<String, AeaType> entry : aeaTypeMap.entrySet()) {
      if (type.startsWith(entry.getKey())) {
        return entry.getValue();
      }
    }
    return null;
  }
}
