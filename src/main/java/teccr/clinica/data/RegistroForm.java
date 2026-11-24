package teccr.clinica.data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Datos del formulario de registro de pacientes (con validaciones de la capa de presentación).
 */
@Data
public class RegistroForm {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    private String email;

    @Size(min = 3, message = "El usuario debe tener al menos 3 caracteres")
    private String username;

    @Size(min = 4, message = "La contraseña debe tener al menos 4 caracteres")
    private String password;
}
