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
import br.com.univille.service.ClienteService;
import br.com.univille.web.form.ClienteForm;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/web/clientes")
public class ClienteWebController {

    private final ClienteService service;

    public ClienteWebController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", service.listar());
        return "clientes/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new ClienteForm());
        prepararForm(model, "Novo cliente", "/web/clientes");
        return "clientes/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") ClienteForm form,
                        BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                service.criar(form.toRequest());
                redirect.addFlashAttribute("mensagem", "Cliente cadastrado com sucesso");
                return "redirect:/web/clientes";
            } catch (RegraNegocioException ex) {
                result.rejectValue("email", "duplicado", ex.getMessage());
            }
        }
        prepararForm(model, "Novo cliente", "/web/clientes");
        return "clientes/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("form", ClienteForm.de(service.buscar(id)));
        } catch (RecursoNaoEncontradoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/web/clientes";
        }
        prepararForm(model, "Editar cliente", "/web/clientes/" + id + "/editar");
        return "clientes/form";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") ClienteForm form,
                            BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                service.atualizar(id, form.toRequest());
                redirect.addFlashAttribute("mensagem", "Cliente atualizado com sucesso");
                return "redirect:/web/clientes";
            } catch (RegraNegocioException ex) {
                result.rejectValue("email", "duplicado", ex.getMessage());
            } catch (RecursoNaoEncontradoException ex) {
                redirect.addFlashAttribute("erro", ex.getMessage());
                return "redirect:/web/clientes";
            }
        }
        prepararForm(model, "Editar cliente", "/web/clientes/" + id + "/editar");
        return "clientes/form";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            service.excluir(id);
            redirect.addFlashAttribute("mensagem", "Cliente excluído com sucesso");
        } catch (RecursoNaoEncontradoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("erro", "Não é possível excluir: o cliente possui chamados");
        }
        return "redirect:/web/clientes";
    }

    private void prepararForm(Model model, String titulo, String action) {
        model.addAttribute("titulo", titulo);
        model.addAttribute("action", action);
    }
}