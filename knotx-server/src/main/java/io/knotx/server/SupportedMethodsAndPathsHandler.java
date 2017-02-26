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
package io.knotx.server;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.ext.web.RoutingContext;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

public class SupportedMethodsAndPathsHandler implements Handler<RoutingContext> {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(SupportedMethodsAndPathsHandler.class);

  private KnotxServerConfiguration configuration;

  private SupportedMethodsAndPathsHandler(KnotxServerConfiguration configuration) {
    this.configuration = configuration;
  }

  public static SupportedMethodsAndPathsHandler create(KnotxServerConfiguration configuration) {
    return new SupportedMethodsAndPathsHandler(configuration);
  }

  @Override
  public void handle(RoutingContext context) {

    boolean shouldRejectMethod = configuration.getEngineRouting().keySet().stream()
        .noneMatch(supportedMethod -> supportedMethod == context.request().method());

    EnumMap<HttpMethod, List<RoutingEntry>> engineRouting = configuration.getEngineRouting();

    Collection<List<RoutingEntry>> engineRoutingValues = engineRouting.values();

    boolean shouldRejectPath = engineRoutingValues.stream().noneMatch(
        routingEntries -> routingEntries.stream()
            .anyMatch(item -> context.request().path().matches(item.path()))
    );

    if (shouldRejectMethod) {
      LOGGER.warn("Requested method {} is not supported based on configuration",
          context.request().method());
      context.fail(HttpResponseStatus.METHOD_NOT_ALLOWED.code());
    } else if (shouldRejectPath) {
      LOGGER.warn("Requested path {} is not supported based on configuration",
          context.request().path());
      context.fail(HttpResponseStatus.NOT_FOUND.code());
    } else {
      context.next();
    }
    if((shouldRejectMethod || shouldRejectPath) && LOGGER.isDebugEnabled()) {
      String engineRoutingJsonString = Json.encodePrettily(engineRouting);
      LOGGER.debug(
          "Allowed paths per method as per the knotx:io.knotx.KnotxServer config found:\n{}",
          engineRoutingJsonString);
    }
  }
}
