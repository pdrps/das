package br.com.univille.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import br.com.univille.entity.Categoria;

@DataJpaTest
class CategoriaRepositoryTest {

    @Autowired
    private CategoriaRepository repository;

    @Test
    void deveSalvarCategoriaComNomeEDescricaoEGerarId() {
        Categoria salva = repository.save(new Categoria("Hardware", "Problemas com equipamentos físicos"));

        assertNotNull(salva.getId());
        assertEquals("Hardware", salva.getNome());
        assertEquals("Problemas com equipamentos físicos", salva.getDescricao());
    }

    @Test
    void existsByNomeDeveRetornarVerdadeiroSomenteParaNomeSalvo() {
        repository.save(new Categoria("Hardware", "Problemas com equipamentos físicos"));

        assertTrue(repository.existsByNome("Hardware"));
        assertFalse(repository.existsByNome("Periféricos"));
    }

    @Test
    void existsByNomeAndIdNotDeveIgnorarAPropriaCategoria() {
        Categoria categoria1 = repository.save(new Categoria("Hardware", "Problemas com equipamentos físicos"));
        Categoria categoria2 = repository.save(new Categoria("Software", "Problemas com software"));

        assertFalse(repository.existsByNomeAndIdNot("Hardware", categoria1.getId()));
        assertTrue(repository.existsByNomeAndIdNot("Hardware", categoria2.getId()));
    }

    @Test
    void deveRejeitarCategoriaSemDescricaoPeloBanco() {
        assertThrows(DataIntegrityViolationException.class, () ->
            repository.saveAndFlush(new Categoria("Sem Descrição", null)));
    }
}