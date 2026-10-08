package com.tallerautos.mapper;

import org.springframework.stereotype.Component;

import com.tallerautos.dto.response.ClienteResponse;
import com.tallerautos.entity.Cliente;


@Component
public class ClienteMapper {

    public ClienteResponse aResponse(Cliente cliente) {
        if (cliente == null) {
            return null;
        }

        return new ClienteResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getDireccion(),
                cliente.getFechaRegistro());
    }
}
