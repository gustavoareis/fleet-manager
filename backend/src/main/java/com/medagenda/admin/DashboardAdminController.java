package com.medagenda.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardAdminController {

    private final ReceptionService receptionService;

    public DashboardAdminController(ReceptionService receptionService) {
        this.receptionService = receptionService;
    }

    @GetMapping("/admin")
    String dashboard(Model model) {
        var today = receptionService.today();
        model.addAttribute("today", today.toString());
        model.addAttribute("summary", receptionService.summary(today));
        return "admin/dashboard";
    }
}
