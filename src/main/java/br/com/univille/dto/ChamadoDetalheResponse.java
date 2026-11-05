package br.com.univille.dto;

import java.time.LocalDateTime;
import java.util.List;

import br.com.univille.entity.Chamado;
import br.com.univille.entity.Prioridade;
import br.com.univille.entity.StatusChamado;

public record ChamadoDetalheResponse(
    Long id,
    String titulo,
    String descricao,
    LocalDateTime dataAbertura,
    LocalDateTime dataFechamento,
    Prioridade prioridade,
    StatusChamado status,
    Long clienteId,
    String clienteNome,
    Long categoriaId,
    String categoriaNome,
    Long tecnicoId,
    String tecnicoNome,
    List<ComentarioResponse> comentarios
) {
    public static ChamadoDetalheResponse de(Chamado c, List<ComentarioResponse> comentarios) {
        return new ChamadoDetalheResponse(
            c.getId(),
            c.getTitulo(),
            c.getDescricao(),
            c.getDataAbertura(),
            c.getDataFechamento(),
            c.getPrioridade(),
            c.getStatus(),
            c.getCliente().getId(),
            c.getCliente().getNome(),
            c.getCategoria().getId(),
            c.getCategoria().getNome(),
            c.getTecnico() != null ? c.getTecnico().getId() : null,
            c.getTecnico() != null ? c.getTecnico().getNome() : null,
            comentarios
        );
    }
}