package us.martink.lunar.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestContextHolder {
    private static final ThreadLocal<RequestContext> REQUEST_CONTEXT = new ThreadLocal<>();

    public static RequestContext getContext() {
        RequestContext context = REQUEST_CONTEXT.get();

        if (context == null) {
            context = new RequestContext();
            REQUEST_CONTEXT.set(context);

        }
        return REQUEST_CONTEXT.get();
    }

    public static void setContext(RequestContext context) {
        REQUEST_CONTEXT.set(context);
    }

}