package com.example.hormigas.empresa.controller;

import com.example.hormigas.empresa.dto.EmpresaCreateDTO;
import com.example.hormigas.empresa.dto.EmpresaResponseDTO;
import com.example.hormigas.empresa.dto.EmpresaUpdateDTO;
import com.example.hormigas.empresa.exception.EmpresaDuplicadaException;
import com.example.hormigas.empresa.service.EmpresaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/empresa")
public class EmpresaController {
    private final EmpresaService empresaService;

    public EmpresaController (EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @PostMapping("/create")
    public ResponseEntity<EmpresaResponseDTO> crear (
            @RequestBody EmpresaCreateDTO dto
            ) {
        EmpresaResponseDTO empresa = empresaService.createEmpresa(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(empresa);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        empresaService.deleteEmpresa(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaResponseDTO> obtener(@PathVariable Long id) {
        EmpresaResponseDTO empresa = empresaService.getEmpresaDetails(id);

        return ResponseEntity.ok(empresa);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmpresaResponseDTO> actualizarEmpresa(
            @PathVariable Long id,
            @RequestBody EmpresaUpdateDTO dto
            ) {
        EmpresaResponseDTO empresa = empresaService.updateEmpresa(id, dto);

        return ResponseEntity.ok(empresa);
    }

    @ExceptionHandler(EmpresaDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> handleEmpresaDuplicada(EmpresaDuplicadaException ex) {
        Map<String, Object> error = Map.of(
                "timestamp", LocalDateTime.now(),
                "Error", "Empresa duplicada",
                "message", ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }
}
