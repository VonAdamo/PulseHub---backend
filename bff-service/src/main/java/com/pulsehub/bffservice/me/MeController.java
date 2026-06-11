package com.pulsehub.bffservice.me;

import com.pulsehub.bffservice.security.JwtClaims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final MeService meService;

    public MeController(MeService meService) {
        this.meService = meService;
    }

    @GetMapping
    public MeResponse getMe(HttpServletRequest request) {
        JwtClaims claims = (JwtClaims) request.getAttribute("jwtClaims");
        return meService.getMe(claims);
    }
}
