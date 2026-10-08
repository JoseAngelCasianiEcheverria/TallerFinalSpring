package com.tallerautos.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tallerautos.dto.request.ClienteRequest;
import com.tallerautos.dto.response.ClienteResponse;
import com.tallerautos.entity.Cliente;
import com.tallerautos.exception.ConHistorialException;
import com.tallerautos.exception.EmailDuplicadoException;
import com.tallerautos.exception.NotFoundException;
import com.tallerautos.mapper.ClienteMapper;
import com.tallerautos.repository.ClienteRepository;


@Service
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository repositorio;
    private final ClienteMapper mapper;

    public ClienteService(ClienteRepository repositorio, ClienteMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    
    public List<ClienteResponse> listar() {
        return repositorio.findAllByOrderByIdAsc().stream()
                .map(mapper::aResponse)
                .toList();
    }

    
    public ClienteResponse buscarPorId(Long id) {
        return mapper.aResponse(obtener(id));
    }

    
    @Transactional
    public ClienteResponse registrar(ClienteRequest request) {
        String correo = normalizarCorreo(request.getEmail());

        if (repositorio.existsByEmail(correo)) {
            throw new EmailDuplicadoException(correo);
        }

        Cliente cliente = new Cliente(
                request.getNombre().trim(),
                correo,
               (request.getTelefono() == null) ? null : request.getTelefono().trim(),
                (request.getDireccion() == null) ? null : request.getDireccion().trim());

        return mapper.aResponse(repositorio.save(cliente));
    }

    
    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = obtener(id);
        String correo = normalizarCorreo(request.getEmail());

        
        if (repositorio.existsByEmailAndIdNot(correo, id)) {
            throw new EmailDuplicadoException(correo);
        }

        cliente.setNombre(request.getNombre().trim());
        cliente.setEmail(correo);
        cliente.setTelefono(request.getTelefono() == null ? null : request.getTelefono().trim());
        cliente.setDireccion(request.getDireccion() == null ? null : request.getDireccion().trim());

        return mapper.aResponse(repositorio.save(cliente));
    }

    
    @Transactional
    public void eliminar(Long id) {
        Cliente cliente = obtener(id);

        if (repositorio.existeVentaDe(id)) {
            throw new ConHistorialException(
                    "el cliente " + cliente.getNombre(),
                    "al menos una venta");
        }

        repositorio.delete(cliente);
    }

    private Cliente obtener(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "No existe un cliente con el identificador " + id + "."));
    }

    
    private String normalizarCorreo(String correo) {
        return correo == null ? null : correo.trim().toLowerCase(Locale.ROOT);
    }
}
