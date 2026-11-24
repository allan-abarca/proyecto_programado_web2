package teccr.clinica.service;

import org.springframework.stereotype.Service;
import teccr.clinica.data.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SERVICIO TEMPORAL EN MEMORIA.
 * Sirve para que las vistas funcionen sin base de datos. La firma de los métodos es el
 * "contrato" con la capa de reglas de negocio / persistencia: se puede reemplazar su
 * implementación por repositorios de Spring Data sin tocar controladores ni vistas.
 */
@Service
public class UsuarioService {

    private final List<Usuario> usuarios = new ArrayList<>();
    private final AtomicLong secuencia = new AtomicLong(1);

    public UsuarioService() {
        // Datos de ejemplo
        registrar("admin", "admin123", "Recepción Clínica", "admin@clinica.com", Usuario.Tipo.ADMIN);
        registrar("paciente1", "1234", "Ana Rojas", "ana@correo.com", Usuario.Tipo.PACIENTE);
        registrar("paciente2", "1234", "Luis Mora", "luis@correo.com", Usuario.Tipo.PACIENTE);
    }

    public Optional<Usuario> autenticar(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        return usuarios.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username.trim())
                        && u.getPassword().equals(password))
                .findFirst();
    }

    public boolean existeUsername(String username) {
        return usuarios.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username.trim()));
    }

    public Usuario registrar(String username, String password, String nombre, String email, Usuario.Tipo tipo) {
        Usuario u = new Usuario(secuencia.getAndIncrement(), username.trim(), password, nombre.trim(), email.trim(), tipo);
        usuarios.add(u);
        return u;
    }

    public List<Usuario> listarPacientes() {
        return usuarios.stream().filter(u -> u.getTipo() == Usuario.Tipo.PACIENTE).toList();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarios.stream().filter(u -> u.getId().equals(id)).findFirst();
    }
}
