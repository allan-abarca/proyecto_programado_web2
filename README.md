# Clínica Dental - Sistema de gestión de citas (capa de presentación)

Spring Boot 4.0.2 · Java 21 · Thymeleaf · Maven

## Ejecutar
```
./mvnw spring-boot:run
```
Abrir http://localhost:8080

Cuentas de prueba: `admin / admin123` (administrador) · `paciente1 / 1234` (paciente)

## Estructura
- `controller/`  AuthController, PacienteController, AdminController
- `service/`     UsuarioService, CitaService  (**en memoria, temporales**)
- `data/`        Usuario, Cita, RegistroForm
- `interceptor/` AuthenticationInterceptor (sesión + perfil por ruta)
- `templates/`   home, login, registro, paciente/*, admin/*, fragments/layout

## Contrato con la capa de reglas de negocio / persistencia
Los controladores solo usan estos métodos. Para conectar la persistencia real basta con
reimplementarlos con repositorios de Spring Data sin cambiar controladores ni vistas:

- `UsuarioService`: `autenticar`, `existeUsername`, `registrar`, `listarPacientes`, `buscarPorId`
- `CitaService`: `horariosDisponibles(fecha)`, `crear(paciente, fecha, hora)`,
  `cancelar(citaId, solicitante)`, `citasDePaciente(paciente)`, `agenda(desde, hasta)`
  (`crear` y `cancelar` lanzan `IllegalArgumentException` con mensaje para el usuario)
