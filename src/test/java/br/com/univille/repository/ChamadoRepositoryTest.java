package br.com.univille.repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import br.com.univille.entity.Categoria;
import br.com.univille.entity.Chamado;
import br.com.univille.entity.Cliente;
import br.com.univille.entity.Comentario;
import br.com.univille.entity.Prioridade;
import br.com.univille.entity.StatusChamado;

@DataJpaTest
class ChamadoRepositoryTest {

    @Autowired
    private ChamadoRepository repository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private ComentarioRepository comentarioRepository;

    private Cliente ana;
    private Cliente bruno;
    private Categoria categoria;
    private Chamado chamado1;

    @BeforeEach
    void preparar() {
        ana = clienteRepository.save(new Cliente("Ana Souza", "ana@exemplo.com", "47999999999"));
        bruno = clienteRepository.save(new Cliente("Bruno Alves", "bruno@exemplo.com", "47988888888"));
        // ajuste esta linha se o construtor da sua Categoria for diferente
        categoria = categoriaRepository.save(new Categoria("Hardware", "Problemas com equipamentos"));

        chamado1 = novoChamado("Chamado 1", Prioridade.ALTA, StatusChamado.ABERTO, ana);
        novoChamado("Chamado 2", Prioridade.BAIXA, StatusChamado.ABERTO, bruno);
        novoChamado("Chamado 3", Prioridade.ALTA, StatusChamado.FECHADO, ana);
    }

    private Chamado novoChamado(String titulo, Prioridade prioridade, StatusChamado status, Cliente cliente) {
        Chamado c = new Chamado(titulo, "descricao", prioridade, cliente, categoria);
        c.setStatus(status);
        c.setDataAbertura(LocalDateTime.now());
        return repository.save(c);
    }

    @Test
    void filtrarSemFiltrosDeveRetornarTodos() {
        Page<Chamado> pagina = repository.filtrar(null, null, null, PageRequest.of(0, 10));

        assertEquals(3, pagina.getTotalElements());
    }

    @Test
    void filtrarPorStatusDeveRetornarSomenteOsDaquelaSituacao() {
        Page<Chamado> pagina = repository.filtrar(StatusChamado.ABERTO, null, null, PageRequest.of(0, 10));

        assertEquals(2, pagina.getTotalElements());
    }

    @Test
    void filtrarCombinandoStatusEPrioridade() {
        Page<Chamado> pagina = repository.filtrar(StatusChamado.ABERTO, Prioridade.ALTA, null, PageRequest.of(0, 10));

        assertEquals(1, pagina.getTotalElements());
        assertEquals("Chamado 1", pagina.getContent().get(0).getTitulo());
    }

    @Test
    void filtrarPorClienteDeveRetornarSomenteOsDele() {
        Page<Chamado> pagina = repository.filtrar(null, null, ana.getId(), PageRequest.of(0, 10));

        assertEquals(2, pagina.getTotalElements());
    }

    @Test
    void filtrarDevePaginar() {
        Page<Chamado> pagina = repository.filtrar(null, null, null, PageRequest.of(0, 1));

        assertEquals(1, pagina.getContent().size());
        assertEquals(3, pagina.getTotalElements());
        assertEquals(3, pagina.getTotalPages());
    }

    @Test
    void comentariosDevemVirEmOrdemCronologica() {
        Comentario recente = new Comentario("Segundo comentário", "Carlos", chamado1);
        recente.setData(LocalDateTime.now());
        Comentario antigo = new Comentario("Primeiro comentário", "Carlos", chamado1);
        antigo.setData(LocalDateTime.now().minusHours(2));

        // salva o mais recente primeiro, para provar que a ordenação vem da consulta
        comentarioRepository.save(recente);
        comentarioRepository.save(antigo);

        List<Comentario> lista = comentarioRepository.findByChamadoIdOrderByDataAsc(chamado1.getId());

        assertEquals(2, lista.size());
        assertEquals("Primeiro comentário", lista.get(0).getTexto());
        assertEquals("Segundo comentário", lista.get(1).getTexto());
    }
}