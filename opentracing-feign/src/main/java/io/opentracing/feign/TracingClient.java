package io.opentracing.feign;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import feign.Client;
import feign.Request;
import feign.Response;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.contrib.spanmanager.DefaultSpanManager;
import io.opentracing.contrib.spanmanager.SpanManager;
import io.opentracing.feign.internal.HttpHeadersInjectAdapter;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;

/**
 * OpenTracing Feign integration. This client wraps actual client implementation and creates tracing data for
 * outgoing requests.
 *
 * @author Pavol Loffay
 */
public class TracingClient implements Client {

    private Tracer tracer;
    private List<FeignSpanDecorator> spanDecorators;
    private SpanManager spanManager = DefaultSpanManager.getInstance();

    private Client delegate;

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
        Span span = null;
        try {
            Tracer.SpanBuilder spanBuilder = tracer.buildSpan(request.method())
                    .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);

            if (spanManager.current().getSpan() != null) {
                spanBuilder.asChildOf(spanManager.current().getSpan());
            }

            span = spanBuilder.start();

            for (FeignSpanDecorator spanDecorator: spanDecorators) {
                spanDecorator.onRequest(request, options, span);
            }

            request = inject(span.context(), request);
            Response response = delegate.execute(request, options);
            for (FeignSpanDecorator spanDecorator: spanDecorators) {
                spanDecorator.onResponse(response, options, span);
            }

            return response;
        } catch (Exception ex) {
            for (FeignSpanDecorator spanDecorator: spanDecorators) {
                spanDecorator.onError(ex, request, span);
            }
            throw ex;
        } finally {
            if (span != null) {
                span.finish();
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
