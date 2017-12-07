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

/** DisseminationControls */
public enum DissemControl {
  RSEN(new String[] {"RSEN", "RISK SENSITIVE"}, new String[] {"RS"}),
  IMCON(new String[] {"IMCON", "CONTROLLED IMAGERY"}, new String[] {"IMC"}),
  NOFORN(new String[] {"NOFORN", "NOT RELEASABLE TO FOREIGN NATIONALS"}, new String[] {"NF"}),
  PROPIN(new String[] {"PROPIN", "CAUTION-PROPRIETARY INFORMATION INVOLVED"}, new String[] {"PR"}),
  RELIDO(
      new String[] {"RELIDO", "RELEASABLE BY INFORMATION DISCLOSURE OFFICIAL"},
      new String[] {"RELIDO"}),
  FISA(new String[] {"FISA", "FOREIGN INTELLIGENCE SURVEILLANCE ACT"}, new String[] {"FISA"}),
  ORCON(new String[] {"ORCON", "ORIGINATOR CONTROLLED"}, new String[] {"OC"}),
  DEA_SENSITIVE(new String[] {"DEA SENSITIVE"}, new String[] {"DSEN"}),
  FOUO(new String[] {"FOUO", "FOR OFFICIAL USE ONLY"}, new String[] {"FOUO"}),
  EYES_ONLY(new String[] {"EYES ONLY"}, new String[] {"EYES"}),
  WAIVED(new String[] {"WAIVED"}, new String[] {"WAIVED"});

  private String name;

  private List<String> bannerNames;

  private List<String> portionNames;

  DissemControl(String[] bannerNames, String[] portionNames) {
    this.bannerNames = ImmutableList.copyOf(bannerNames);
    this.portionNames = ImmutableList.copyOf(portionNames);
    name = bannerNames[0];
  }

  public String getName() {
    return name;
  }

  /**
   * Retrieve Dissemination Control via banner name
   *
   * @param name
   * @return
   */
  public static DissemControl lookupBannerName(String name) {
    return Arrays.stream(DissemControl.values())
        .filter(dc -> dc.bannerNames.contains(name))
        .findFirst()
        .orElse(null);
  }

  /**
   * Retrieve Dissemination Control via portion marking name
   *
   * @param name
   * @return
   */
  public static DissemControl lookupPortionName(String name) {
    return Arrays.stream(DissemControl.values())
        .filter(dc -> dc.portionNames.contains(name))
        .findFirst()
        .orElse(null);
  }
}
