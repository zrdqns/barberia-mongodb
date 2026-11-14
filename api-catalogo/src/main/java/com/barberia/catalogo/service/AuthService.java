package com.barberia.catalogo.service;

import com.barberia.catalogo.dto.LoginDTORequest;
import com.barberia.catalogo.dto.LoginDTOResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserDetailsService usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    // Cuánto dura un token (jwt.duracion en application.properties)
    @Value("${jwt.duracion}")
    private Duration duracion;

    // Verifica usuario y contraseña y, si coinciden, firma un JWT con sus roles.
    public LoginDTOResponse iniciarSesion(LoginDTORequest request) {
        UserDetails usuario;
        try {
            usuario = usuarios.loadUserByUsername(request.getUsername().trim());
        } catch (UsernameNotFoundException e) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos.");
        }
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos.");
        }

        // ROLE_ADMIN -> ADMIN
        List<String> roles = usuario.getAuthorities().stream()
                .map(rol -> rol.getAuthority().replaceFirst("^ROLE_", ""))
                .toList();

        Instant ahora = Instant.now();
        Instant expira = ahora.plus(duracion);
        JwtClaimsSet datos = JwtClaimsSet.builder()
                .issuer("barberia")
                .subject(usuario.getUsername())
                .issuedAt(ahora)
                .expiresAt(expira)
                .claim("roles", roles)
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), datos))
                .getTokenValue();

        return new LoginDTOResponse(token, "Bearer", usuario.getUsername(), roles.get(0), expira);
    }
}
