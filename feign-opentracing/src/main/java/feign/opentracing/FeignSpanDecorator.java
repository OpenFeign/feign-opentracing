package feign.opentracing;

import java.util.HashMap;
import java.util.Map;

import feign.Request;
import feign.Response;
import io.opentracing.Span;
import io.opentracing.tag.Tags;

/**
 * Decorate span by adding tags/logs or change operation name.
 *
 * <p>Do not finish span or throw any exceptions!
 *
 * @author Pavol Loffay
 */
public interface FeignSpanDecorator {

    /**
     * Decorate span before {@link feign.Client#execute(Request, Request.Options)} is called on the delegating client.
     *
     * @param request request
     * @param options request options
     * @param span client span
     */
    void onRequest(Request request, Request.Options options, Span span);

    /**
     * Decorate span after {@link feign.Client#execute(Request, Request.Options)} is called on the delegating client.
     *
     * @param response response
     * @param options request options
     * @param span client span
     */
    void onResponse(Response response, Request.Options options, Span span);

    /**
     * Decorate span if exception is thrown during {@link feign.Client#execute(Request, Request.Options)}.
     *
     * @param exception exception
     * @param request request
     * @param span client span
     */
    void onError(Exception exception, Request request, Span span);


    /**
     * This decorator adds set of standard tags to the span.
     */
    class StandardTags implements FeignSpanDecorator {

        @Override
        public void onRequest(Request request, Request.Options options, Span span) {
            Tags.COMPONENT.set(span, "feign");
            Tags.HTTP_URL.set(span, request.url());
            Tags.HTTP_METHOD.set(span, request.method());
        }

        @Override
        public void onResponse(Response response, Request.Options options, Span span) {
            Tags.HTTP_STATUS.set(span, response.status());
        }

        @Override
        public void onError(Exception exception, Request request, Span span) {
            Tags.ERROR.set(span, Boolean.TRUE);
            span.log(errorLogs(exception));
        }


        public static Map<String, Object> errorLogs(Exception ex) {
            Map<String, Object> errorLogs = new HashMap<>(2);
            errorLogs.put("event", Tags.ERROR.getKey());
            errorLogs.put("error.object", ex);

            return errorLogs;
        }
    }
}
