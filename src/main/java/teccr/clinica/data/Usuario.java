package teccr.clinica.data;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Usuario del sistema. Puede ser PACIENTE o ADMIN (personal administrativo).
 */
@Data
@AllArgsConstructor
public class Usuario {
    private Long id;
    private String username;
    private String password;   // TODO (seguridad): guardar con hash, nunca en texto plano
    private String nombre;
    private String email;
    private Tipo tipo;

    public enum Tipo {
        ADMIN,
        PACIENTE
    }
}
