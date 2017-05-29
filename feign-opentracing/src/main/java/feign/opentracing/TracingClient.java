package feign.opentracing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import feign.Client;
import feign.Request;
import feign.Response;
import io.opentracing.ActiveSpan;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;

/**
 * OpenTracing Feign integration. This client wraps actual client implementation and creates tracing data for
 * outgoing requests.
 *
 * @author Pavol Loffay
 */
public class TracingClient implements Client {
    private static final Logger log = Logger.getLogger(TracingClient.class.getName());

    private Tracer tracer;
    private List<FeignSpanDecorator> spanDecorators;

    private Client delegate;

    /**
     * @param delegate delegating client
     * @param tracer tracer
     */
    public TracingClient(Client delegate, Tracer tracer) {
        this(delegate, tracer, Collections.<FeignSpanDecorator>singletonList(new FeignSpanDecorator.StandardTags()));
    }

    /**
     * @param delegate delegating client
     * @param tracer tracer
     * @param spanDecorators span decorators
     */
    public TracingClient(Client delegate, Tracer tracer, List<FeignSpanDecorator> spanDecorators) {
        this.delegate = delegate;
        this.tracer = tracer;
        this.spanDecorators = new ArrayList<>(spanDecorators);
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        ActiveSpan span = null;
        try {
            span = tracer.buildSpan(request.method())
                    .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                    .startActive();

            for (FeignSpanDecorator spanDecorator: spanDecorators) {
                try {
                    spanDecorator.onRequest(request, options, span);
                } catch (Exception ex) {
                    log.log(Level.SEVERE, "Exception during decorating span", ex);
                }
            }

            request = inject(span.context(), request);
            Response response = delegate.execute(request, options);
            for (FeignSpanDecorator spanDecorator: spanDecorators) {
                try {
                    spanDecorator.onResponse(response, options, span);
                } catch (Exception ex) {
                    log.log(Level.SEVERE, "Exception during decorating span", ex);
                }
            }

            return response;
        } catch (Exception ex) {
            for (FeignSpanDecorator spanDecorator: spanDecorators) {
                try {
                    spanDecorator.onError(ex, request, span);
                } catch (Exception exDecorator) {
                    log.log(Level.SEVERE, "Exception during decorating span", exDecorator);
                }
            }

            throw ex;
        } finally {
            if (span != null) {
                span.deactivate();
            }
        }
    }

    private Request inject(SpanContext spanContext, Request request) {
        Map<String, Collection<String>> headersWithTracingContext = new HashMap<>(request.headers());
        tracer.inject(spanContext, Format.Builtin.HTTP_HEADERS, new HttpHeadersInjectAdapter(headersWithTracingContext));
        return request.create(request.method(), request.url(), headersWithTracingContext,request.body(),
                request.charset());
    }
}
