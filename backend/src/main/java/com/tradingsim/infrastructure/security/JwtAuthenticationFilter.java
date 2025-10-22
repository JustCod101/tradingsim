package com.tradingsim.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT认证过滤器
 * 从请求头中提取JWT Token并验证，设置Spring Security上下文
 * 
 * @author TradingSim Team
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtTokenService jwtTokenService;
    
    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, 
            HttpServletResponse response, 
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        try {
            String token = extractTokenFromRequest(request);
            
            if (token != null && jwtTokenService.validateAccessToken(token)) {
                String userId = jwtTokenService.getUserIdFromToken(token);
                String username = jwtTokenService.getUsernameFromToken(token);
                String role = jwtTokenService.getRoleFromToken(token);
                
                if (userId != null && username != null) {
                    // 创建认证对象
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + (role != null ? role.toUpperCase() : "USER"))
                    );
                    
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 设置到Security上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    logger.debug("Set authentication for user: {} with role: {}", username, role);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 从请求中提取JWT Token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
    
    /**
     * 判断是否应该跳过过滤器
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // 跳过认证端点
        if (path.startsWith("/api/auth/login") || 
            path.startsWith("/api/auth/register") || 
            path.startsWith("/api/auth/refresh") ||
            path.startsWith("/api/auth/validate")) {
            return true;
        }
        
        // 跳过公开的市场数据端点
        if (path.startsWith("/api/market/") || path.startsWith("/api/market-data/")) {
            return true;
        }
        
        // 跳过WebSocket端点
        if (path.startsWith("/ws/") || path.startsWith("/websocket/")) {
            return true;
        }
        
        // 跳过健康检查和监控端点
        if (path.startsWith("/actuator/") || path.startsWith("/health/")) {
            return true;
        }
        
        // 跳过API文档端点
        if (path.equals("/doc.html") || 
            path.startsWith("/webjars/") || 
            path.startsWith("/swagger-resources/") || 
            path.startsWith("/v3/api-docs/") || 
            path.startsWith("/swagger-ui/") || 
            path.equals("/swagger-ui.html")) {
            return true;
        }
        
        // 跳过静态资源
        if (path.startsWith("/static/") || 
            path.startsWith("/public/") || 
            path.equals("/favicon.ico")) {
            return true;
        }
        
        return false;
    }
}