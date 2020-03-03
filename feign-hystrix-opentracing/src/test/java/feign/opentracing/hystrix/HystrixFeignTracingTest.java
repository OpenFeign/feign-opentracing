package feign.opentracing.hystrix;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;

import com.netflix.hystrix.strategy.HystrixPlugins;

import feign.Client;
import feign.Feign;
import feign.Retryer;
import feign.hystrix.HystrixFeign;
import feign.opentracing.FeignTracingTest;
import feign.opentracing.TracingClient;

/**
 * @author Pavol Loffay
 */
public class HystrixFeignTracingTest extends FeignTracingTest {

    @Override
    public void before() throws IOException {
        HystrixPlugins.reset();
        TracingConcurrencyStrategy.register(mockTracer);
        super.before();
    }

    @Override
    protected Feign getClient() {
        return feign  = HystrixFeign.builder()
                .client(tracingClient(new Client.Default(null, null)))
                .retryer(new Retryer.Default(100, SECONDS.toMillis(1), FeignTracingTest.NUMBER_OF_RETRIES))
                .build();
    }
}
