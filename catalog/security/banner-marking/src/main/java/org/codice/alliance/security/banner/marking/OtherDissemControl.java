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
import java.util.Arrays;
import java.util.List;

/** Other dissemination controls */
public enum OtherDissemControl {
  ACCM(new String[] {"ACCM"}, new String[] {"ACCM"}),
  EXDIS(new String[] {"EXDIS", "EXCLUSIVE DISTRIBUTION"}, new String[] {"XD"}),
  LIMDIS(new String[] {"LIMDIS", "LIMITED DISTRIBUTION"}, new String[] {"DS"}),
  NODIS(new String[] {"NODIS", "NO DISTRIBUTION"}, new String[] {"ND"}),
  SBU(new String[] {"SBU", "SENSITIVE BUT UNCLASSIFIED"}, new String[] {"SBU"}),
  SBU_NOFORN(
      new String[] {"SBU NOFORN", "SENSITIVE BUT UNCLASSIFIED NOFORN"}, new String[] {"SBU-NF"}),
  LES(new String[] {"LES", "LAW ENFORCEMENT SENSITIVE "}, new String[] {"LES"}),
  LES_NOFORN(
      new String[] {"LES NOFORN", "LAW ENFORCEMENT SENSITIVE NOFORN "}, new String[] {"LES-NF"}),
  SSI(new String[] {"SSI", "SENSITIVE SECURITY INFORMATION"}, new String[] {"SSI"});
  private String name;

  private List<String> bannerNames;

  private List<String> portionNames;

  OtherDissemControl(String[] bannerNames, String[] portionNames) {
    this.bannerNames = ImmutableList.copyOf(bannerNames);
    this.portionNames = ImmutableList.copyOf(portionNames);
    name = bannerNames[0];
  }

  public String getName() {
    return name;
  }

  public static OtherDissemControl lookupBannerName(String name) {
    return Arrays.stream(OtherDissemControl.values())
        .filter(dc -> dc.bannerNames.contains(name))
        .findFirst()
        .orElse(null);
  }

  public static OtherDissemControl lookupPortionName(String name) {
    return Arrays.stream(OtherDissemControl.values())
        .filter(dc -> dc.portionNames.contains(name))
        .findFirst()
        .orElse(null);
  }

  public static boolean prefixBannerMatch(String value) {
    return Arrays.stream(OtherDissemControl.values())
        .flatMap(odc -> odc.bannerNames.stream())
        .anyMatch(value::startsWith);
  }

  public static boolean prefixPortionMatch(String value) {
    return Arrays.stream(OtherDissemControl.values())
        .flatMap(odc -> odc.portionNames.stream())
        .anyMatch(value::startsWith);
  }
}
