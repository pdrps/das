package br.com.univille.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.univille.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long>{
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
}