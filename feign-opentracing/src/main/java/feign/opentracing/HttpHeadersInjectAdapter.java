package feign.opentracing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            throw new NullPointerException("Headers should not be null!");
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

        try {
            values.add(value);
        } catch (UnsupportedOperationException ex) {
            if (values instanceof List) {
                // Handle unmodifiable Lists
                List<String> list = new ArrayList<>(values);
                list.add(value);
                headers.put(key, list);
            } else if (values instanceof Set) {
                // Handle unmodifiable Sets
                Set<String> set = new HashSet<>(values);
                set.add(value);
                headers.put(key, set);
            } else {
                throw ex;
            }
        }
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException("This class should be used only with tracer#inject()");
    }
}
