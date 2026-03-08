package com.example.hormigas.empresa.exception;

public class EmpresaDuplicadaException extends RuntimeException {

    public EmpresaDuplicadaException (String mensaje) {
        super(mensaje);
    }
}
