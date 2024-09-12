# Extism Quarkus Greeting App

This is a demo for the [Quarkus Wasm Extension][quarkus-wasm-extension]. The Quarkus Wasm Extension is a demonstrative extension for Quarkus leveraging the [Chicory pure-Java WebAssembly runtime][chicory], and [Extism](https://extism.org/)'s bleeding edge [Chicory SDK][chicory-sdk].


[quarkus-wasm-extension]: https://github.com/evacchi/quarkus-wasm-extension

[![Quarkus Demo ](https://cdn.loom.com/sessions/thumbnails/40043d98406940739e5e2bf049aec0e8-b551743d1bab726c-full-play.gif)](https://www.loom.com/share/40043d98406940739e5e2bf049aec0e8?sid=7649f41a-3d80-41fe-bd5a-a9b8f5450e8f)

## Prepare for installation

This example relies on bleeding-edge version of [Chicory][chicory] and the [Extism Chicory SDK][chicory-sdk]

[chicory]: https://github.com/dylibso/chicory/
[chicory-sdk]: https://github.com/extism/chicory-sdk/


### Install a snapshot version of the Chicory Wasm runtime

The last snapshot at `main`, but in case it does not, at the time of writing, commit hash `ebf4ddf` works, so you can:

```
git clone https://github.com/dylibso/chicory
git checkout ebf4ddfc00e001416a67d3b8bedabf763ebed572
```

then install. You can skip all tests and codegen with:

```
cd chicory
mvn install -Dquickly
```

### Install the chicory-sdk linker branch

Clone from evacchi's fork and checkout the `linker` branch:

```
git clone https://github.com/evacchi/chicory-sdk/
git checkout linker
```

then install:

```
cd chicory-sdk
mvn install -DskipTests
```

### Install the Quarkus extension

Clone the Quarkus extension. This is where most logic is handled.

```
git clone https://github.com/evacchi/quarkus-wasm-extension
git checkout chicory-sdk
cd quarkus-wasm-extension
mvn install -DskipTests -DskipITs
```

Notice how the one bit of `native-image` config is defined in the [Quarkus processor](https://github.com/evacchi/quarkus-wasm-extension/blob/main/deployment/src/main/java/io/quarkiverse/quarkus/wasm/deployment/WasmProcessor.java#L18-L20):

```
    @BuildStep
    void registerWasmResources(BuildProducer<NativeImageResourceBuildItem> nativeResources) {
        nativeResources.produce(new NativeImageResourceBuildItem("extism-runtime.wasm"));
    }
```

which is equivalent to appending the directive `-H:IncludeResources=extism-runtime.wasm` to the `native-image` executable, or [using an appropriate config directive as described in the manual](https://www.graalvm.org/latest/reference-manual/native-image/dynamic-features/Resources/).


### Optional: Quarkus Wasm SDK

This repo contains the source code for the Wasm plug-ins. 
You can find the full source code of the examples and the SDK at https://github.com/evacchi/quarkus-wasm-sdk

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/greeting-app-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Hot reload of Wasm modules

The config file under `config/application.yaml` is automatically reloaded for changes. In dev mode, this is handled by Quarkus. In JVM or native mode, you can enable the file watcher with `-Dquarkus.wasm.file-watcher.enabled=true`

For instance:

```
$ target/greeting-app-1.0.0-SNAPSHOT-runner -Dquarkus.wasm.file-watcher.enabled=true
```

There are 3 example `wasm` plug-ins. Their source code can be found at https://github.com/evacchi/quarkus-wasm-sdk/.

- **hello-world**: Appends the **request** header `X-Wasm-Plugin: Hello World!`
- **basic-auth**: Looks for the `Authorization` header in the **request** and verifies the credentials. 
    This is a just a silly, hardcoded demo that expects the header to look like:

    ```
    Authorization: Basic YWRtaW46YWRtaW4=
    ```

    the base64-encoded string is `admin:admin`. If the validation passes, then the header `X-Authorized: admin` is appended to the **request**.

- **fortunes**: It appends to the **responses** a random fortune, e.g.:

    ```
    X-Fortune-Plugin: [cpu: can't dial helix.cpu: ken hasn't implemented datakit]
    ```

You can observe the contents of the request and response as they are sent out from the client using verbose mode in your client, e.g., with cURL:

```
❯ curl http://localhost:8080/wasm/ -vvv -H 'Authorization: Basic YWRtaW46YWRtaW4='
* Host localhost:8080 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
*   Trying [::1]:8080...
* Connected to localhost (::1) port 8080
> GET /wasm/ HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/8.7.1
> Accept: */*
> Authorization: Basic YWRtaW46YWRtaW4=
>
* Request completely sent off
< HTTP/1.1 200 OK
< content-length: 13
< Content-Type: text/plain;charset=UTF-8
< Content-Type: [text/plain;charset=UTF-8]
< X-Fortune-Plugin: [03:23:33       0.0017   USER        CLOSE CALLS -                              8]
<
* Connection #0 to host localhost left intact
Hello Dylibso%
```

Notice that if a plug-in modifies the **request** this will not be observable in the output, because the change will happen directly within the Quarkus application. You should see feedback in the Quarkus logs:

```
2024-09-12 09:48:57,215 INFO  [io.qua.qua.was.run.adm.FileSystemWatcher] (vert.x-eventloop-thread-6) File system watcher reloaded the config.
[...]
2024-09-12 09:48:57,465 INFO  [io.qua.qua.was.run.RequestFilter] (vert.x-eventloop-thread-6) Filter chain was reloaded: ImmutableFilterChainConfig{plugins=[Plugin[name=hello-world, type=filesystem], Plugin[name=basic-auth, type=filesystem], Plugin[name=fortunes, type=filesystem]]}
2024-09-12 09:48:57,465 INFO  [io.qua.qua.was.run.FilterChain] (vert.x-eventloop-thread-6) Invoking plugin hello-world
2024-09-12 09:48:57,471 INFO  [io.qua.qua.was.run.FilterChain] (vert.x-eventloop-thread-6) Invoking plugin basic-auth
2024-09-12 09:48:57,478 INFO  [io.qua.qua.was.run.FilterChain] (vert.x-eventloop-thread-6) Invoking plugin fortunes
2024-09-12 09:48:57,484 INFO  [io.git.eva.qua.was.WasmResource] (executor-thread-1) /Users/evacchi/Devel/java/native-image-demo/extism-quarkus-greeting-app
2024-09-12 09:48:57,484 INFO  [io.git.eva.qua.was.WasmResource] (executor-thread-1) X-Authorized: [admin]
2024-09-12 09:48:57,484 INFO  [io.git.eva.qua.was.WasmResource] (executor-thread-1) X-Wasm-Plugin: [Hello World!]
2024-09-12 09:48:57,484 INFO  [io.git.eva.qua.was.WasmResource] (executor-thread-1) User-Agent: [curl/8.7.1]
2024-09-12 09:48:57,484 INFO  [io.git.eva.qua.was.WasmResource] (executor-thread-1) Accept: [*/*]
2024-09-12 09:48:57,484 INFO  [io.git.eva.qua.was.WasmResource] (executor-thread-1) Host: [localhost:8080]
2024-09-12 09:48:57,484 INFO  [io.git.eva.qua.was.WasmResource] (executor-thread-1) Authorization: [Basic YWRtaW46YWRtaW4=]
2024-09-12 09:48:57,484 INFO  [io.qua.qua.was.run.FilterChain] (executor-thread-1) Invoking plugin hello-world
2024-09-12 09:48:57,486 INFO  [io.qua.qua.was.run.FilterChain] (executor-thread-1) Invoking plugin basic-auth
2024-09-12 09:48:57,488 INFO  [io.qua.qua.was.run.FilterChain] (executor-thread-1) Invoking plugin fortunes
```

You can update the config file `config/application.yaml` to see unload and reload plug-ins dynamically. Notice that the [FileSystemWatcher](https://github.com/evacchi/quarkus-wasm-extension/blob/main/runtime/src/main/java/io/quarkiverse/quarkus/wasm/runtime/admin/FileSystemWatcher.java) might take a little while to register the change. If you don't see any changes, just wait a little and send the request again.
