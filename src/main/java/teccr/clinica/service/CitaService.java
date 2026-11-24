package teccr.clinica.service;

import org.springframework.stereotype.Service;
import teccr.clinica.data.Cita;
import teccr.clinica.data.Usuario;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SERVICIO TEMPORAL EN MEMORIA con las reglas de negocio básicas de citas.
 * Se reemplaza por la implementación real (con repositorios) manteniendo estas firmas.
 *
 * Supuestos (ver reglas de negocio): la clínica atiende de lunes a viernes,
 * de 8:00 a 16:00, un horario por hora y una sola cita por horario.
 */
@Service
public class CitaService {

    private static final List<LocalTime> HORARIOS = List.of(
            LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
            LocalTime.of(13, 0), LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0));

    private final List<Cita> citas = new ArrayList<>();
    private final AtomicLong secuencia = new AtomicLong(1);

    public CitaService(UsuarioService usuarioService) {
        // Datos de ejemplo para ver la agenda con contenido
        List<Usuario> pacientes = usuarioService.listarPacientes();
        // Se siembra desde mañana para que las citas de ejemplo siempre sean futuras y válidas
        LocalDate dia = proximoDiaHabil(LocalDate.now().plusDays(1));
        crear(pacientes.get(0), dia, LocalTime.of(9, 0));
        crear(pacientes.get(1), dia, LocalTime.of(10, 0));
        crear(pacientes.get(0), proximoDiaHabil(dia.plusDays(1)), LocalTime.of(14, 0));
    }

    /** Horarios que la clínica ofrece en un día hábil. */
    public List<LocalTime> horariosDelDia() {
        return HORARIOS;
    }

    public boolean esDiaHabil(LocalDate fecha) {
        DayOfWeek d = fecha.getDayOfWeek();
        return d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY;
    }

    public LocalDate proximoDiaHabil(LocalDate desde) {
        LocalDate f = desde;
        while (!esDiaHabil(f)) {
            f = f.plusDays(1);
        }
        return f;
    }

    public boolean estaDisponible(LocalDate fecha, LocalTime hora) {
        return esDiaHabil(fecha)
                && HORARIOS.contains(hora)
                && citas.stream().noneMatch(c -> c.getEstado() == Cita.Estado.ACTIVA
                        && c.getFecha().equals(fecha) && c.getHora().equals(hora));
    }

    /** Horarios libres de una fecha (vacío si la fecha no es válida). */
    public List<LocalTime> horariosDisponibles(LocalDate fecha) {
        if (fecha == null || fecha.isBefore(LocalDate.now()) || !esDiaHabil(fecha)) {
            return List.of();
        }
        return HORARIOS.stream()
                .filter(h -> estaDisponible(fecha, h))
                .filter(h -> !fecha.equals(LocalDate.now()) || h.isAfter(LocalTime.now()))
                .toList();
    }

    /**
     * Crea una cita validando fecha y disponibilidad.
     * @throws IllegalArgumentException con un mensaje apto para mostrar al usuario
     */
    public Cita crear(Usuario paciente, LocalDate fecha, LocalTime hora) {
        if (fecha == null || hora == null) {
            throw new IllegalArgumentException("Debe indicar la fecha y la hora de la cita.");
        }
        if (fecha.isBefore(LocalDate.now()) || (fecha.equals(LocalDate.now()) && !hora.isAfter(LocalTime.now()))) {
            throw new IllegalArgumentException("La fecha y hora de la cita deben ser futuras.");
        }
        if (!esDiaHabil(fecha) || !HORARIOS.contains(hora)) {
            throw new IllegalArgumentException("La clínica no atiende en ese día u horario.");
        }
        if (!estaDisponible(fecha, hora)) {
            throw new IllegalArgumentException("Ese horario ya está ocupado. Elija otro horario.");
        }
        Cita cita = new Cita(secuencia.getAndIncrement(), paciente, fecha, hora, Cita.Estado.ACTIVA);
        citas.add(cita);
        return cita;
    }

    /**
     * Cancela una cita. Un paciente solo puede cancelar las suyas; el administrador cualquiera.
     * Al cancelarla, el horario queda libre de nuevo.
     */
    public void cancelar(Long citaId, Usuario solicitante) {
        Cita cita = citas.stream().filter(c -> c.getId().equals(citaId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La cita no existe."));
        boolean esAdmin = solicitante.getTipo() == Usuario.Tipo.ADMIN;
        if (!esAdmin && !cita.getPaciente().getId().equals(solicitante.getId())) {
            throw new IllegalArgumentException("Solo puede cancelar sus propias citas.");
        }
        cita.setEstado(Cita.Estado.CANCELADA);
    }

    public List<Cita> citasDePaciente(Usuario paciente) {
        return ordenadas(citas.stream().filter(c -> c.getPaciente().getId().equals(paciente.getId())).toList());
    }

    /** Citas activas entre dos fechas (inclusive), para la agenda diaria o semanal. */
    public List<Cita> agenda(LocalDate desde, LocalDate hasta) {
        return ordenadas(citas.stream()
                .filter(c -> c.getEstado() == Cita.Estado.ACTIVA)
                .filter(c -> !c.getFecha().isBefore(desde) && !c.getFecha().isAfter(hasta))
                .toList());
    }

    private List<Cita> ordenadas(List<Cita> lista) {
        return lista.stream()
                .sorted(Comparator.comparing(Cita::getFecha).thenComparing(Cita::getHora))
                .toList();
    }
}
