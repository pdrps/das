package br.com.univille.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.univille.dto.AlterarStatusRequest;
import br.com.univille.dto.AtribuirTecnicoRequest;
import br.com.univille.dto.ChamadoRequest;
import br.com.univille.dto.ChamadoResponse;
import br.com.univille.entity.Categoria;
import br.com.univille.entity.Chamado;
import br.com.univille.entity.Cliente;
import br.com.univille.entity.Prioridade;
import br.com.univille.entity.StatusChamado;
import br.com.univille.entity.Tecnico;
import br.com.univille.exception.RecursoNaoEncontradoException;
import br.com.univille.exception.RegraNegocioException;
import br.com.univille.repository.CategoriaRepository;
import br.com.univille.repository.ChamadoRepository;
import br.com.univille.repository.ClienteRepository;
import br.com.univille.repository.TecnicoRepository;

@Service
public class ChamadoService {

    private final ChamadoRepository repository;
    private final ClienteRepository clienteRepository;
    private final CategoriaRepository categoriaRepository;
    private final TecnicoRepository tecnicoRepository;

    public ChamadoService(ChamadoRepository repository,
                          ClienteRepository clienteRepository,
                          CategoriaRepository categoriaRepository,
                          TecnicoRepository tecnicoRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.categoriaRepository = categoriaRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    @Transactional
    public ChamadoResponse criar(ChamadoRequest dto) {
        Cliente cliente = buscarCliente(dto.clienteId());
        Categoria categoria = buscarCategoria(dto.categoriaId());

        Chamado chamado = new Chamado(dto.titulo(), dto.descricao(), dto.prioridade(), cliente, categoria);
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setDataAbertura(LocalDateTime.now());

        if (dto.tecnicoId() != null) {
            chamado.setTecnico(buscarTecnico(dto.tecnicoId()));
        }

        Chamado salvo = repository.save(chamado);
        return ChamadoResponse.de(salvo);
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listar(StatusChamado status, Prioridade prioridade,
                                    Long clienteId, Pageable pageable) {
    return repository.filtrar(status, prioridade, clienteId, pageable)
            .map(ChamadoResponse::de);
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listarPorCliente(Long clienteId, Pageable pageable) {
        if (!clienteRepository.existsById(clienteId)) {
         throw new RecursoNaoEncontradoException("Cliente não encontrado com o id " + clienteId);
        }
    return repository.filtrar(null, null, clienteId, pageable)
            .map(ChamadoResponse::de);
    }

    @Transactional(readOnly = true)
    public ChamadoResponse buscar(Long id) {
        return ChamadoResponse.de(buscarEntity(id));
    }

    @Transactional
    public ChamadoResponse atualizar(Long id, ChamadoRequest dto) {
        
        Chamado chamado = buscarEntity(id);
        garantirNaoFechado(chamado);

        chamado.setTitulo(dto.titulo());
        chamado.setDescricao(dto.descricao());
        chamado.setPrioridade(dto.prioridade());
        chamado.setCliente(buscarCliente(dto.clienteId()));
        chamado.setCategoria(buscarCategoria(dto.categoriaId()));
        chamado.setTecnico(dto.tecnicoId() != null ? buscarTecnico(dto.tecnicoId()) : null);

        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscarEntity(id));
    }

    private Chamado buscarEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado não encontrado com o id " + id));
    }

    private Cliente buscarCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado com o id " + id));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada com o id " + id));
    }

    private Tecnico buscarTecnico(Long id) {
        return tecnicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Técnico não encontrado com o id " + id));
    }

    @Transactional
    public ChamadoResponse atribuir(Long id, AtribuirTecnicoRequest dto) {
        Chamado chamado = buscarEntity(id);
        garantirNaoFechado(chamado);
        chamado.setTecnico(buscarTecnico(dto.tecnicoId()));
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public ChamadoResponse alterarStatus(Long id, AlterarStatusRequest dto) {
        Chamado chamado = buscarEntity(id);
        garantirNaoFechado(chamado);

        StatusChamado atual = chamado.getStatus();
        StatusChamado novo = dto.status();
            if (!transicaoPermitida(atual, novo)) {
                throw new RegraNegocioException(
                "Transição de " + atual + " para " + novo + " não é permitida");
            }

         chamado.setStatus(novo);
            if (novo == StatusChamado.FECHADO) {
                chamado.setDataFechamento(LocalDateTime.now());
                }
        return ChamadoResponse.de(chamado);
}

    private void garantirNaoFechado(Chamado chamado) {
        if (chamado.getStatus() == StatusChamado.FECHADO) {
            throw new RegraNegocioException("Chamado fechado não pode ser alterado");
        }
    }

    private boolean transicaoPermitida(StatusChamado atual, StatusChamado novo) {
    return switch (atual) {
        case ABERTO -> novo == StatusChamado.EM_ATENDIMENTO || novo == StatusChamado.FECHADO;
        case EM_ATENDIMENTO -> novo == StatusChamado.AGUARDANDO_CLIENTE || novo == StatusChamado.FECHADO;
        case AGUARDANDO_CLIENTE -> novo == StatusChamado.EM_ATENDIMENTO || novo == StatusChamado.FECHADO;
        case FECHADO -> false;
    };
}
}