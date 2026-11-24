package com.example.hormigas.producto.controller;

import com.example.hormigas.producto.dto.CategoriaResponseDTO;
import com.example.hormigas.producto.dto.NuevaCategoriaDTO;
import com.example.hormigas.producto.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categoria")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping("/")
    public List<CategoriaResponseDTO> listar(@RequestParam(defaultValue = "false") boolean todas) {
        return categoriaService.listar(todas);
    }

    @PostMapping("/nueva")
    public CategoriaResponseDTO crear(@Valid @RequestBody NuevaCategoriaDTO dto) {
        return categoriaService.crear(dto);
    }

    @PutMapping("/{id}")
    public CategoriaResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody NuevaCategoriaDTO dto) {
        return categoriaService.actualizar(id, dto);
    }

    @PatchMapping("/{id}/toggle")
    public CategoriaResponseDTO toggleActivo(@PathVariable Long id) {
        return categoriaService.toggleActivo(id);
    }
}
