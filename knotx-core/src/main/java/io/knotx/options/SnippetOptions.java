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
package io.knotx.options;

import com.google.common.collect.Lists;
import io.knotx.util.StringUtil;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Describes SnippetOptions Knot configuration
 */
@DataObject(generateConverter = true, publicConverter = false)
public class SnippetOptions {

  private static final String DEFAULT_TAG_NAME = "script";
  private static final String DEFAULT_FALLBACK_TAG_NAME = "knotx:fallback";
  private static final String DEFAULT_PARAMS_PREFIX = "data-knotx-";

  private String tagName;
  private String paramsPrefix;
  private String fallbackTagName;
  private String defaultFallback;
  private List<FallbackMetadata> fallbacks;

  /**
   * Default constructor
   */
  public SnippetOptions() {
    init();
    configureDefaultFallback();
  }

  /**
   * Copy constructor
   *
   * @param other the instance to copy
   */
  public SnippetOptions(SnippetOptions other) {
    this.tagName = other.tagName;
    this.paramsPrefix = other.paramsPrefix;
    this.fallbackTagName = other.fallbackTagName;
    this.defaultFallback = other.defaultFallback;
    this.fallbacks = new ArrayList<>(other.fallbacks);
  }

  /**
   * Create an settings from JSON
   *
   * @param json the JSON
   */
  public SnippetOptions(JsonObject json) {
    init();
    SnippetOptionsConverter.fromJson(json, this);
    configureDefaultFallback();
  }

  /**
   * Convert to JSON
   *
   * @return the JSON
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    SnippetOptionsConverter.toJson(this, json);
    return json;
  }

  private void init() {
    tagName = DEFAULT_TAG_NAME;
    paramsPrefix = DEFAULT_PARAMS_PREFIX;
    fallbackTagName = DEFAULT_FALLBACK_TAG_NAME;
  }

  private void configureDefaultFallback() {
    if (CollectionUtils.isEmpty(fallbacks)) {
      fallbacks = Lists.newArrayList(new FallbackMetadata("BLANK", StringUtils.EMPTY));
    }
  }

  /**
   * @return a snippet tag name.
   */
  public String getTagName() {
    return tagName;
  }

  public String getFallbackTagName() {
    return fallbackTagName;
  }

  public String getDefaultFallback() {
    return defaultFallback;
  }

  public SnippetOptions setDefaultFallback(String defaultFallback) {
    this.defaultFallback = defaultFallback;
    return this;
  }

  /**
   * Sets a Knot.x snippet HTML tag name. Default is 'script'
   *
   * @param tagName tag name
   * @return a reference to this, so the API can be used fluently
   */
  public SnippetOptions setTagName(String tagName) {
    this.tagName = tagName;
    return this;
  }

  public SnippetOptions setFallbackTagName(String fallbackTagName) {
    this.fallbackTagName = fallbackTagName;
    return this;
  }

  /**
   * @return a snippet params prefix
   */
  public String getParamsPrefix() {
    return paramsPrefix;
  }

  /**
   * Sets Knot.x snippet parameters prefix. Default is 'data-knotx-'
   *
   * @param paramsPrefix prefix
   * @return a reference to this, so the API can be used fluently
   */
  public SnippetOptions setParamsPrefix(String paramsPrefix) {
    this.paramsPrefix = paramsPrefix == null ? StringUtils.EMPTY : paramsPrefix;
    return this;
  }

  public List<FallbackMetadata> getFallbacks() {
    return fallbacks;
  }

  public SnippetOptions setFallbacks(List<FallbackMetadata> fallbacks) {
    this.fallbacks = fallbacks;
    return this;
  }
}
