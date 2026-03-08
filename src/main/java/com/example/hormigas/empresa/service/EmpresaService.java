package com.example.hormigas.empresa.service;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.empresa.repository.EmpresaRepository;
import org.springframework.stereotype.Service;

@Service
public class EmpresaService {
    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }
    // Crear empresa
    public Empresa createEmpresa(Empresa empressa) {

        return empresaRepository.save(empressa);
    }

    // Actualizar empresa

    // Eliminar empresa

    // Ver detalle de emrpesa
}
