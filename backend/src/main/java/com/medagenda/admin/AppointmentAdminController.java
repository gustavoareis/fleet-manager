package com.medagenda.admin;

import com.medagenda.doctor.DoctorService;
import java.time.LocalDate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** The day's appointments with the "confirm arrival" (check-in) button. */
@Controller
public class AppointmentAdminController {

    private final ReceptionService receptionService;
    private final DoctorService doctorService;

    public AppointmentAdminController(ReceptionService receptionService, DoctorService doctorService) {
        this.receptionService = receptionService;
        this.doctorService = doctorService;
    }

    @GetMapping("/admin/consultas")
    String list(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                @RequestParam(required = false) Long medico,
                @RequestParam(defaultValue = "0") int pagina,
                Model model) {
        LocalDate day = data != null ? data : receptionService.today();
        model.addAttribute("date", day.toString());
        model.addAttribute("isToday", day.equals(receptionService.today()));
        model.addAttribute("doctorId", medico);
        model.addAttribute("page", receptionService.listDay(day, medico, pagina));
        model.addAttribute("summary", receptionService.summary(day));
        model.addAttribute("doctors", doctorService.search("", null,
                PageRequest.of(0, 100, Sort.by("name"))).content());
        return "admin/consultas";
    }

    /** POST (never GET): it changes data, so it also goes through the CSRF check. */
    @PostMapping("/admin/consultas/{id}/chegada")
    String confirmArrival(@PathVariable Long id,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                          @RequestParam(required = false) Long medico,
                          @RequestParam(defaultValue = "0") int pagina,
                          RedirectAttributes redirect) {
        String patient = receptionService.confirmArrival(id);
        redirect.addFlashAttribute("success", "Chegada de " + patient + " confirmada.");
        // Keep the filters the user was looking at (added to the query string of the redirect).
        // The date goes as ISO text: redirect attributes would otherwise format a LocalDate with the
        // locale (27/11/2026), which the "data" filter does not accept.
        redirect.addAttribute("data", data.toString());
        redirect.addAttribute("pagina", pagina);
        if (medico != null) {
            redirect.addAttribute("medico", medico);
        }
        return "redirect:/admin/consultas";
    }
}
