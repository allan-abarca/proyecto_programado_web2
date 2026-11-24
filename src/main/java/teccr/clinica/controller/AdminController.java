package teccr.clinica.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import teccr.clinica.data.Cita;
import teccr.clinica.data.Usuario;
import teccr.clinica.service.CitaService;
import teccr.clinica.service.UsuarioService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Vistas del personal administrativo: agenda diaria/semanal y gestión de citas. */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CitaService citaService;
    private final UsuarioService usuarioService;

    public AdminController(CitaService citaService, UsuarioService usuarioService) {
        this.citaService = citaService;
        this.usuarioService = usuarioService;
    }

    /** Agenda: vista=dia | semana, fecha=cualquier día del periodo a consultar. */
    @GetMapping("/agenda")
    public String agenda(@RequestParam(defaultValue = "dia") String vista,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                         Model model) {
        LocalDate base = fecha != null ? fecha : LocalDate.now();
        boolean semanal = vista.equals("semana");

        LocalDate desde = semanal ? base.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) : base;
        LocalDate hasta = semanal ? desde.plusDays(4) : base;   // lunes a viernes
        int salto = semanal ? 7 : 1;

        // Un grupo por día del periodo (incluso los días sin citas)
        Map<LocalDate, List<Cita>> porDia = new LinkedHashMap<>();
        for (LocalDate d = desde; !d.isAfter(hasta); d = d.plusDays(1)) {
            porDia.put(d, new ArrayList<>());
        }
        citaService.agenda(desde, hasta).forEach(c -> porDia.get(c.getFecha()).add(c));

        model.addAttribute("vista", semanal ? "semana" : "dia");
        model.addAttribute("fecha", base);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("anterior", base.minusDays(salto));
        model.addAttribute("siguiente", base.plusDays(salto));
        model.addAttribute("porDia", porDia);
        return "admin/agenda";
    }

    @PostMapping("/citas/{id}/cancelar")
    public String cancelar(@PathVariable Long id,
                           @RequestParam(defaultValue = "dia") String vista,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                           HttpSession session,
                           RedirectAttributes redirect) {
        Usuario admin = (Usuario) session.getAttribute("usuario");
        try {
            citaService.cancelar(id, admin);
            redirect.addFlashAttribute("exito", "La cita fue cancelada. El horario quedó disponible.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/agenda?vista=" + vista + (fecha != null ? "&fecha=" + fecha : "");
    }

    /** El administrador agenda una cita a nombre de un paciente. */
    @GetMapping("/citas/nueva")
    public String nuevaCita(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                            Model model) {
        model.addAttribute("hoy", LocalDate.now());
        model.addAttribute("fecha", fecha);
        model.addAttribute("pacientes", usuarioService.listarPacientes());
        if (fecha != null) {
            model.addAttribute("horarios", citaService.horariosDisponibles(fecha));
        }
        return "admin/nueva-cita";
    }

    @PostMapping("/citas")
    public String crearCita(@RequestParam Long pacienteId,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora,
                            RedirectAttributes redirect) {
        try {
            Usuario paciente = usuarioService.buscarPorId(pacienteId)
                    .orElseThrow(() -> new IllegalArgumentException("Debe seleccionar un paciente válido."));
            citaService.crear(paciente, fecha, hora);
            redirect.addFlashAttribute("exito", "Cita registrada para " + paciente.getNombre() + ".");
            return "redirect:/admin/agenda?vista=dia&fecha=" + fecha;
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/citas/nueva?fecha=" + fecha;
        }
    }
}
