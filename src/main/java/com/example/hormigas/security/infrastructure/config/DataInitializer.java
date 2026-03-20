package com.example.hormigas.security.infrastructure.config;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.empresa.repository.EmpresaRepository;
import com.example.hormigas.security.domain.Role;
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
        Map<String, Modulo> modulos = new HashMap<>();
        for (String nombreModulo : nombresModulos) {
            Modulo m = moduloRepository.findByNombre(nombreModulo)
                    .orElseGet(() -> {
                        Modulo mod = new Modulo();
                        mod.setNombre(nombreModulo);
                        return moduloRepository.save(mod);
                    });
            modulos.put(nombreModulo, m);
        }

        // Configuración de permisos por módulo
        Map<String, List<String>> permisosConfig = Map.of(
                "productos", List.of("producto.ver", "producto.crear", "producto.editar", "producto.eliminar"),
                "inventario", List.of("inventario.ver", "inventario.movimiento"),
                "movimientos", List.of("movimiento.ver", "movimiento.crear", "movimiento.editar", "movimiento.eliminar"),
                "usuarios", List.of("usuario.ver", "usuario.crear", "usuario.editar", "usuario.eliminar", "usuario.asignar-roles"),
                "reportes", List.of("reporte.ver", "reporte.generar"),
                "sucursales", List.of("sucursal.ver", "sucursal.crear", "sucursal.editar", "sucursal.eliminar")
        );

        // Crear permisos sin builder
        List<Permiso> permisos = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : permisosConfig.entrySet()) {
            Modulo modulo = modulos.get(entry.getKey());
            for (String codigo : entry.getValue()) {
                Permiso p = new Permiso();
                p.setCodigo(codigo);
                p.setModulo(modulo);
                p.setNombre(humanize(codigo));
                permisos.add(p);
            }
        }

        // Permiso superadmin
        Permiso superAdminTemp = new Permiso();
        superAdminTemp.setCodigo("system.superadmin");
        superAdminTemp.setModulo(modulos.get("usuarios"));
        superAdminTemp.setNombre("Permiso de superadministrador");
        permisos.add(superAdminTemp);

        // Guardar todos los permisos
        permisoRepository.saveAll(permisos);
        System.out.println("Permisos iniciales insertados correctamente");

        // Recuperar superAdmin fresco desde BD (con ID asignado por JPA)
        Permiso superAdmin = permisoRepository.findByCodigo("system.superadmin")
                .orElseThrow(() -> new RuntimeException("Permiso superadmin no encontrado"));

        // Crear empresa por defecto
        Empresa empresa = empresaRepository.findById(1L).orElseGet(() -> {
            Empresa e = new Empresa();
            e.setTelefono("5551234567");
            e.setNombre("Empresa por defecto S.A. de C.V.");
            e.setRfc("XAXX010101000");
            e.setDireccion("Av. Principal 123, Ciudad, Estado");
            e.setActivo(true);
            return empresaRepository.save(e);
        });

        // Crear rol superadmin
        Rol superRol = rolRepository.findByNombre("Super rol").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombre("Super rol");
            r.setDescripcion("Este rol es para el super usuario");
            r.setActivo(true);
            r.setPermisos(Set.of(superAdmin));
            return rolRepository.save(r);
        });

        // Crear usuario superadmin
        createUsuarioIfNotExists(email, nombre, password, empresa, superRol);

        // Crear rol admin empresa
        Rol adminRol = rolRepository.findByNombre("Admin Empresa").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombre("Admin Empresa");
            r.setDescripcion("Rol para administrar la empresa por defecto");
            r.setActivo(true);
            r.setPermisos(Set.of(superAdmin));
            return rolRepository.save(r);
        });

        // Crear usuario admin empresa
        createUsuarioIfNotExists(email, nombre, password, empresa, adminRol);
    }

    private void createUsuarioIfNotExists(String correo, String nombre, String pass, Empresa empresa, Rol rol) {
        if (usuarioRepository.findByCorreo(correo).isEmpty()) {
            Usuario u = new Usuario();
            u.setCorreo(correo);
            u.setNombre(nombre);
            u.setPasswordHash(passwordEncoder.encode(pass));
            u.setActivo(true);
            u.setEmpresa(empresa);
            u.addRol(Role.SUPER_ADMIN); // si quieres agregar otros roles, puedes ajustar
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