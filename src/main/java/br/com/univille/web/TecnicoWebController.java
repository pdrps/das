package br.com.univille.web;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.univille.entity.Perfil;
import br.com.univille.exception.RecursoNaoEncontradoException;
import br.com.univille.exception.RegraNegocioException;
import br.com.univille.service.TecnicoService;
import br.com.univille.web.form.TecnicoForm;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/web/tecnicos")
public class TecnicoWebController {

    private final TecnicoService service;

    public TecnicoWebController(TecnicoService service) {
        this.service = service;
    }

    @ModelAttribute("perfis")
    public Perfil[] perfis() {
        return Perfil.values();
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tecnicos", service.listar());
        return "tecnicos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new TecnicoForm());
        prepararForm(model, "Novo técnico", "/web/tecnicos");
        return "tecnicos/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") TecnicoForm form,
                        BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                service.criar(form.toRequest());
                redirect.addFlashAttribute("mensagem", "Técnico cadastrado com sucesso");
                return "redirect:/web/tecnicos";
            } catch (RegraNegocioException ex) {
                result.rejectValue("email", "duplicado", ex.getMessage());
            }
        }
        prepararForm(model, "Novo técnico", "/web/tecnicos");
        return "tecnicos/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("form", TecnicoForm.de(service.buscar(id)));
        } catch (RecursoNaoEncontradoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/web/tecnicos";
        }
        prepararForm(model, "Editar técnico", "/web/tecnicos/" + id + "/editar");
        return "tecnicos/form";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") TecnicoForm form,
                            BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                service.atualizar(id, form.toRequest());
                redirect.addFlashAttribute("mensagem", "Técnico atualizado com sucesso");
                return "redirect:/web/tecnicos";
            } catch (RegraNegocioException ex) {
                result.rejectValue("email", "duplicado", ex.getMessage());
            } catch (RecursoNaoEncontradoException ex) {
                redirect.addFlashAttribute("erro", ex.getMessage());
                return "redirect:/web/tecnicos";
            }
        }
        prepararForm(model, "Editar técnico", "/web/tecnicos/" + id + "/editar");
        return "tecnicos/form";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            service.excluir(id);
            redirect.addFlashAttribute("mensagem", "Técnico excluído com sucesso");
        } catch (RecursoNaoEncontradoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("erro", "Não é possível excluir: o técnico possui chamados");
        }
        return "redirect:/web/tecnicos";
    }

    private void prepararForm(Model model, String titulo, String action) {
        model.addAttribute("titulo", titulo);
        model.addAttribute("action", action);
    }
}