package com.example.hormigas.empresa.controller;

import com.example.hormigas.empresa.dto.EmpresaConAdminCreateDTO;
import com.example.hormigas.empresa.dto.EmpresaResponseDTO;
import com.example.hormigas.empresa.dto.EmpresaUpdateDTO;
import com.example.hormigas.empresa.service.EmpresaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresa")
public class EmpresaController {
    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @PostMapping("/create")
    public ResponseEntity<EmpresaResponseDTO> crear(@Valid @RequestBody EmpresaConAdminCreateDTO dto) {
        EmpresaResponseDTO empresa = empresaService.createEmpresa(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(empresa);
    }

    @DeleteMapping("/delete/{id}")
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
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        empresaService.activate(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PatchMapping("/{rfc}/activate")
    public ResponseEntity<EmpresaResponseDTO> activar(@PathVariable String rfc) {
        empresaService.activate(rfc);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/")
    public ResponseEntity<EmpresaResponseDTO> obtener() {
        return ResponseEntity.ok(empresaService.getEmpresaDetails());
    }

    @PatchMapping("/update")
    public ResponseEntity<EmpresaResponseDTO> actualizarEmpresa(@RequestBody EmpresaUpdateDTO dto) {
        return ResponseEntity.ok(empresaService.updateEmpresa(dto));
    }

    @GetMapping("/all")
    public ResponseEntity<List<EmpresaResponseDTO>> obtenerTodo() {
        return ResponseEntity.ok(empresaService.getAllEmpresas());
    }
}
