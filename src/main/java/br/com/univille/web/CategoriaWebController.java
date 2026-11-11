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

import br.com.univille.exception.RecursoNaoEncontradoException;
import br.com.univille.exception.RegraNegocioException;
import br.com.univille.service.CategoriaService;
import br.com.univille.web.form.CategoriaForm;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/web/categorias")
public class CategoriaWebController {

    private final CategoriaService service;

    public CategoriaWebController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", service.listar());
        return "categorias/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new CategoriaForm());
        prepararForm(model, "Nova categoria", "/web/categorias");
        return "categorias/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") CategoriaForm form,
                        BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                service.criar(form.toRequest());
                redirect.addFlashAttribute("mensagem", "Categoria cadastrada com sucesso");
                return "redirect:/web/categorias";
            } catch (RegraNegocioException ex) {
                result.rejectValue("nome", "duplicado", ex.getMessage());
            }
        }
        prepararForm(model, "Nova categoria", "/web/categorias");
        return "categorias/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("form", CategoriaForm.de(service.buscar(id)));
        } catch (RecursoNaoEncontradoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/web/categorias";
        }
        prepararForm(model, "Editar categoria", "/web/categorias/" + id + "/editar");
        return "categorias/form";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") CategoriaForm form,
                            BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                service.atualizar(id, form.toRequest());
                redirect.addFlashAttribute("mensagem", "Categoria atualizada com sucesso");
                return "redirect:/web/categorias";
            } catch (RegraNegocioException ex) {
                result.rejectValue("nome", "duplicado", ex.getMessage());
            } catch (RecursoNaoEncontradoException ex) {
                redirect.addFlashAttribute("erro", ex.getMessage());
                return "redirect:/web/categorias";
            }
        }
        prepararForm(model, "Editar categoria", "/web/categorias/" + id + "/editar");
        return "categorias/form";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            service.excluir(id);
            redirect.addFlashAttribute("mensagem", "Categoria excluída com sucesso");
        } catch (RecursoNaoEncontradoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("erro", "Não é possível excluir: a categoria possui chamados");
        }
        return "redirect:/web/categorias";
    }

    private void prepararForm(Model model, String titulo, String action) {
        model.addAttribute("titulo", titulo);
        model.addAttribute("action", action);
    }
}