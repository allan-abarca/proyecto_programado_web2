package teccr.clinica.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Cita asociada a un paciente, con fecha y hora definidas.
 * Una cita CANCELADA libera su horario.
 */
@Data
@AllArgsConstructor
public class Cita {
    private Long id;
    private Usuario paciente;
    private LocalDate fecha;
    private LocalTime hora;
    private Estado estado;

    public enum Estado {
        ACTIVA,
        CANCELADA
    }
}
