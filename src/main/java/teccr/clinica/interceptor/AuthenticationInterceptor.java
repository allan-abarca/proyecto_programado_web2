package teccr.clinica.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import teccr.clinica.data.Usuario;

/**
 * Protege las rutas según el perfil:
 *  - rutas públicas: inicio, login, registro y recursos estáticos
 *  - /paciente/** solo para PACIENTE
 *  - /admin/** solo para ADMIN
 */
@Slf4j
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        if (esPublica(path)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        Usuario usuario = session == null ? null : (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            log.info("Acceso sin sesión a {}", path);
            response.sendRedirect("/login?error=no_autenticado");
            return false;
        }

        boolean rutaAdmin = path.startsWith("/admin");
        boolean rutaPaciente = path.startsWith("/paciente");
        if ((rutaAdmin && usuario.getTipo() != Usuario.Tipo.ADMIN)
                || (rutaPaciente && usuario.getTipo() != Usuario.Tipo.PACIENTE)) {
            log.warn("Usuario {} ({}) sin permiso para {}", usuario.getUsername(), usuario.getTipo(), path);
            response.sendRedirect("/?error=sin_permiso");
            return false;
        }
        return true;
    }

    private boolean esPublica(String path) {
        return path.equals("/")
                || path.startsWith("/login")
                || path.startsWith("/registro")
                || path.startsWith("/logout")
                || path.startsWith("/css/")
                || path.startsWith("/images/")
                || path.startsWith("/error");
    }
}
