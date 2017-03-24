package io.opentracing.contrib.feign.hystrix;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;

import com.netflix.hystrix.strategy.HystrixPlugins;

import feign.Client;
import feign.Feign;
import feign.Retryer;
import feign.hystrix.HystrixFeign;
import io.opentracing.feign.AbstractFeignTracingTest;
import io.opentracing.feign.TracingClient;

/**
 * @author Pavol Loffay
 */
public class HystrixFeignTracingTest extends AbstractFeignTracingTest {

    @Override
    public void before() throws IOException {
        HystrixPlugins.reset();
        TracingConcurrencyStrategy.register();
        super.before();
    }

    @Override
    protected Feign getClient() {
        return feign  = HystrixFeign.builder()
                .client(new TracingClient(new Client.Default(null, null), mockTracer))
                .retryer(new Retryer.Default(100, SECONDS.toMillis(1), AbstractFeignTracingTest.NUMBER_OF_RETRIES))
                .build();
    }
}
