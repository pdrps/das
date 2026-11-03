package br.com.univille.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.univille.entity.Chamado;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
}