package teccr.clinica.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import teccr.clinica.data.Usuario;
import teccr.clinica.service.CitaService;

import java.time.LocalDate;
import java.time.LocalTime;

/** Vistas del paciente: ver, solicitar y cancelar SUS propias citas. */
@Controller
@RequestMapping("/paciente")
public class PacienteController {

    private final CitaService citaService;

    public PacienteController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping("/citas")
    public String misCitas(HttpSession session, Model model) {
        Usuario paciente = (Usuario) session.getAttribute("usuario");
        model.addAttribute("citas", citaService.citasDePaciente(paciente));
        return "paciente/citas";
    }

    /** Paso 1: elegir fecha. Paso 2 (con ?fecha=): se muestran los horarios libres de ese día. */
    @GetMapping("/citas/nueva")
    public String nuevaCita(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                            Model model) {
        model.addAttribute("hoy", LocalDate.now());
        model.addAttribute("fecha", fecha);
        if (fecha != null) {
            model.addAttribute("horarios", citaService.horariosDisponibles(fecha));
        }
        return "paciente/nueva-cita";
    }

    @PostMapping("/citas")
    public String solicitarCita(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora,
                                HttpSession session,
                                RedirectAttributes redirect) {
        Usuario paciente = (Usuario) session.getAttribute("usuario");
        try {
            citaService.crear(paciente, fecha, hora);
            redirect.addFlashAttribute("exito", "Su cita fue registrada correctamente.");
            return "redirect:/paciente/citas";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/paciente/citas/nueva?fecha=" + fecha;
        }
    }

    @PostMapping("/citas/{id}/cancelar")
    public String cancelar(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        Usuario paciente = (Usuario) session.getAttribute("usuario");
        try {
            citaService.cancelar(id, paciente);
            redirect.addFlashAttribute("exito", "La cita fue cancelada. El horario quedó disponible.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/paciente/citas";
    }
}
