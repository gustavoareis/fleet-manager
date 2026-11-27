package com.medagenda.admin;

import com.medagenda.common.error.ConflictException;
import com.medagenda.insurance.HealthInsuranceRequest;
import com.medagenda.insurance.HealthInsuranceResponse;
import com.medagenda.insurance.HealthInsuranceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/convenios")
public class HealthInsuranceAdminController {

    private static final int PAGE_SIZE = 10;
    private static final String FORM_VIEW = "admin/convenios/form";
    private static final String LIST_URL = "/admin/convenios";

    private final HealthInsuranceService insuranceService;

    public HealthInsuranceAdminController(HealthInsuranceService insuranceService) {
        this.insuranceService = insuranceService;
    }

    @GetMapping
    String list(@RequestParam(defaultValue = "") String busca, @RequestParam(defaultValue = "0") int pagina,
                Model model) {
        model.addAttribute("page", insuranceService.search(busca, null,
                PageRequest.of(Math.max(pagina, 0), PAGE_SIZE, Sort.by("name"))));
        model.addAttribute("busca", busca);
        return "admin/convenios/list";
    }

    @GetMapping("/novo")
    String newForm(Model model) {
        model.addAttribute("form", new HealthInsuranceForm());
        model.addAttribute("action", LIST_URL);
        return FORM_VIEW;
    }

    @PostMapping
    String create(@Valid @ModelAttribute("form") HealthInsuranceForm form, BindingResult result, Model model,
                  RedirectAttributes redirect) {
        model.addAttribute("action", LIST_URL);
        return save(result, redirect,
                () -> insuranceService.create(new HealthInsuranceRequest(form.getName(), form.isActive())),
                "Convênio cadastrado.");
    }

    @GetMapping("/{id}/editar")
    String editForm(@PathVariable Long id, Model model) {
        HealthInsuranceResponse insurance = insuranceService.findById(id);
        HealthInsuranceForm form = new HealthInsuranceForm();
        form.setName(insurance.name());
        form.setActive(insurance.active());
        model.addAttribute("form", form);
        model.addAttribute("action", LIST_URL + "/" + id);
        return FORM_VIEW;
    }

    @PostMapping("/{id}")
    String update(@PathVariable Long id, @Valid @ModelAttribute("form") HealthInsuranceForm form,
                  BindingResult result, Model model, RedirectAttributes redirect) {
        model.addAttribute("action", LIST_URL + "/" + id);
        return save(result, redirect,
                () -> insuranceService.update(id, new HealthInsuranceRequest(form.getName(), form.isActive())),
                "Convênio atualizado.");
    }

    @PostMapping("/{id}/excluir")
    String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            insuranceService.delete(id);
            redirect.addFlashAttribute("success", "Convênio excluído.");
        } catch (ConflictException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:" + LIST_URL;
    }

    private String save(BindingResult result, RedirectAttributes redirect, Runnable operation,
                        String successMessage) {
        if (result.hasErrors()) {
            return FORM_VIEW;
        }
        try {
            operation.run();
        } catch (ConflictException ex) {
            result.rejectValue("name", "duplicate", ex.getMessage());
            return FORM_VIEW;
        }
        redirect.addFlashAttribute("success", successMessage);
        return "redirect:" + LIST_URL;
    }
}
