package br.com.banco.verso.resource;

import br.com.banco.verso.dto.ClienteRequest;
import br.com.banco.verso.dto.ClienteResponse;
import br.com.banco.verso.model.Cliente;
import br.com.banco.verso.model.UserRole;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.quarkus.elytron.security.common.BcryptUtil;

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

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarClientePorId(@PathParam("id") Long id) {
        Cliente cliente = Cliente.findById(id);

        if (cliente == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Cliente não encontrado")
                    .build();
        }

        ClienteResponse response = new ClienteResponse(
                cliente.id,
                cliente.getNome(),
                cliente.getEmail()
        );

        return Response.ok(response).build();
    }




    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response atualizarCliente(@PathParam("id") Long id, ClienteRequest request) {

        // Regra: CPF não pode ser informado no update
        if (request.getCpf() != null && !request.getCpf().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("O campo CPF não pode ser alterado")
                    .build();
        }

        Cliente cliente = Cliente.findById(id);

        if (cliente == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Cliente não encontrado")
                    .build();
        }

        // Atualiza os campos permitidos
        cliente.setNome(request.getNome());
        cliente.setEmail(request.getEmail());

        // Atualiza a senha com hash Bcrypt
        if (request.getSenha() != null && !request.getSenha().isBlank()) {
            String senhaHash = BcryptUtil.bcryptHash(request.getSenha());
            cliente.setSenha(senhaHash);
        }

        cliente.persist();

        ClienteResponse response = new ClienteResponse(
                cliente.id,
                cliente.getNome(),
                cliente.getEmail()
        );

        return Response.ok(response).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response criarCliente(ClienteRequest request) {

        // Validações básicas
        if (request.getNome() == null || request.getNome().isBlank() ||
                request.getEmail() == null || request.getEmail().isBlank() ||
                request.getCpf() == null || request.getCpf().isBlank() ||
                request.getSenha() == null || request.getSenha().isBlank()) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Todos os campos são obrigatórios: nome, email, cpf, senha")
                    .build();
        }

        // Verifica duplicidade de CPF
        if (Cliente.find("cpf", request.getCpf()).firstResult() != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Já existe um cliente cadastrado com este CPF")
                    .build();
        }

        // Verifica duplicidade de email
        if (Cliente.find("email", request.getEmail()).firstResult() != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Já existe um cliente cadastrado com este email")
                    .build();
        }

        // Cria novo cliente
        Cliente cliente = new Cliente();
        cliente.setNome(request.getNome());
        cliente.setCpf(request.getCpf());
        cliente.setEmail(request.getEmail());
        cliente.setRole(UserRole.CLIENTE); //Padrao

        // Hash da senha
        String senhaHash = BcryptUtil.bcryptHash(request.getSenha());
        cliente.setSenha(senhaHash);

        cliente.persist();

        ClienteResponse response = new ClienteResponse(
                cliente.id,
                cliente.getNome(),
                cliente.getEmail()
        );

        return Response.status(Response.Status.CREATED).entity(response).build();
    }




}
