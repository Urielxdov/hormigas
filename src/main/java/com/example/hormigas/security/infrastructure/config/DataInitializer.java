package com.example.hormigas.security.infrastructure.config;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.empresa.repository.EmpresaRepository;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.domain.permiso.Modulo;
import com.example.hormigas.security.domain.permiso.Permiso;
import com.example.hormigas.security.domain.Rol;
import com.example.hormigas.security.domain.repository.ModuloRepository;
import com.example.hormigas.security.domain.repository.PermisoRepository;
import com.example.hormigas.security.domain.repository.RolRepository;
import com.example.hormigas.security.domain.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements CommandLineRunner {

    @Value("${app.bootstrap.admin.email}")
    private String email;

    @Value("${app.bootstrap.admin.password}")
    private String password;

    @Value("${app.bootstrap.admin.nombre}")
    private String nombre;

    private final PasswordEncoder passwordEncoder;
    private final PermisoRepository permisoRepository;
    private final ModuloRepository moduloRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public DataInitializer(PermisoRepository permisoRepository,
                           ModuloRepository moduloRepository,
                           EmpresaRepository empresaRepository,
                           UsuarioRepository usuarioRepository,
                           RolRepository rolRepository,
                           PasswordEncoder passwordEncoder) {
        this.permisoRepository = permisoRepository;
        this.moduloRepository = moduloRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (permisoRepository.count() > 0) return;

        // Crear y guardar módulos
        List<String> nombresModulos = List.of("productos", "inventario", "movimientos", "usuarios", "reportes", "sucursales");
        Map<String, Modulo> modulos = nombresModulos.stream()
                .collect(Collectors.toMap(
                        nombre -> nombre,
                        nombre -> moduloRepository.findByNombre(nombre)
                                .orElseGet(() -> moduloRepository.save(Modulo.builder().nombre(nombre).build()))
                ));

        // Configuración de permisos por módulo
        Map<String, List<String>> permisosConfig = Map.of(
                "productos", List.of("producto.ver", "producto.crear", "producto.editar", "producto.eliminar"),
                "inventario", List.of("inventario.ver", "inventario.movimiento"),
                "movimientos", List.of("movimiento.ver", "movimiento.crear", "movimiento.editar", "movimiento.eliminar"),
                "usuarios", List.of("usuario.ver", "usuario.crear", "usuario.editar", "usuario.eliminar", "usuario.asignar-roles"),
                "reportes", List.of("reporte.ver", "reporte.generar"),
                "sucursales", List.of("sucursal.ver", "sucursal.crear", "sucursal.editar", "sucursal.eliminar")
        );

        // Crear permisos con builder, usando módulos persistidos
        List<Permiso> permisos = permisosConfig.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(codigo -> Permiso.builder()
                                .codigo(codigo)
                                .modulo(modulos.get(entry.getKey()))
                                .nombre(humanize(codigo))
                                .build()))
                .collect(Collectors.toList());

        // Permiso superadmin (agregado a la lista antes del saveAll)
        Permiso superAdminTemp = Permiso.builder()
                .codigo("system.superadmin")
                .modulo(modulos.get("usuarios"))
                .nombre("Permiso de superadministrador")
                .build();
        permisos.add(superAdminTemp);

        // Guardar todos los permisos
        permisoRepository.saveAll(permisos);
        System.out.println("Permisos iniciales insertados correctamente");

        // Recuperar superAdmin fresco desde BD (con ID asignado por JPA)
        Permiso superAdmin = permisoRepository.findByCodigo("system.superadmin")
                .orElseThrow(() -> new RuntimeException("Permiso superadmin no encontrado"));

        // Crear empresa por defecto
        Empresa empresa = empresaRepository.findById(1L).orElseGet(() ->
                empresaRepository.save(new Empresa("5551234567", "Empresa por defecto S.A. de C.V.",
                        "XAXX010101000", "Av. Principal 123, Ciudad, Estado", true))
        );

        // Crear rol superadmin
        Rol superRol = rolRepository.findByNombre("Super rol").orElseGet(() ->
                rolRepository.save(new Rol("Super rol", "Este rol es para el super usuario", true, Set.of(superAdmin)))
        );

        // Crear usuario superadmin
        createUsuarioIfNotExists(email, nombre, password, empresa, superRol);

        // Crear rol admin empresa
        Rol adminRol = rolRepository.findByNombre("Admin Empresa").orElseGet(() ->
                rolRepository.save(new Rol("Admin Empresa", "Rol para administrar la empresa por defecto", true, Set.of(superAdmin)))
        );

        // Crear usuario admin empresa
        createUsuarioIfNotExists(email, nombre, password, empresa, superRol);
    }

    private void createUsuarioIfNotExists(String correo, String nombre, String pass, Empresa empresa, Rol rol) {
        if (usuarioRepository.findByCorreo(correo).isEmpty()) {
            Usuario u = new Usuario();
            u.setCorreo(correo);
            u.setNombre(nombre);
            u.setPasswordHash(passwordEncoder.encode(pass));
            u.setActivo(true);
            u.setEmpresa(empresa);
            u.addRol(rol);
            usuarioRepository.save(u);
            System.out.println("Usuario creado: " + correo);
        }
    }

    private String humanize(String codigo) {
        return Arrays.stream(codigo.split("\\."))
                .map(w -> w.substring(0, 1).toUpperCase() + w.substring(1))
                .collect(Collectors.joining(" "));
    }
}