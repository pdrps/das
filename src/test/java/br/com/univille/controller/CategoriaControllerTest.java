package br.com.univille.controller;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.univille.dto.CategoriaRequest;
import br.com.univille.dto.CategoriaResponse;
import br.com.univille.exception.RecursoNaoEncontradoException;
import br.com.univille.exception.RegraNegocioException;
import br.com.univille.service.CategoriaService;

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService service;

    private static final String JSON_VALIDO = """
        {"nome": "Hardware", "descricao": "Problemas com equipamentos físicos"}
        """;

    @Test
    void criarDeveRetornar201() throws Exception {
        when(service.criar(any(CategoriaRequest.class)))
            .thenReturn(new CategoriaResponse(1L, "Hardware", "Problemas com equipamentos físicos"));

        mockMvc.perform(post("/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_VALIDO))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/categorias/1"))
            .andExpect(jsonPath("$.nome").value("Hardware"));
    }

    @Test
    void criarSemNomeESemDescricaoDeveRetornar400() throws Exception {
        mockMvc.perform(post("/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"","descricao":"Problemas com equipamentos físicos"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }

    @Test
    void criarComDescricaoMuitoLongaDeveRetornar400() throws Exception {
        mockMvc.perform(post("/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Hardware","descricao":"Descrição muuuuuuuuuuuuuuuuuuuuuuuuuuuuuuito longa que exceeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeede o limite peeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeermitido"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }

    @Test
    void criarComDescricaoDuplicadaDeveRetornar409() throws Exception {
        when(service.criar(any(CategoriaRequest.class)))
            .thenThrow(new RegraNegocioException("Já existe categoria com o nome Hardware"));

        mockMvc.perform(post("/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_VALIDO))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void buscarInexistenteDeveRetornar404() throws Exception {
        when(service.buscar(99L))
            .thenThrow(new RecursoNaoEncontradoException("Categoria não encontrada com o id 99"));

        mockMvc.perform(get("/categorias/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }
}