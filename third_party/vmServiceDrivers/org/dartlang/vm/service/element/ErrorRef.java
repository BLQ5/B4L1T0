/*
 * Copyright (c) 2015, the Dart project authors.
 *
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dartlang.vm.service.element;

// This is a generated file.

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * {@link ErrorRef} is a reference to an {@link ErrorObj}.
 */
@SuppressWarnings({"WeakerAccess", "unused", "UnnecessaryInterfaceModifier"})
public class ErrorRef extends ObjRef {

  public ErrorRef(JsonObject json) {
    super(json);
  }

  /**
   * What kind of error is this?
   */
  public ErrorKind getKind() {
    JsonElement value = json.get("kind");
    try {
      return value == null ? ErrorKind.Unknown : ErrorKind.valueOf(value.getAsString());
    } catch (IllegalArgumentException e) {
      return ErrorKind.Unknown;
    }
  }

  /**
   * A description of the error.
   */
  public String getMessage() {
    return json.get("message").getAsString();
  }
}