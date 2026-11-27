package com.medagenda.admin;

import com.medagenda.common.error.BadRequestException;
import com.medagenda.common.error.BusinessException;
import com.medagenda.common.error.ConflictException;
import com.medagenda.common.error.NotFoundException;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Errors of the JSP pages are shown as HTML. The global handler of the REST API answers JSON,
 * so this advice is limited to this package and runs first: that way the two never mix.
 */
@ControllerAdvice(basePackages = "com.medagenda.admin")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdminControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(AdminControllerAdvice.class);

    /** Available to every admin view: who is logged in. */
    @ModelAttribute
    void addLoggedUser(Principal principal, Model model) {
        if (principal != null) {
            model.addAttribute("adminUser", principal.getName());
        }
    }

    @ExceptionHandler(NotFoundException.class)
    ModelAndView notFound(NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "Não encontrado", ex.getMessage());
    }

    @ExceptionHandler({BusinessException.class, ConflictException.class, BadRequestException.class})
    ModelAndView rejected(RuntimeException ex) {
        HttpStatus status = ex instanceof ConflictException ? HttpStatus.CONFLICT
                : ex instanceof BusinessException ? HttpStatus.UNPROCESSABLE_CONTENT
                : HttpStatus.BAD_REQUEST;
        return error(status, "Não foi possível concluir a operação", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ModelAndView invalidParameter(MethodArgumentTypeMismatchException ex) {
        return error(HttpStatus.BAD_REQUEST, "Parâmetro inválido",
                "O valor informado para '%s' não é válido.".formatted(ex.getName()));
    }

    @ExceptionHandler(Exception.class)
    ModelAndView unexpected(Exception ex) {
        log.error("Unexpected error in the admin module", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado",
                "Ocorreu um erro interno. Tente novamente em instantes.");
    }

    private static ModelAndView error(HttpStatus status, String title, String message) {
        ModelAndView view = new ModelAndView("admin/error", status);
        view.addObject("errorTitle", title);
        view.addObject("errorMessage", message);
        return view;
    }
}
