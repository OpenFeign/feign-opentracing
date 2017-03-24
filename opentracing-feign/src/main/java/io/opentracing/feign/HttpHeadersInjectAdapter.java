package io.opentracing.feign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.opentracing.propagation.TextMap;

/**
 * Inject adapter for HTTP headers see {@link io.opentracing.Tracer#inject}.
 *
 * @author Pavol Loffay
 */
class HttpHeadersInjectAdapter implements TextMap {

    private Map<String, Collection<String>> headers;

    public HttpHeadersInjectAdapter(Map<String, Collection<String>> headers) {
        if (headers == null) {
            headers = new HashMap<>();
        }

        this.headers = headers;
    }

    @Override
    public void put(String key, String value) {
        Collection<String> values = headers.get(key);
        if (values == null) {
            values = new ArrayList<>(1);
            headers.put(key, values);
        }

        values.add(value);
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException("This class should be used only with tracer#inject()");
    }
}
