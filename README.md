[![Build Status][ci-img]][ci] [![Released Version][maven-img]][maven]

# OpenTracing Feign Instrumentation
OpenTracing instrumentation for Feign client. This instrumentation creates client span for each outgoing request.
Repository currently uses [SpanManager](https://github.com/opentracing-contrib/java-spanmanager) to link 
with a parent span.

## Configuration & Usage

### Feign
```java
Feign feign = Feign.builder()
    .client(new TracingClient(feignCompatibleClient, tracer))
    .build();

```

### HystrixFeign
```java
TracingConcurrencyStrategy.register();
```
and create feign client like it is described above.

## Development
```shell
./mvnw clean install
```

## Release
Follow instructions in [RELEASE](RELEASE.md)

   [ci-img]: https://travis-ci.org/OpenFeign/feign-opentracing.svg?branch=master
   [ci]: https://travis-ci.org/OpenFeign/feign-opentracing
   [maven-img]: https://img.shields.io/maven-central/v/io.github.feign.opentracing/feign-opentracing.svg?maxAge=2592000
   [maven]: http://search.maven.org/#search%7Cga%7C1%7Cfeign-opentracing
