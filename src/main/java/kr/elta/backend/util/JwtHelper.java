package kr.elta.backend.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class JwtHelper {
    
    /**
     * 현재 인증된 사용자의 username을 반환
     * @return JWT의 subject (username)
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }
    
    /**
     * 현재 인증된 사용자의 username을 Long으로 변환하여 반환
     * @return username을 Long으로 변환한 값
     */
    public static Long getCurrentUserUuid() {
        String username = getCurrentUsername();
        return username != null ? Long.parseLong(username) : null;
    }
    
    /**
     * 현재 인증된 사용자의 권한(role)을 반환
     * @return 사용자의 권한 목록
     */
    public static Collection<? extends GrantedAuthority> getCurrentUserAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getAuthorities() : null;
    }
    
    /**
     * 현재 인증된 사용자가 특정 권한을 가지고 있는지 확인
     * @param role 확인할 권한 (예: "SELLER", "ADMIN")
     * @return 권한 보유 여부
     */
    public static boolean hasRole(String role) {
        Collection<? extends GrantedAuthority> authorities = getCurrentUserAuthorities();
        return authorities != null && 
               authorities.stream().anyMatch(auth -> auth.getAuthority().equals(role));
    }
    
    /**
     * 현재 사용자가 인증되어 있는지 확인
     * @return 인증 여부
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}