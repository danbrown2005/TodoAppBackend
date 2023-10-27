package uk.co.autotrader.springpractice.filter;

import io.micrometer.common.lang.NonNullApi;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import uk.co.autotrader.springpractice.service.AuthService;
import java.io.IOException;
import java.util.Set;

@Component
//@Order(2)
@NonNullApi
public class AuthFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private final Set<String> UNFILTERED_ENDPOINTS = Set.of("/user/register", "/user/login");

    public AuthFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        if (!UNFILTERED_ENDPOINTS.contains(wrappedRequest.getRequestURI()) &&
                !authService.verifyToken(wrappedRequest.getHeader("authorization"))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            wrappedRequest.setAttribute("username", authService.getUsernameFromToken(wrappedRequest.getHeader("authorization")));
            filterChain.doFilter(wrappedRequest, response);
        }
    }
}
