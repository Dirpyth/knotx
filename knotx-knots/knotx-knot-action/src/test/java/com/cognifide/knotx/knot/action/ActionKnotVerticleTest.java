/*
 * Knot.x - Reactive microservice assembler - Action Knot Verticle
 *
 * Copyright (C) 2016 Cognifide Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cognifide.knotx.knot.action;

import com.cognifide.knotx.launcher.junit.FileReader;
import com.cognifide.knotx.launcher.junit.KnotxConfiguration;
import com.cognifide.knotx.launcher.junit.TestVertxDeployer;
import com.google.common.collect.Lists;

import com.cognifide.knotx.dataobjects.AdapterRequest;
import com.cognifide.knotx.dataobjects.AdapterResponse;
import com.cognifide.knotx.dataobjects.ClientResponse;
import com.cognifide.knotx.dataobjects.KnotContext;
import com.cognifide.knotx.fragments.Fragment;
import com.cognifide.knotx.http.MultiMapCollector;
import com.cognifide.knotx.junit.KnotContextFactory;
import com.cognifide.knotx.junit.Logback;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rxjava.core.MultiMap;
import io.vertx.rxjava.core.buffer.Buffer;
import rx.Observable;
import rx.functions.Action1;

@RunWith(VertxUnitRunner.class)
public class ActionKnotVerticleTest {

  public static final String EXPECTED_KNOT_TRANSITION = "next";
  private final static String ADDRESS = "knotx.knot.action";
  private final static String HIDDEN_INPUT_TAG_NAME = "snippet-identifier";
  private static final String FRAGMENT_KNOT = "data-knot-types";
  private final static String FRAGMENT_REDIRECT_IDENTIFIER = "someId123";
  private final static String FRAGMENT_SELF_IDENTIFIER = "someId456";
  private final static Fragment FIRST_FRAGMENT = Fragment.raw("<html><head></head><body>");
  private final static Fragment LAST_FRAGMENT = Fragment.raw("</body></html>");
  private static final Document.OutputSettings OUTPUT_SETTINGS = new Document.OutputSettings()
      .escapeMode(Entities.EscapeMode.xhtml)
      .indentAmount(0)
      .prettyPrint(false);
  //Test Runner Rule of Verts
  private RunTestOnContext vertx = new RunTestOnContext();

  //Test Runner Rule of Knotx
  private TestVertxDeployer knotx = new TestVertxDeployer(vertx);

  //Junit Rule, sets up logger, prepares verts, starts verticles according to the config (supplied in annotation of test method)
  @Rule
  public RuleChain chain = RuleChain.outerRule(new Logback()).around(vertx).around(knotx);

  @Test
  @KnotxConfiguration("knotx-test.json")
  public void callGetWithNoActionFragments_expectResponseOkNoFragmentChanges(TestContext context) throws Exception {
    String expectedTemplatingFragment = FileReader.readText("fragment_templating_out.txt");
    KnotContext knotContext = createKnotContext(FIRST_FRAGMENT, LAST_FRAGMENT, "fragment_templating_in.txt");
    knotContext.clientRequest().setMethod(HttpMethod.GET);

    callActionKnotWithAssertions(context, knotContext,
        clientResponse -> {
          context.assertEquals(HttpResponseStatus.OK, clientResponse.clientResponse().statusCode());
          context.assertTrue(clientResponse.transition().isPresent());
          context.assertEquals("next", clientResponse.transition().get());
          context.assertTrue(clientResponse.fragments().isPresent());

          List<Fragment> fragments = clientResponse.fragments().get();
          context.assertEquals(FIRST_FRAGMENT.content(), fragments.get(0).content());
          context.assertEquals(expectedTemplatingFragment, fragments.get(1).content());
          context.assertEquals(LAST_FRAGMENT.content(), fragments.get(2).content());
        },
        error -> context.fail(error.getMessage()));
  }

  @Test
  @KnotxConfiguration("knotx-test.json")
  public void callGetWithTwoActionFragments_expectResponseOkTwoFragmentChanges(TestContext context) throws Exception {
    String expectedRedirectFormFragment = FileReader.readText("fragment_form_redirect_out.txt");
    String expectedSelfFormFragment = FileReader.readText("fragment_form_self_out.txt");
    KnotContext knotContext = createKnotContext(FIRST_FRAGMENT, LAST_FRAGMENT, "fragment_form_redirect_in.txt", "fragment_form_self_in.txt");
    knotContext.clientRequest().setMethod(HttpMethod.GET);

    callActionKnotWithAssertions(context, knotContext,
        clientResponse -> {
          context.assertEquals(HttpResponseStatus.OK, clientResponse.clientResponse().statusCode());
          context.assertTrue(clientResponse.transition().isPresent());
          context.assertEquals(EXPECTED_KNOT_TRANSITION, clientResponse.transition().get());
          context.assertTrue(clientResponse.fragments().isPresent());

          List<Fragment> fragments = clientResponse.fragments().get();
          context.assertEquals(FIRST_FRAGMENT.content(), fragments.get(0).content());
          context.assertEquals(clean(expectedRedirectFormFragment), clean(fragments.get(1).content()));
          context.assertEquals(clean(expectedSelfFormFragment), clean(fragments.get(2).content()));
          context.assertEquals(LAST_FRAGMENT.content(), fragments.get(3).content());
        },
        error -> context.fail(error.getMessage()));
  }

  @Test
  @KnotxConfiguration("knotx-test.json")
  public void callGetWithActionFragmentWithoutIdentifier_expectResponseOkWithOneFragmentChanges(TestContext context) throws Exception {
    KnotContext knotContext = createKnotContext("fragment_form_no_identifier_in.txt");
    String expectedFragmentHtml = FileReader.readText("fragment_form_no_identifier_out.txt");
    knotContext.clientRequest().setMethod(HttpMethod.GET);

    callActionKnotWithAssertions(context, knotContext,
        clientResponse -> {
          context.assertEquals(HttpResponseStatus.OK, clientResponse.clientResponse().statusCode());
          context.assertTrue(clientResponse.transition().isPresent());
          context.assertEquals(EXPECTED_KNOT_TRANSITION, clientResponse.transition().get());
          context.assertTrue(clientResponse.fragments().isPresent());

          List<Fragment> fragments = clientResponse.fragments().get();
          context.assertEquals(fragments.size(), 1);
          context.assertEquals(clean(expectedFragmentHtml), clean(fragments.get(0).content()));
        },
        error -> context.fail(error.getMessage()));
  }

  @Test
  @KnotxConfiguration("knotx-test.json")
  public void callGetWithActionFragmentActionHandlerNotExists_expectStatusCode500(TestContext context) throws Exception {
    KnotContext knotContext = createKnotContext("fragment_form_actionhandler_not_exists_in.txt");
    knotContext.clientRequest().setMethod(HttpMethod.GET);

    callActionKnotWithAssertions(context, knotContext,
        clientResponse -> {
          context.assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, clientResponse.clientResponse().statusCode());
          context.assertFalse(clientResponse.transition().isPresent());
          context.assertFalse(clientResponse.fragments().isPresent());
        },
        error -> context.fail(error.getMessage()));
  }

  @Test
  @KnotxConfiguration("knotx-test.json")
  public void callPostWithTwoActionFragments_expectResponseOkWithTransitionStep2(TestContext context) throws Exception {
    createKnotConsumer("address-redirect", "", "step2");
    KnotContext knotContext = createKnotContext("fragment_form_redirect_in.txt", "fragment_form_self_in.txt");
    knotContext.clientRequest()
        .setMethod(HttpMethod.POST)
        .setFormAttributes(MultiMap.caseInsensitiveMultiMap().add(HIDDEN_INPUT_TAG_NAME, FRAGMENT_REDIRECT_IDENTIFIER));

    callActionKnotWithAssertions(context, knotContext,
        clientResponse -> {
          context.assertEquals(HttpResponseStatus.MOVED_PERMANENTLY, clientResponse.clientResponse().statusCode());
          context.assertEquals("/content/form/step2.html", clientResponse.clientResponse().headers().get("Location"));
          context.assertFalse(clientResponse.transition().isPresent());
          context.assertFalse(clientResponse.fragments().isPresent());
        },
        error -> context.fail(error.getMessage()));
  }

  @Test
  @KnotxConfiguration("knotx-test.json")
  public void callPostWithActionFragmentWithoutRequestedFragmentIdentifier_expectStatusCode500(TestContext context) throws Exception {
    KnotContext knotContext = createKnotContext("fragment_form_incorrect_identifier_in.txt");
    knotContext.clientRequest().setMethod(HttpMethod.POST);

    callActionKnotWithAssertions(context, knotContext,
        clientResponse -> {
          context.assertEquals(HttpResponseStatus.NOT_FOUND, clientResponse.clientResponse().statusCode());
          context.assertFalse(clientResponse.fragments().isPresent());
          context.assertFalse(clientResponse.transition().isPresent());
        },
        error -> context.fail(error.getMessage()));
  }

  @Test
  @KnotxConfiguration("knotx-test.json")
  public void callPostWithActionFragmentWithIncorrectSnippetId_expectStatusCode500(TestContext context) throws Exception {
    KnotContext knotContext = createKnotContext("fragment_form_redirect_in.txt");
    knotContext.clientRequest().setMethod(HttpMethod.POST)
        .setFormAttributes(MultiMap.caseInsensitiveMultiMap().add(HIDDEN_INPUT_TAG_NAME, "snippet_id_not_exists"));

    callActionKnotWithAssertions(context, knotContext,
        clientResponse -> {
          context.assertEquals(HttpResponseStatus.NOT_FOUND, clientResponse.clientResponse().statusCode());
          context.assertFalse(clientResponse.fragments().isPresent());
          context.assertFalse(clientResponse.transition().isPresent());
        },
        error -> context.fail(error.getMessage()));
  }

  private void callActionKnotWithAssertions(TestContext context, KnotContext knotContext, Action1<KnotContext> onSuccess, Action1<Throwable> onError) {
    Async async = context.async();

    vertx.vertx().eventBus().<KnotContext>send(ADDRESS, knotContext, ar -> {
      if (ar.succeeded()) {
        Observable
            .just(ar.result().body())
            .subscribe(
                onSuccess,
                onError,
                async::complete
            );
      } else {
        context.fail(ar.cause());
      }
    });
  }

  private KnotContext createKnotContext(String... snippetFilenames) throws Exception {
    return createKnotContext(null, null, snippetFilenames);
  }

  private KnotContext createKnotContext(Fragment firstFragment, Fragment lastFragment, String... snippetFilenames) throws Exception {
    List<Fragment> fragments = Lists.newArrayList();
    Optional.ofNullable(firstFragment).ifPresent(fragments::add);
    for (String file : snippetFilenames) {
      String fileContent = FileReader.readText(file);
      String fragmentIdentifiers = Jsoup.parse(fileContent).getElementsByAttribute(FRAGMENT_KNOT).attr(
          FRAGMENT_KNOT);
      fragments.add(Fragment.snippet(Arrays.asList(fragmentIdentifiers.split(",")), fileContent));
    }
    Optional.ofNullable(lastFragment).ifPresent(fragments::add);

    return KnotContextFactory.empty(fragments);
  }

  private String clean(String text) {
    String cleanText = text.replace("\n", "").replaceAll(">(\\s)+<", "><").replaceAll(">(\\s)+\\{", ">{").replaceAll("\\}(\\s)+<", "}<");
    return Jsoup.parse(cleanText, "UTF-8", Parser.xmlParser())
        .outputSettings(OUTPUT_SETTINGS)
        .html()
        .trim();
  }

  private void createKnotConsumer(String adddress, String addToBody, String signal) {
    createKnotConsumer(adddress, addToBody, signal, Collections.emptyMap());
  }

  private void createKnotConsumer(String adddress, String addToBody, String signal, Map<String, List<String>> headers) {
    EventBus eventBus = vertx.vertx().eventBus();
    eventBus.<AdapterRequest>consumer(adddress, msg -> {
      ClientResponse response = new ClientResponse();
      response.setStatusCode(HttpResponseStatus.OK);
      response.setBody(Buffer.buffer().appendString(addToBody));
      response.setHeaders(headers.keySet().stream().collect(MultiMapCollector.toMultimap(o -> o, headers::get)));
      msg.reply(new AdapterResponse().setResponse(response).setSignal(signal));
    });
  }

}