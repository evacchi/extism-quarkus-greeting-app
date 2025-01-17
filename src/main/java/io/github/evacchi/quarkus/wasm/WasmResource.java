/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.evacchi.quarkus.wasm;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.HttpHeaders;

import java.util.List;

@Path("/wasm")
@RequestScoped
public class WasmResource {

    @GET
    public String hello(HttpHeaders h) {
        for (var e : h.getRequestHeaders().entrySet()) {
            Log.infof("%s: %s", e.getKey(), e.getValue());
        }

        if (List.of("Hello World!").equals(h.getRequestHeaders().get("X-Wasm-Plugin"))) {
            return "Hello Dylibso";
        }
        return "No Wasm for you!";
    }
}
