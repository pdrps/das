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

import br.com.univille.entity.Perfil;
import br.com.univille.entity.Tecnico;

@DataJpaTest
class TecnicoRepositoryTest {

    @Autowired
    private TecnicoRepository repository;

    @Test
    void deveSalvarTecnicoComPerfilEGerarId() {
        Tecnico salvo = repository.save(new Tecnico("Carlos Lima", "carlos@exemplo.com", Perfil.ADMIN));

        assertNotNull(salvo.getId());
        assertEquals(Perfil.ADMIN, salvo.getPerfil());
    }

    @Test
    void existsByEmailDeveRetornarVerdadeiroSomenteParaEmailSalvo() {
        repository.save(new Tecnico("Carlos Lima", "carlos@exemplo.com", Perfil.TECNICO));

        assertTrue(repository.existsByEmail("carlos@exemplo.com"));
        assertFalse(repository.existsByEmail("outro@exemplo.com"));
    }

    @Test
    void existsByEmailAndIdNotDeveIgnorarOProprioTecnico() {
        Tecnico carlos = repository.save(new Tecnico("Carlos Lima", "carlos@exemplo.com", Perfil.TECNICO));
        Tecnico paula = repository.save(new Tecnico("Paula Admin", "paula@exemplo.com", Perfil.ADMIN));

        assertFalse(repository.existsByEmailAndIdNot("carlos@exemplo.com", carlos.getId()));
        assertTrue(repository.existsByEmailAndIdNot("carlos@exemplo.com", paula.getId()));
    }

    @Test
    void deveRejeitarEmailDuplicadoPeloBanco() {
        repository.saveAndFlush(new Tecnico("Carlos Lima", "carlos@exemplo.com", Perfil.TECNICO));

        assertThrows(DataIntegrityViolationException.class, () ->
            repository.saveAndFlush(new Tecnico("Outro Carlos", "carlos@exemplo.com", Perfil.ADMIN)));
    }

    @Test
    void deveRejeitarTecnicoSemPerfilPeloBanco() {
        assertThrows(DataIntegrityViolationException.class, () ->
            repository.saveAndFlush(new Tecnico("Sem Perfil", "semperfil@exemplo.com", null)));
    }
}