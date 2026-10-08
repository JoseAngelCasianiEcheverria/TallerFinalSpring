package com.tallerautos.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ClienteRequest {

    @NotBlank(message = "el nombre es obligatorio")
    @Size(max = 100, message = "el nombre no puede superar 100 caracteres")
    private String nombre;

    @NotBlank(message = "el correo es obligatorio")
    @Email(message = "el correo no tiene un formato valido")
    @Size(max = 120, message = "el correo no puede superar 120 caracteres")
    private String email;

    
    @Pattern(regexp = "^[0-9]{0,10}$",
             message = "el telefono solo admite numeros, hasta 10")
    private String telefono;

    @Size(max = 200, message = "la direccion no puede superar 200 caracteres")
    private String direccion;

    public ClienteRequest() {
    }

    public ClienteRequest(String nombre, String email, String telefono, String direccion) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
