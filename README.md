[![Build Status][ci-img]][ci] [![Released Version][maven-img]][maven]

# OpenTracing Feign Client Instrumentation
OpenTracing instrumentation for Feign.

## Configuration & Usage
```java
Feign feign = Feign.builder()
    .client(new TracingClient(feignCompatibleClient, tracer,
        Arrays.asList(new FeignSpanDecorator.StandardTags())))
    .build();

```

## Development
```shell
./mvnw clean install
```

## Release
Follow instructions in [RELEASE](RELEASE.md)

   [ci-img]: https://travis-ci.org/opentracing-contrib/java-feign.svg?branch=master
   [ci]: https://travis-ci.org/opentracing-contrib/java-feign
   [maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/opentracing-feign.svg?maxAge=2592000
   [maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-feign
