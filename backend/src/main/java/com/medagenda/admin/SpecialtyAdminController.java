package com.medagenda.admin;

import com.medagenda.common.error.ConflictException;
import com.medagenda.specialty.SpecialtyRequest;
import com.medagenda.specialty.SpecialtyResponse;
import com.medagenda.specialty.SpecialtyService;
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

/**
 * Same service as the REST controller: the JSP module is just another client of the business
 * layer. Writes follow Post/Redirect/Get, so refreshing the page never re-submits a form.
 */
@Controller
@RequestMapping("/admin/especialidades")
public class SpecialtyAdminController {

    private static final int PAGE_SIZE = 10;
    private static final String FORM_VIEW = "admin/especialidades/form";
    private static final String LIST_URL = "/admin/especialidades";

    private final SpecialtyService specialtyService;

    public SpecialtyAdminController(SpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    @GetMapping
    String list(@RequestParam(defaultValue = "") String busca, @RequestParam(defaultValue = "0") int pagina,
                Model model) {
        model.addAttribute("page", specialtyService.search(busca,
                PageRequest.of(Math.max(pagina, 0), PAGE_SIZE, Sort.by("name"))));
        model.addAttribute("busca", busca);
        return "admin/especialidades/list";
    }

    @GetMapping("/nova")
    String newForm(Model model) {
        model.addAttribute("form", new SpecialtyForm());
        model.addAttribute("action", LIST_URL);
        return FORM_VIEW;
    }

    @PostMapping
    String create(@Valid @ModelAttribute("form") SpecialtyForm form, BindingResult result, Model model,
                  RedirectAttributes redirect) {
        model.addAttribute("action", LIST_URL);
        return save(result, redirect, () -> specialtyService.create(new SpecialtyRequest(form.getName())),
                "Especialidade cadastrada.");
    }

    @GetMapping("/{id}/editar")
    String editForm(@PathVariable Long id, Model model) {
        SpecialtyResponse specialty = specialtyService.findById(id);
        SpecialtyForm form = new SpecialtyForm();
        form.setName(specialty.name());
        model.addAttribute("form", form);
        model.addAttribute("action", LIST_URL + "/" + id);
        return FORM_VIEW;
    }

    @PostMapping("/{id}")
    String update(@PathVariable Long id, @Valid @ModelAttribute("form") SpecialtyForm form, BindingResult result,
                  Model model, RedirectAttributes redirect) {
        model.addAttribute("action", LIST_URL + "/" + id);
        return save(result, redirect, () -> specialtyService.update(id, new SpecialtyRequest(form.getName())),
                "Especialidade atualizada.");
    }

    @PostMapping("/{id}/excluir")
    String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            specialtyService.delete(id);
            redirect.addFlashAttribute("success", "Especialidade excluída.");
        } catch (ConflictException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:" + LIST_URL;
    }

    /** Shared by create and update: validation errors and duplicates re-show the form with the messages. */
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
