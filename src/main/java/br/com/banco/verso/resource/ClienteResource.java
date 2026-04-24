package br.com.banco.verso.resource;

import br.com.banco.verso.dto.ClienteResponse;
import br.com.banco.verso.model.Cliente;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.stream.Collectors;

@Path("/clientes")
public class ClienteResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ClienteResponse> listarClientes() {
        return Cliente.<Cliente>listAll()
                .stream()
                .map(c -> new ClienteResponse(
                        c.id,
                        c.getNome(),
                        c.getEmail()
                ))
                .collect(Collectors.toList());
    }
}
