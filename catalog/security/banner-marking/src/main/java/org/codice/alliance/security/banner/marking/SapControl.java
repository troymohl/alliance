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
import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Special Access Program controls. */
public class SapControl implements Serializable {
  private static final Pattern CONTROL_PATTERN = Pattern.compile("[/]");

  private boolean multiple;

  private boolean hvsaco;

  private List<String> programs;

  public SapControl(String programString) {
    programs =
        ImmutableList.copyOf(
            CONTROL_PATTERN.splitAsStream(programString).collect(Collectors.toList()));

    multiple = (programs.size() == 1 && programs.contains("MULTIPLE PROGRAMS"));
    if (multiple) {
      programs = ImmutableList.of();
    }

    hvsaco = false;
  }

  public SapControl() {
    programs = ImmutableList.of();
    multiple = false;
    hvsaco = true;
  }

  public boolean isMultiple() {
    return multiple;
  }

  public List<String> getPrograms() {
    return programs;
  }

  public boolean isHvsaco() {
    return hvsaco;
  }

  @Override
  public String toString() {
    if (hvsaco) {
      return "HVSACO";
    }

    StringBuilder sb = new StringBuilder("SAR");
    if (multiple) {
      sb.append("-MULTIPLE PROGRAMS");
      return sb.toString();
    }
    sb.append(programs.stream().collect(Collectors.joining("/", "-", "")));

    return sb.toString();
  }
}
