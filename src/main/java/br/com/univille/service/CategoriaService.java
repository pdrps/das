package br.com.univille.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.univille.dto.CategoriaRequest;
import br.com.univille.dto.CategoriaResponse;
import br.com.univille.entity.Categoria;
import br.com.univille.exception.RecursoNaoEncontradoException;
import br.com.univille.exception.RegraNegocioException;
import br.com.univille.repository.CategoriaRepository;

@Service
public class CategoriaService {
    
    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    @Transactional 
    public CategoriaResponse criar(CategoriaRequest dto) {
        if (repository.existsByNome(dto.nome())) {
            throw new RegraNegocioException("Já existe categoria com o nome " + dto.nome());
        }
        Categoria salvo = repository.save(new Categoria(dto.nome(), dto.descricao()));
        return CategoriaResponse.de(salvo);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return repository.findAll().stream().map(CategoriaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscar(Long id) {
        return CategoriaResponse.de(buscarEntity(id));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest dto) {
        Categoria categoria = buscarEntity(id);
        if (repository.existsByNomeAndIdNot(dto.nome(), id)) {
            throw new RegraNegocioException("Já existe categoria com o nome " + dto.nome());
        }
        categoria.setNome(dto.nome());
        categoria.setDescricao(dto.descricao());
        return CategoriaResponse.de(categoria);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscarEntity(id));
    }

     private Categoria buscarEntity(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Categoria " + id + " não encontrada"));
    }
}
