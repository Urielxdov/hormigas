package com.example.hormigas.shared.exception;

/** Conflicto de negocio (recurso duplicado, estado incompatible). Se responde con 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String mensaje) {
        super(mensaje);
    }
}
