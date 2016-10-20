/*
 * Knot.x - Mocked services for sample app
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
package com.cognifide.knotx.mocks.knot;

import org.apache.commons.lang3.tuple.Pair;

import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.core.file.AsyncFile;
import io.vertx.rxjava.core.file.FileSystem;
import rx.Observable;

enum KnotContextKeys {
  RESPONSE("clientResponse") {
    @Override
    Object defaultValue() {
      return new JsonObject().put("statusCode", 200);
    }
  },
  REQUEST("clientRequest") {
    @Override
    Object defaultValue() {
      return null;
    }
  },
  FRAGMENTS("fragments") {
    @Override
    Object defaultValue() {
      return new JsonArray();
    }
  },
  TRANSITION("transition") {
    @Override
    Object defaultValue() {
      return null;
    }

    @Override
    Observable<Object> mockValue(FileSystem fileSystem, String mockConfigValue) {
      return Observable.just(mockConfigValue);
    }
  };

  private final String key;

  KnotContextKeys(String key) {
    this.key = key;
  }

  String key() {
    return key;
  }

  Pair<String, Object> valueOrDefault(FileSystem fileSystem, JsonObject responseConfig) {
    Object value;
    if (responseConfig.containsKey(key)) {
      value = mockValue(fileSystem, responseConfig.getString(key));
    } else {
      value = defaultValue();
    }
    return Pair.of(key, value);
  }

  Observable<Object> mockValue(FileSystem fileSystem, String resourcePath) {
    return fileSystem.openObservable(resourcePath, new OpenOptions().setCreate(false).setWrite(false))
        .flatMap(this::processFile)
        .map(this::toJson);
  }

  abstract Object defaultValue();

  private Object toJson(Buffer buffer) {
    return buffer.toString().charAt(0) == '{' ? buffer.toJsonObject() : buffer.toJsonArray();
  }

  private Observable<Buffer> processFile(final AsyncFile asyncFile) {
    return Observable.just(Buffer.buffer())
        .mergeWith(asyncFile.toObservable())
        .reduce(Buffer::appendBuffer);
  }
}
