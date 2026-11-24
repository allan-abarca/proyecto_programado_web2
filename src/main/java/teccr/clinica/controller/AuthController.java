package teccr.clinica.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import teccr.clinica.data.RegistroForm;
import teccr.clinica.data.Usuario;
import teccr.clinica.service.UsuarioService;

import java.util.Optional;

/** Login, logout y registro de pacientes. */
@Slf4j
@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session) {
        Optional<Usuario> usuario = usuarioService.autenticar(username, password);
        if (usuario.isEmpty()) {
            log.warn("Login fallido para {}", username);
            return "redirect:/login?error=credenciales";
        }
        session.setAttribute("usuario", usuario.get());
        return usuario.get().getTipo() == Usuario.Tipo.ADMIN
                ? "redirect:/admin/inicio"
                : "redirect:/paciente/inicio";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/?logout";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("registroForm", new RegistroForm());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registroForm") RegistroForm form,
                            Errors errors,
                            RedirectAttributes redirect) {
        if (form.getUsername() != null && usuarioService.existeUsername(form.getUsername())) {
            errors.rejectValue("username", "duplicado", "Ese nombre de usuario ya está en uso");
        }
        if (errors.hasErrors()) {
            return "registro";
        }
        // Todo registro público crea un PACIENTE; los administradores no se autoregistran.
        usuarioService.registrar(form.getUsername(), form.getPassword(), form.getNombre(),
                form.getEmail(), Usuario.Tipo.PACIENTE);
        redirect.addFlashAttribute("exito", "Registro exitoso. Ya puede iniciar sesión.");
        return "redirect:/login";
    }
}
