package com.example.hormigas.inventario.service;

import com.example.hormigas.inventario.dto.InventarioResponseDTO;
import com.example.hormigas.inventario.entity.Inventario;
import com.example.hormigas.inventario.mapper.InventarioMapper;
import com.example.hormigas.inventario.repository.InventarioRepository;
import com.example.hormigas.sucursal.repository.SucursalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final SucursalRepository sucursalRepository;

    public InventarioService (InventarioRepository inventarioRepository,
                              SucursalRepository sucursalRepository
    ) {
        this.inventarioRepository = inventarioRepository;
        this.sucursalRepository = sucursalRepository;
    }

    // Ver inventario por sucursal
    public List<InventarioResponseDTO> obtenerInventarioPorSucursal (Long sucursalId) {
        if (!sucursalRepository.existsById(sucursalId)) {
            throw new EntityNotFoundException("Sucursal no encontrada");
        }

        return inventarioRepository.findBySucursalId(sucursalId)
                .stream()
                .map(InventarioMapper::toResponseInventario)
                .toList();
    }
    // Actualizar inventario (ajuste)
    public InventarioResponseDTO actualizarInventario(Long idSucursal, Long idInventario, int nuevoStock) {

        if (nuevoStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }

        Inventario inventario = inventarioRepository.findById(idInventario)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado"));

        if (!inventario.getSucursal().getId().equals(idSucursal)) {
            throw new EntityNotFoundException("Inventario no encontrado en esta sucursal");
        }

        inventario.setStockActual(nuevoStock);

        return InventarioMapper.toResponseInventario(
                inventarioRepository.save(inventario)
        );
    }

    // Ver historial de inventario (Historial de movmientos)
}
