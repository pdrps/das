package br.com.univille.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.univille.dto.AlterarStatusRequest;
import br.com.univille.dto.AtribuirTecnicoRequest;
import br.com.univille.dto.ChamadoDetalheResponse;
import br.com.univille.dto.ChamadoRequest;
import br.com.univille.dto.ChamadoResponse;
import br.com.univille.dto.ComentarioRequest;
import br.com.univille.dto.ComentarioResponse;
import br.com.univille.entity.Prioridade;
import br.com.univille.entity.StatusChamado;
import br.com.univille.service.ChamadoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    private final ChamadoService service;

    public ChamadoController(ChamadoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ChamadoResponse> criar(
        @Valid @RequestBody ChamadoRequest dto) {
        ChamadoResponse criado = service.criar(dto);
        return ResponseEntity.created(URI.create("/chamados/" + criado.id())).body(criado);
    }

    @GetMapping
    public Page<ChamadoResponse> listar(
        @RequestParam(required = false) StatusChamado status,
        @RequestParam(required = false) Prioridade prioridade,
        @RequestParam(required = false) Long clienteId,
        @PageableDefault(size = 10, sort = "dataAbertura", direction = Sort.Direction.DESC)
        Pageable pageable) {
        return service.listar(status, prioridade, clienteId, pageable);
    }

    @GetMapping("/{id}")
    public ChamadoDetalheResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public ChamadoResponse atualizar(
        @PathVariable Long id,
        @Valid @RequestBody ChamadoRequest dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/atribuir")
    public ChamadoResponse atribuir(
        @PathVariable Long id,
        @Valid @RequestBody AtribuirTecnicoRequest dto) {
        return service.atribuir(id, dto);
    }

    @PatchMapping("/{id}/status")
    public ChamadoResponse alterarStatus(
        @PathVariable Long id,
        @Valid @RequestBody AlterarStatusRequest dto) {
        return service.alterarStatus(id, dto);
    }

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ComentarioResponse> comentar(
        @PathVariable Long id,
        @Valid @RequestBody ComentarioRequest dto) {
        ComentarioResponse criado = service.comentar(id, dto);
        return ResponseEntity
                .created(URI.create("/chamados/" + id + "/comentarios/" + criado.id()))
                .body(criado);
    }
}