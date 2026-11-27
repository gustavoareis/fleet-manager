package com.medagenda.admin;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReportAdminController {

    private final ReportService reportService;
    private final ReceptionService receptionService;

    public ReportAdminController(ReportService reportService, ReceptionService receptionService) {
        this.reportService = reportService;
        this.receptionService = receptionService;
    }

    /** Default period: from the first day of the current month until today. */
    @GetMapping("/admin/relatorio")
    String report(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                  Model model) {
        LocalDate today = receptionService.today();
        LocalDate from = de != null ? de : today.withDayOfMonth(1);
        LocalDate to = ate != null ? ate : today;
        model.addAttribute("report", reportService.report(from, to));
        model.addAttribute("from", from.toString());
        model.addAttribute("to", to.toString());
        return "admin/relatorio";
    }
}
