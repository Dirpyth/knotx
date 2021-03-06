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
package io.knotx.assembler;

import com.google.common.collect.Maps;
import io.knotx.dataobjects.Fragment;
import io.knotx.dataobjects.KnotContext;
import io.knotx.fallback.DefaultFallbackStrategy;
import io.knotx.fallback.FallbackStrategy;
import io.knotx.fragments.FragmentConstants;
import io.knotx.options.SnippetOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import org.apache.commons.lang3.StringUtils;

public class FragmentAssemblerFallbackHandler {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(FragmentAssemblerFallbackHandler.class);

  private final Map<String, FallbackStrategy> fallbackStrategies = Maps.newHashMap();

  private final FragmentAssemblerOptions options;

  public FragmentAssemblerFallbackHandler(FragmentAssemblerOptions options) {
    this.options = options;
    ServiceLoader<FallbackStrategy> fallbackStrategyServiceLoader = ServiceLoader.load(FallbackStrategy.class);
    for (FallbackStrategy strategy : fallbackStrategyServiceLoader) {
      fallbackStrategies.put(strategy.getId(), strategy);
    }
  }

  public String applyFallback(Fragment failed, KnotContext knotContext, Map<String, Fragment> fallbackFragmentCache) {
    Fragment fallback = getFallback(failed, knotContext, fallbackFragmentCache);
    FallbackStrategy strategy = getFallbackStrategy(fallback);
    return strategy.applyFallback(failed, fallback, knotContext);
  }

  private FallbackStrategy getFallbackStrategy(Fragment fallbackFragment) {
    String strategyId = Optional.ofNullable(fallbackFragment.getAttribute(FragmentConstants.FALLBACK_STRATEGY))
        .orElse(DefaultFallbackStrategy.ID);
    return Optional.ofNullable(fallbackStrategies.get(strategyId)).orElseThrow(() -> {
      LOGGER.error("Fragment {} specifies fallback strategy but no fallback strategy with given id was found", fallbackFragment);
      return new IllegalArgumentException(String.format("no strategy with id %s found", strategyId));
    });
  }

  private Fragment getFallback(Fragment failed, KnotContext knotContext, Map<String, Fragment> fallbackFragmentCache) {
    String fallbackId = failed.fallback().get();
    Fragment result = fallbackFragmentCache.get(fallbackId);

    if (result == null) {
      result = knotContext.getFragments().stream()
          .filter(f -> f.isFallback())
          .filter(f -> StringUtils.equals(fallbackId, f.getAttribute(FragmentConstants.FALLBACK_ID)))
          .findFirst()
          .orElse(null);

      if (result == null) {
        result = getGlobalFallback(failed)
            .orElseThrow(() -> {
              LOGGER.error("Fragment {} specifies fallback but no fallback snippet with id '{}' was found", failed, fallbackId);
              return new IllegalArgumentException(String.format("No fallback snippet with id '%s' was found", fallbackId));
            });
      }
      fallbackFragmentCache.put(fallbackId, result);
    }
    return result;
  }

  private Optional<Fragment> getGlobalFallback(Fragment failed) {
    return this.options.getSnippetOptions().getFallbacks().stream()
        .filter(f -> StringUtils.equals(failed.fallback().get(), f.getId()))
        .findFirst()
        .map(fm -> Fragment.fallback(wrap(fm.getMarkup(), this.options.getSnippetOptions()), fm.getId()));
  }

  private String wrap(String fallbackMarkup, SnippetOptions snippetOptions) {
    return String.format("<%s>%s</%s>", snippetOptions.getFallbackTagName(), fallbackMarkup, snippetOptions.getFallbackTagName());
  }

}
