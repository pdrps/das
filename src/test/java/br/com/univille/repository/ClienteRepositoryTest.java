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

import br.com.univille.entity.Cliente;

@DataJpaTest
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository repository;

    @Test
    void deveSalvarClienteEGerarId() {
        Cliente salvo = repository.save(new Cliente("Ana Souza", "ana@exemplo.com", "47999999999"));

        assertNotNull(salvo.getId());
        assertEquals("Ana Souza", salvo.getNome());
    }

    @Test
    void existsByEmailDeveRetornarVerdadeiroSomenteParaEmailSalvo() {
        repository.save(new Cliente("Ana Souza", "ana@exemplo.com", "47999999999"));

        assertTrue(repository.existsByEmail("ana@exemplo.com"));
        assertFalse(repository.existsByEmail("outro@exemplo.com"));
    }

    @Test
    void existsByEmailAndIdNotDeveIgnorarOProprioCliente() {
        Cliente ana = repository.save(new Cliente("Ana Souza", "ana@exemplo.com", "47999999999"));
        Cliente bruno = repository.save(new Cliente("Bruno Alves", "bruno@exemplo.com", "47988888888"));

        // o próprio e-mail da Ana não conta como duplicado para ela mesma
        assertFalse(repository.existsByEmailAndIdNot("ana@exemplo.com", ana.getId()));
        // mas conta como duplicado para o Bruno
        assertTrue(repository.existsByEmailAndIdNot("ana@exemplo.com", bruno.getId()));
    }

    @Test
    void deveRejeitarEmailDuplicadoPeloBanco() {
        repository.saveAndFlush(new Cliente("Ana Souza", "ana@exemplo.com", "47999999999"));

        assertThrows(DataIntegrityViolationException.class, () ->
            repository.saveAndFlush(new Cliente("Outra Ana", "ana@exemplo.com", "47977777777")));
    }
}