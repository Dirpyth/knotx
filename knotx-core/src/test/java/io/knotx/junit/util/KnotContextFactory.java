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
package io.knotx.junit.util;

import io.knotx.dataobjects.ClientRequest;
import io.knotx.dataobjects.ClientResponse;
import io.knotx.dataobjects.Fragment;
import io.knotx.dataobjects.KnotContext;
import io.knotx.fragments.FragmentConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.reactivex.core.MultiMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Deprecated
public final class KnotContextFactory {

  private KnotContextFactory() {
    //util class
  }

  public static KnotContext empty(List<Fragment> fragments) {
    return empty("").setFragments(fragments);
  }

  public static KnotContext empty() {
    return empty("");
  }

  public static KnotContext empty(String template) {
    return new KnotContext()
        .setClientResponse(
            new ClientResponse()
                .setBody(StringUtils.isEmpty(template) ? Buffer.buffer() : Buffer.buffer(template))
                .setStatusCode(HttpResponseStatus.OK.code())
                .setHeaders(MultiMap.caseInsensitiveMultiMap()))
        .setClientRequest(new ClientRequest());
  }

  public static KnotContext create(List<Triple<List<String>, String, String>> fragments) {
    return new KnotContext()
        .setFragments(
            fragments != null
                ? fragments.stream().map(data -> {
                  Fragment fragment = Fragment.snippet(data.getLeft(), data.getRight(), data.getMiddle());
                  if (data.getMiddle() != null) {
                    fragment.failure(data.getLeft().get(0), new RuntimeException("knot failure"));
                  }
                  if ("_fallback".equals(data.getLeft().get(0))) {
                    fragment.setAttribute(FragmentConstants.FALLBACK_ID, getAttribute(fragment, "data-knotx-fallback-id"));
                  }
                  return fragment;
            }).collect(Collectors.toList())
                : null)
        .setClientRequest(new ClientRequest())
        .setClientResponse(
            new ClientResponse()
                .setHeaders(MultiMap.caseInsensitiveMultiMap()));
  }

  private static String getAttribute(Fragment fragment, String attributeId) {
    Document document = Jsoup.parseBodyFragment(fragment.content());
    Element scriptTag = document.body().child(0);
    List<Attribute> attributes = scriptTag.attributes().asList();
    return attributes.stream()
        .filter(a -> StringUtils.equals(attributeId, a.getKey()))
        .findFirst()
        .map(Attribute::getValue)
        .orElse(null);
  }

}
