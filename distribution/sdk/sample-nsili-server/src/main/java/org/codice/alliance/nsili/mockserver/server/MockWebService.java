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
package org.codice.alliance.nsili.mockserver.server;

import java.io.File;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("data")
public class MockWebService {

  private static final String PRODUCT_RELATIVE_PATH =
      "./src/main/java/org/codice/alliance/nsili/mockserver/data/product.jpg";

  private MockNsili mockNsili;

  /**
   * Default constructor needed only in the context of integration tests outside the use of
   * dependency injection. Instantiated by reflection in @link{MockNsili}
   */
  public MockWebService() {
    this.mockNsili = MockNsili.getInstance();
  }

  @GET
  @Path("product.jpg")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getProductFile() {
    File file = new File(PRODUCT_RELATIVE_PATH);
    Response.ResponseBuilder response = Response.ok(file);
    response.header("Content-Disposition", "attachment; filename=product.jpg");
    return response.build();
  }

  @GET
  @Path("ior.txt")
  @Produces(MediaType.TEXT_PLAIN)
  public Response getIORFile() {
    String responseStr = mockNsili.getIorString() + "\n";
    Response.ResponseBuilder response = Response.ok(responseStr);
    return response.build();
  }
}
