package br.com.univille.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.univille.dto.AlterarStatusRequest;
import br.com.univille.dto.ChamadoDetalheResponse;
import br.com.univille.dto.ChamadoRequest;
import br.com.univille.dto.ChamadoResponse;
import br.com.univille.dto.ComentarioRequest;
import br.com.univille.dto.ComentarioResponse;
import br.com.univille.entity.Prioridade;
import br.com.univille.entity.StatusChamado;
import br.com.univille.exception.RecursoNaoEncontradoException;
import br.com.univille.exception.RegraNegocioException;
import br.com.univille.service.ChamadoService;

@WebMvcTest(ChamadoController.class)
class ChamadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChamadoService service;

    private static final String JSON_VALIDO = """
        {"titulo":"Notebook não liga","descricao":"Sem sinal de energia",
         "prioridade":"ALTA","clienteId":1,"categoriaId":1}
        """;

    private ChamadoResponse resposta(StatusChamado status) {
        return new ChamadoResponse(1L, "Notebook não liga", "Sem sinal de energia",
            LocalDateTime.now(), null, Prioridade.ALTA, status,
            1L, "Ana Souza", 1L, "Hardware", null, null);
    }

    @Test
    void criarDeveRetornar201() throws Exception {
        when(service.criar(any(ChamadoRequest.class))).thenReturn(resposta(StatusChamado.ABERTO));

        mockMvc.perform(post("/chamados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_VALIDO))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/chamados/1"))
            .andExpect(jsonPath("$.status").value("ABERTO"));
    }

    @Test
    void criarComDadosInvalidosDeveRetornar400() throws Exception {
        mockMvc.perform(post("/chamados")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"titulo":"","descricao":"","clienteId":1}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }

    @Test
    void criarComClienteInexistenteDeveRetornar404() throws Exception {
        when(service.criar(any(ChamadoRequest.class)))
            .thenThrow(new RecursoNaoEncontradoException("Cliente não encontrado com o id 1"));

        mockMvc.perform(post("/chamados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_VALIDO))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void buscarDeveRetornarDetalheComComentarios() throws Exception {
        ComentarioResponse comentario =
            new ComentarioResponse(1L, "Verificando o equipamento", "Carlos Lima", LocalDateTime.now(), 1L);
        ChamadoDetalheResponse detalhe = new ChamadoDetalheResponse(1L, "Notebook não liga",
            "Sem sinal de energia", LocalDateTime.now(), null, Prioridade.ALTA, StatusChamado.ABERTO,
            1L, "Ana Souza", 1L, "Hardware", null, null, List.of(comentario));
        when(service.buscar(1L)).thenReturn(detalhe);

        mockMvc.perform(get("/chamados/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.comentarios[0].autor").value("Carlos Lima"));
    }

    @Test
    void buscarInexistenteDeveRetornar404() throws Exception {
        when(service.buscar(99L))
            .thenThrow(new RecursoNaoEncontradoException("Chamado não encontrado com o id 99"));

        mockMvc.perform(get("/chamados/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void alterarStatusDeChamadoFechadoDeveRetornar409() throws Exception {
        when(service.alterarStatus(eq(1L), any(AlterarStatusRequest.class)))
            .thenThrow(new RegraNegocioException("Chamado fechado não pode ser alterado"));

        mockMvc.perform(patch("/chamados/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status":"EM_ATENDIMENTO"}
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.erro").value("Chamado fechado não pode ser alterado"));
    }

    @Test
    void comentarDeveRetornar201() throws Exception {
        when(service.comentar(eq(1L), any(ComentarioRequest.class)))
            .thenReturn(new ComentarioResponse(5L, "Verificando o equipamento", "Carlos Lima",
                LocalDateTime.now(), 1L));

        mockMvc.perform(post("/chamados/1/comentarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"texto":"Verificando o equipamento","autor":"Carlos Lima"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/chamados/1/comentarios/5"));
    }

    @Test
    void comentarComDadosInvalidosDeveRetornar400() throws Exception {
        mockMvc.perform(post("/chamados/1/comentarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"texto":"","autor":""}
                    """))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void listarComFiltroDeStatusDeveRetornar200() throws Exception {
        when(service.listar(eq(StatusChamado.ABERTO), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(resposta(StatusChamado.ABERTO)), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/chamados").param("status", "ABERTO"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].status").value("ABERTO"));
    }

    @Test
    void listarComStatusForaDoEnumDeveRetornar400() throws Exception {
        mockMvc.perform(get("/chamados").param("status", "XPTO"))
            .andExpect(status().isBadRequest());
    }
}