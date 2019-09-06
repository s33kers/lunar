package us.martink.lunar.context;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class RequestContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        populateContext(httpServletRequest);
        filterChain.doFilter(httpServletRequest, servletResponse);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // noop
    }

    @Override
    public void destroy() {
        // noop
    }

    private static void populateContext(HttpServletRequest httpServletRequest) {
        RequestContextHolder.getContext().setAuthorization(httpServletRequest.getHeader("Authorization"));
    }
}