package feign.opentracing.hystrix;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy;

import io.opentracing.ActiveSpan;
import io.opentracing.NoopActiveSpanSource;
import io.opentracing.Tracer;

/**
 * @author Pavol Loffay
 */
public class TracingConcurrencyStrategy extends HystrixConcurrencyStrategy {
    private static Logger log = Logger.getLogger(TracingConcurrencyStrategy.class.getName());

    private HystrixConcurrencyStrategy delegateStrategy;
    private Tracer tracer;

    public static TracingConcurrencyStrategy register(Tracer tracer) {
        return new TracingConcurrencyStrategy(tracer);
    }

    private TracingConcurrencyStrategy(Tracer tracer) {
        this.tracer = tracer;
        try {
            this.delegateStrategy = HystrixPlugins.getInstance().getConcurrencyStrategy();
            if (this.delegateStrategy instanceof TracingConcurrencyStrategy) {
                return;
            }

            HystrixCommandExecutionHook commandExecutionHook = HystrixPlugins.getInstance().getCommandExecutionHook();
            HystrixEventNotifier eventNotifier = HystrixPlugins.getInstance().getEventNotifier();
            HystrixMetricsPublisher metricsPublisher = HystrixPlugins.getInstance().getMetricsPublisher();
            HystrixPropertiesStrategy propertiesStrategy = HystrixPlugins.getInstance().getPropertiesStrategy();

            HystrixPlugins.reset();
            HystrixPlugins.getInstance().registerConcurrencyStrategy(this);
            HystrixPlugins.getInstance().registerCommandExecutionHook(commandExecutionHook);
            HystrixPlugins.getInstance().registerEventNotifier(eventNotifier);
            HystrixPlugins.getInstance().registerMetricsPublisher(metricsPublisher);
            HystrixPlugins.getInstance().registerPropertiesStrategy(propertiesStrategy);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed to register " + TracingConcurrencyStrategy.class +
                            ", to HystrixPlugins", ex);
        }
    }

    @Override
    public <T> Callable<T> wrapCallable(Callable<T> callable) {
        if (callable instanceof OpenTracingHystrixCallable) {
            return callable;
        }

        Callable<T> delegateCallable = this.delegateStrategy == null ? callable :
                this.delegateStrategy.wrapCallable(callable);

        if (delegateCallable instanceof OpenTracingHystrixCallable) {
            return delegateCallable;
        }

        return new OpenTracingHystrixCallable<>(delegateCallable);
    }

    private class OpenTracingHystrixCallable<S> implements Callable<S> {
        private Callable<S> delegateCallable;
        private ActiveSpan parentSpan;

        public OpenTracingHystrixCallable(Callable<S> delegate) {
            this.delegateCallable = delegate;
            parentSpan = tracer.activeSpan();
        }

        @Override
        public S call() throws Exception {
            ActiveSpan.Continuation continuation = parentSpan != null
                    ? parentSpan.capture() : NoopActiveSpanSource.NoopContinuation.INSTANCE;
            ActiveSpan activeHere = continuation.activate();
            try {
                return this.delegateCallable.call();
            } finally {
                activeHere.deactivate();
            }
        }
    }
}
