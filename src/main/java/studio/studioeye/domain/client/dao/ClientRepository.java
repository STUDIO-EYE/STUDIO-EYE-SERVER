package studio.studioeye.domain.client.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import studio.studioeye.domain.client.domain.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Page<Client> findAll(Pageable pageable);
}
