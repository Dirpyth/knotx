/*
 * Copyright (C) 2016 Cognifide Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.knotx.dataobjects;

import io.knotx.util.MultiMapConverter;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.MultiMap;
import java.util.Base64;
import org.apache.commons.lang3.StringUtils;

@DataObject
public class ClientResponse {

  private JsonObject clientResponse;

  public ClientResponse() {
    clientResponse = new JsonObject();
  }

  public ClientResponse(JsonObject json) {
    this.clientResponse = json.copy();
  }

  public JsonObject toJson() {
    return clientResponse.copy();
  }

  public ClientResponse clearBody() {
    clientResponse.remove("body");
    return this;
  }

  public JsonObject toMetadataJson() {
    JsonObject json = new JsonObject();
    json.put("statusCode", clientResponse.getInteger("statusCode"));
    json.put("headers", clientResponse.getJsonObject("headers"));
    return json;
  }


  public int getStatusCode() {
    return clientResponse.getInteger("statusCode");
  }

  public ClientResponse setStatusCode(int statusCode) {
    clientResponse.put("statusCode", statusCode);
    return this;
  }

  public Buffer getBody() {
    return Buffer.buffer(Base64.getDecoder().decode((String) clientResponse.getValue("body",
        StringUtils.EMPTY)));
  }

  public ClientResponse setBody(Buffer body) {
    clientResponse.put("body", body.getBytes());
    return this;
  }

  public MultiMap getHeaders() {
    return MultiMapConverter.fromJsonObject(clientResponse.getJsonObject("headers"));
  }

  public ClientResponse setHeaders(MultiMap headers) {
    clientResponse.put("headers", MultiMapConverter.toJsonObject(headers));
    return this;
  }

  @Override
  public boolean equals(Object o) {
    return clientResponse.equals(o);
  }

  @Override
  public int hashCode() {
    return clientResponse.hashCode();
  }

  @Override
  public String toString() {
    return clientResponse.encodePrettily();
  }
}
