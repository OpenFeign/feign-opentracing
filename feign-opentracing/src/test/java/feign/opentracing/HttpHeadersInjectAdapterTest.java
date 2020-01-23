package feign.opentracing;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.*;

public class HttpHeadersInjectAdapterTest {

    @Test
    public void putNewTraceHeaderValueInsideList() {
        Map<String, Collection<String>> headers = new HashMap<>();
        headers.put("x-header", Arrays.asList("123:123:123:1"));
        HttpHeadersInjectAdapter adapter = new HttpHeadersInjectAdapter(headers);

        adapter.put("x-header", "123:456:456:1");

        assertEquals(headers.get("x-header"), Arrays.asList("123:123:123:1", "123:456:456:1"));
    }

    @Test
    public void putNewTraceHeaderValueInsideUnmodifiableList() {
        Map<String, Collection<String>> headers = new HashMap<>();
        headers.put("x-header", Collections.unmodifiableList(Arrays.asList("123:123:123:1")));
        HttpHeadersInjectAdapter adapter = new HttpHeadersInjectAdapter(headers);

        adapter.put("x-header", "123:456:456:1");

        assertEquals(headers.get("x-header"), Arrays.asList("123:123:123:1", "123:456:456:1"));
    }

    @Test
    public void putNewTraceHeaderValueInsideUnmodifiableSet() {
        Map<String, Collection<String>> headers = new HashMap<>();
        headers.put("x-header", Collections.unmodifiableSet(new HashSet<>(Arrays.asList("123:123:123:1"))));
        HttpHeadersInjectAdapter adapter = new HttpHeadersInjectAdapter(headers);

        adapter.put("x-header", "123:456:456:1");

        assertEquals(headers.get("x-header"), new HashSet<>(Arrays.asList("123:123:123:1", "123:456:456:1")));
    }
}