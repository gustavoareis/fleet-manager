package com.medagenda.admin;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** The login form and the "wrong role" page. The POST of the form is handled by Spring Security. */
@Controller
public class LoginAdminController {

    @GetMapping("/admin/login")
    String login(Principal principal) {
        // Already logged in: no reason to show the form again
        return principal != null ? "redirect:/admin" : "admin/login";
    }

    /** Any method: after a failed CSRF check on a POST, the container forwards here keeping the POST. */
    @RequestMapping("/admin/acesso-negado")
    String denied() {
        return "admin/denied";
    }
}
