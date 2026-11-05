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

import br.com.univille.dto.TecnicoRequest;
import br.com.univille.dto.TecnicoResponse;
import br.com.univille.entity.Perfil;
import br.com.univille.exception.RecursoNaoEncontradoException;
import br.com.univille.exception.RegraNegocioException;
import br.com.univille.service.TecnicoService;

@WebMvcTest(TecnicoController.class)
class TecnicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TecnicoService service;

    private static final String JSON_VALIDO = """
        {"nome":"Carlos Lima","email":"carlos@exemplo.com","perfil":"ADMIN"}
        """;

    @Test
    void criarDeveRetornar201() throws Exception {
        when(service.criar(any(TecnicoRequest.class)))
            .thenReturn(new TecnicoResponse(1L, "Carlos Lima", "carlos@exemplo.com", Perfil.ADMIN));

        mockMvc.perform(post("/tecnicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_VALIDO))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/tecnicos/1"))
            .andExpect(jsonPath("$.perfil").value("ADMIN"));
    }

    @Test
    void criarSemNomeESemPerfilDeveRetornar400() throws Exception {
        mockMvc.perform(post("/tecnicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"","email":"carlos@exemplo.com"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }

    @Test
    void criarComPerfilForaDoEnumDeveRetornar400() throws Exception {
        mockMvc.perform(post("/tecnicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Carlos Lima","email":"carlos@exemplo.com","perfil":"GERENTE"}
                    """))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void criarComEmailDuplicadoDeveRetornar409() throws Exception {
        when(service.criar(any(TecnicoRequest.class)))
            .thenThrow(new RegraNegocioException("Já existe técnico com o e-mail carlos@exemplo.com"));

        mockMvc.perform(post("/tecnicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_VALIDO))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void buscarInexistenteDeveRetornar404() throws Exception {
        when(service.buscar(99L))
            .thenThrow(new RecursoNaoEncontradoException("Técnico não encontrado com o id 99"));

        mockMvc.perform(get("/tecnicos/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }
}