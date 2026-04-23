package br.com.banco.verso.service;

import br.com.banco.verso.model.Cliente;
import br.com.banco.verso.model.LoggedUser;
import br.com.banco.verso.resource.auth.TokenResponse;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Duration;

@ApplicationScoped
public class AuthService implements CurrentUserService {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @Inject
    JsonWebToken jwt;

    private Argon2 argon2;

    @PostConstruct
    void init() {
        argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    }

    @Override
    public LoggedUser getLoggedUser() {
        if (jwt.getName() == null) {
            throw new NotAuthorizedException("Nenhum usuario autenticado na requisicao atual");
        }

        return new LoggedUser(
                getUserId(),
                jwt.getName(),
                jwt.getClaim("cpf"),
                getRole()
        );
    }

    private Long getUserId(){
        return Long.parseLong(jwt.getClaim("userId").toString());
    }

    private String getRole(){
        return jwt.getGroups()
                .stream()
                .findFirst()
                .orElse("CLIENTE");
    }

    public TokenResponse login(String cpf, String password) {
        Cliente cliente = Cliente.find("cpf", cpf).firstResult();
        validatePassword(cliente, password);
        String token = generateToken(cliente);
        return new TokenResponse(
                token,
                cliente.getCpf(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getRole().name()
        );
    }

    private void validatePassword(Cliente cliente, String password) {
        boolean approve = cliente != null
                && BcryptUtil.matches(password, cliente.getSenha());

        if (!approve) {
            throw new NotAuthorizedException("Credenciais invalidas");
        }
    }

    private String generateToken(Cliente cliente) {
        return Jwt.issuer(issuer)
                .upn(cliente.getCpf())
                .groups(cliente.getRole().name())
                .claim("userId", cliente.id)
                .claim("nome", cliente.getNome())
                .expiresIn(Duration.ofMinutes(30))
                .sign();
    }
}
