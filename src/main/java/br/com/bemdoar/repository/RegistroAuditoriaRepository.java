package br.com.bemdoar.repository;

import br.com.bemdoar.entity.RegistroAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, Long> {
    List<RegistroAuditoria> findByTipoRegistroAndRegistroIdOrderByDataHoraAsc(String tipoRegistro, Long registroId);
}
