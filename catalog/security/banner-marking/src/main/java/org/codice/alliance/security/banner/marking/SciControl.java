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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Sensitive Compartmented Information controls */
public class SciControl implements Serializable {
  private final String control;

  private final Map<String, List<String>> compartments;

  public SciControl(String marking) {
    String[] split = marking.split("[-]");
    control = split[0];

    if (split.length == 1) {
      compartments = ImmutableMap.of();
      return;
    }

    Map<String, List<String>> tempCompartments = new HashMap<>();
    for (int i = 1; i < split.length; i++) {
      String[] compartment = split[i].split(" ");
      List<String> subComps;
      if (compartment.length > 1) {
        subComps =
            ImmutableList.copyOf(
                Arrays.asList(Arrays.copyOfRange(compartment, 1, compartment.length)));
      } else {
        subComps = ImmutableList.of();
      }
      tempCompartments.put(compartment[0], subComps);
    }

    compartments = ImmutableSortedMap.copyOf(tempCompartments);
  }

  public String getControl() {
    return control;
  }

  public Map<String, List<String>> getCompartments() {
    return compartments;
  }
}
