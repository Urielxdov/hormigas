package com.example.hormigas.empresa.controller;

import com.example.hormigas.empresa.dto.EmpresaConAdminCreateDTO;
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
            @RequestBody EmpresaConAdminCreateDTO dto
            ) {
        EmpresaResponseDTO empresa = empresaService.createEmpresa(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(empresa);
    }

    @DeleteMapping("/delete/{id}")
    // Exclusivo de super admin
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        empresaService.deleteEmpresa(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> eliminar() {
        empresaService.deleteEmpresa();

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    // Exclusivo de super admin
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        empresaService.activate(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PatchMapping("/{rfc}/activate")
    // Exclusivo de super admin
    public ResponseEntity<EmpresaResponseDTO> activar(@PathVariable String rfc) {
        empresaService.activate(rfc);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/")
    public ResponseEntity<EmpresaResponseDTO> obtener() {
        EmpresaResponseDTO empresa = empresaService.getEmpresaDetails();

        return ResponseEntity.ok(empresa);
    }

    @PatchMapping("/update")
    public ResponseEntity<EmpresaResponseDTO> actualizarEmpresa(
            @RequestBody EmpresaUpdateDTO dto
            ) {
        EmpresaResponseDTO empresa = empresaService.updateEmpresa(dto);

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
