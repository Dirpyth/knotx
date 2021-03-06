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

import static io.knotx.junit5.util.RequestUtil.subscribeToResult_shouldSucceed;

import io.knotx.dataobjects.KnotContext;
import io.knotx.junit.util.FileReader;
import io.knotx.junit.util.KnotContextFactory;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.reactivex.proxy.KnotProxy;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
public class FragmentAssemblerTest {

  private final static String ADDRESS = "knotx.core.assembler";
  private final static String RAW = "_raw";
  private final static String FALLBACK = "_fallback";
  private static final String SERVICES = "services";
  private static final String HANDLEBARS = "handlebars";

  @Test
  @KnotxApplyConfiguration("io/knotx/assembler/test.unwrap.io.knotx.FragmentAssembler.json")
  public void callAssemblerWithNoSnippets_expectInternalServerError(
      VertxTestContext context, Vertx vertx) {
    callAssemblerWithAssertions(context, vertx,
        null,
        knotContext -> Assertions.assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(),
            knotContext.getClientResponse().getStatusCode()
        ));
  }

  @Test
  @KnotxApplyConfiguration("io/knotx/assembler/test.unwrap.io.knotx.FragmentAssembler.json")
  public void callAssemblerWithEmptySnippet_expectNoContentStatus(
      VertxTestContext context, Vertx vertx) {
    callAssemblerWithAssertions(context, vertx,
        Collections
            .singletonList(new ImmutableTriple<>(Collections.singletonList(RAW), null, StringUtils.SPACE)),
        knotContext -> Assertions.assertEquals(HttpResponseStatus.NO_CONTENT.code(),
            knotContext.getClientResponse().getStatusCode()));
  }

  @Test
  @KnotxApplyConfiguration("io/knotx/assembler/test.asIs.io.knotx.FragmentAssembler.json")
  public void callAssemblerWithManySnippets_expectAsIsResult(
      VertxTestContext context, Vertx vertx) throws IOException {
    List<Triple<List<String>, String, String>> fragments = Arrays.asList(
        toTriple("io/knotx/assembler/fragment1.txt",null,  RAW),
        toTriple("io/knotx/assembler/fragment2.txt", null, SERVICES, HANDLEBARS),
        toTriple("io/knotx/assembler/fragment3.txt", null, RAW));
    String expectedResult = FileReader.readText("io/knotx/server/expectedAsIsResult.html");
    callAssemblerWithAssertions(context, vertx,
        fragments,
        knotContext -> {
          Assertions.assertEquals(HttpResponseStatus.OK.code(),
              knotContext.getClientResponse().getStatusCode());
          Assertions
              .assertEquals(expectedResult, knotContext.getClientResponse().getBody().toString());
        });
  }

  @Test
  @KnotxApplyConfiguration("io/knotx/assembler/test.unwrap.io.knotx.FragmentAssembler.json")
  public void callAssemblerWithManySnippets_expectUnwrapResult(
      VertxTestContext context, Vertx vertx) throws IOException {
    List<Triple<List<String>, String, String>> fragments = Arrays.asList(
        toTriple("io/knotx/assembler/fragment1.txt", null, RAW),
        toTriple("io/knotx/assembler/fragment2.txt", null, SERVICES, HANDLEBARS),
        toTriple("io/knotx/assembler/fragment3.txt", null, RAW));
    String expectedResult = FileReader.readText("io/knotx/server/expectedUnwrapResult.html");
    callAssemblerWithAssertions(context, vertx,
        fragments,
        knotContext -> {
          Assertions.assertEquals(HttpResponseStatus.OK.code(),
              knotContext.getClientResponse().getStatusCode());
          Assertions
              .assertEquals(expectedResult, knotContext.getClientResponse().getBody().toString());
        });
  }

  @Test
  @KnotxApplyConfiguration("io/knotx/assembler/test.ignore.io.knotx.FragmentAssembler.json")
  public void callAssemblerWithManySnippets_expectIgnoreResult(
      VertxTestContext context, Vertx vertx) throws IOException {
    List<Triple<List<String>,String, String>> fragments = Arrays.asList(
        toTriple("io/knotx/assembler/fragment1.txt", null, RAW),
        toTriple("io/knotx/assembler/fragment2.txt", null, SERVICES, HANDLEBARS),
        toTriple("io/knotx/assembler/fragment3.txt", null, RAW));
    String expectedResult = FileReader.readText("io/knotx/server/expectedIgnoreResult.html");
    callAssemblerWithAssertions(context, vertx,
        fragments,
        knotContext -> {
          Assertions.assertEquals(HttpResponseStatus.OK.code(),
              knotContext.getClientResponse().getStatusCode());
          Assertions
              .assertEquals(expectedResult, knotContext.getClientResponse().getBody().toString());
        });
  }

  @Test
  @KnotxApplyConfiguration("io/knotx/assembler/test.asIs.io.knotx.FragmentAssembler.json")
  public void callAssemblerWithFailedSnippet_expectFallback(
      VertxTestContext context, Vertx vertx) throws IOException {
    List<Triple<List<String>,String, String>> fragments = Arrays.asList(
        toTriple("io/knotx/assembler/fragment1.txt", null, RAW),
        toTriple("io/knotx/assembler/fragment2.txt", "FALLBACK_1", SERVICES, HANDLEBARS),
        toTriple("io/knotx/assembler/fragment3.txt", null, RAW),
        toTriple("io/knotx/assembler/fallback.txt", null, FALLBACK ));
    String expectedResult = FileReader.readText("io/knotx/server/expectedFallbackResult.html");
    callAssemblerWithAssertions(context, vertx,
        fragments,
        knotContext -> {
          Assertions.assertEquals(HttpResponseStatus.OK.code(),
              knotContext.getClientResponse().getStatusCode());
          Assertions
              .assertEquals(expectedResult, knotContext.getClientResponse().getBody().toString());
        });
  }

  @Test
  @KnotxApplyConfiguration("io/knotx/assembler/test.asIs.io.knotx.FragmentAssembler.json")
  public void callAssemblerWithFailedSnippet_expectFallbackForBlank(
      VertxTestContext context, Vertx vertx) throws IOException {
    List<Triple<List<String>,String, String>> fragments = Arrays.asList(
        toTriple("io/knotx/assembler/fragment1.txt", null, RAW),
        toTriple("io/knotx/assembler/fragment2.txt", "BLANK", SERVICES, HANDLEBARS),
        toTriple("io/knotx/assembler/fragment3.txt", null, RAW));
    String expectedResult = FileReader.readText("io/knotx/server/expectedFallbackForBlankResult.html");
    callAssemblerWithAssertions(context, vertx,
        fragments,
        knotContext -> {
          Assertions.assertEquals(HttpResponseStatus.OK.code(),
              knotContext.getClientResponse().getStatusCode());
          Assertions
              .assertEquals(expectedResult, knotContext.getClientResponse().getBody().toString());
        });
  }

  private void callAssemblerWithAssertions(
      VertxTestContext context, Vertx vertx,
      List<Triple<List<String>, String, String>> fragments,
      Consumer<KnotContext> verifyResultFunction) {
    KnotProxy service = KnotProxy.createProxy(vertx, ADDRESS);

    Single<KnotContext> knotContextSingle = service.rxProcess(KnotContextFactory.create(fragments));

    subscribeToResult_shouldSucceed(context, knotContextSingle, verifyResultFunction);
  }

  private Triple<List<String>, String, String> toTriple(String filePath, String failed, String... knots) throws IOException {
    return new ImmutableTriple<>(Arrays.asList(knots), failed, FileReader.readText(filePath));
  }

}
