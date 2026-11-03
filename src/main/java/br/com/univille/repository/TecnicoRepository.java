package br.com.univille.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.univille.entity.Tecnico;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
}
