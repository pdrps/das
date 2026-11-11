package br.com.univille.web;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

import br.com.univille.dto.AlterarStatusRequest;
import br.com.univille.dto.AtribuirTecnicoRequest;
import br.com.univille.entity.Prioridade;
import br.com.univille.entity.StatusChamado;
import br.com.univille.exception.RecursoNaoEncontradoException;
import br.com.univille.exception.RegraNegocioException;
import br.com.univille.service.CategoriaService;
import br.com.univille.service.ChamadoService;
import br.com.univille.service.ClienteService;
import br.com.univille.service.TecnicoService;
import br.com.univille.web.form.ChamadoForm;
import br.com.univille.web.form.ComentarioForm;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/web/chamados")
public class ChamadoWebController {

    private final ChamadoService service;
    private final ClienteService clienteService;
    private final CategoriaService categoriaService;
    private final TecnicoService tecnicoService;

    public ChamadoWebController(ChamadoService service,
                                ClienteService clienteService,
                                CategoriaService categoriaService,
                                TecnicoService tecnicoService) {
        this.service = service;
        this.clienteService = clienteService;
        this.categoriaService = categoriaService;
        this.tecnicoService = tecnicoService;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam(required = false) Long clienteId,
            @PageableDefault(size = 10, sort = "dataAbertura", direction = Sort.Direction.DESC)
            Pageable pageable,
            Model model) {

        model.addAttribute("chamados", service.listar(status, prioridade, clienteId, pageable));
        model.addAttribute("status", status);
        model.addAttribute("prioridade", prioridade);
        model.addAttribute("clienteId", clienteId);
        model.addAttribute("statusLista", StatusChamado.values());
        model.addAttribute("prioridades", Prioridade.values());
        model.addAttribute("clientes", clienteService.listar());
        return "chamados/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new ChamadoForm());
        carregarOpcoes(model);
        return "chamados/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") ChamadoForm form,
                        BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                service.criar(form.toRequest());
                redirect.addFlashAttribute("mensagem", "Chamado aberto com sucesso");
                return "redirect:/web/chamados";
            } catch (RecursoNaoEncontradoException | RegraNegocioException ex) {
                result.reject("chamado.invalido", ex.getMessage());
            }
        }
        carregarOpcoes(model);
        return "chamados/form";
    }

    private void carregarOpcoes(Model model) {
        model.addAttribute("prioridades", Prioridade.values());
        model.addAttribute("clientes", clienteService.listar());
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("tecnicos", tecnicoService.listar());
    }

    @GetMapping("/{id}")
public String detalhe(@PathVariable Long id, Model model, RedirectAttributes redirect) {
    try {
        model.addAttribute("chamado", service.buscar(id));
    } catch (RecursoNaoEncontradoException ex) {
        redirect.addFlashAttribute("erro", ex.getMessage());
        return "redirect:/web/chamados";
    }
    model.addAttribute("statusLista", StatusChamado.values());
    model.addAttribute("tecnicos", tecnicoService.listar());
    return "chamados/detalhe";
}

@PostMapping("/{id}/status")
public String alterarStatus(@PathVariable Long id,
                            @RequestParam(required = false) StatusChamado status,
                            RedirectAttributes redirect) {
    if (status == null) {
        redirect.addFlashAttribute("erro", "Selecione o novo status");
    } else {
        try {
            service.alterarStatus(id, new AlterarStatusRequest(status));
            redirect.addFlashAttribute("mensagem", "Status alterado para " + status);
        } catch (RegraNegocioException | RecursoNaoEncontradoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
    }
    return "redirect:/web/chamados/" + id;
}

@PostMapping("/{id}/atribuir")
public String atribuir(@PathVariable Long id,
                       @RequestParam(required = false) Long tecnicoId,
                       RedirectAttributes redirect) {
    if (tecnicoId == null) {
        redirect.addFlashAttribute("erro", "Selecione um técnico");
    } else {
        try {
            service.atribuir(id, new AtribuirTecnicoRequest(tecnicoId));
            redirect.addFlashAttribute("mensagem", "Técnico atribuído com sucesso");
        } catch (RegraNegocioException | RecursoNaoEncontradoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
    }
    return "redirect:/web/chamados/" + id;
}

@PostMapping("/{id}/comentarios")
public String comentar(@PathVariable Long id,
                       @Valid @ModelAttribute ComentarioForm form,
                       BindingResult result, RedirectAttributes redirect) {
    if (result.hasErrors()) {
        redirect.addFlashAttribute("erro", result.getAllErrors().get(0).getDefaultMessage());
    } else {
        try {
            service.comentar(id, form.toRequest());
            redirect.addFlashAttribute("mensagem", "Comentário adicionado");
        } catch (RegraNegocioException | RecursoNaoEncontradoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
    }
    return "redirect:/web/chamados/" + id;
}

@PostMapping("/{id}/excluir")
public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
    try {
        service.excluir(id);
        redirect.addFlashAttribute("mensagem", "Chamado excluído com sucesso");
    } catch (RecursoNaoEncontradoException ex) {
        redirect.addFlashAttribute("erro", ex.getMessage());
    } catch (DataIntegrityViolationException ex) {
        redirect.addFlashAttribute("erro", "Não é possível excluir: o chamado possui comentários");
    }
    return "redirect:/web/chamados";
}
}