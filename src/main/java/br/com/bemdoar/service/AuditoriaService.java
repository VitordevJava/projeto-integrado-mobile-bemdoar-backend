package br.com.bemdoar.service;

import br.com.bemdoar.dto.AuditoriaResponse;
import br.com.bemdoar.entity.RegistroAuditoria;
import br.com.bemdoar.entity.Usuario;
import br.com.bemdoar.repository.RegistroAuditoriaRepository;
import br.com.bemdoar.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class AuditoriaService {
    private final RegistroAuditoriaRepository repository;
    private final UsuarioRepository usuarioRepository;

    public AuditoriaService(RegistroAuditoriaRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void registrar(String tipo, Long registroId, String acao, Long responsavelId,
                         String justificativa, String anterior, String novo) {
        RegistroAuditoria evento = new RegistroAuditoria();
        evento.setTipoRegistro(tipo);
        evento.setRegistroId(registroId);
        evento.setAcao(acao);
        Usuario usuario = responsavelId == null ? null : usuarioRepository.findById(responsavelId).orElse(null);
        evento.setResponsavel(usuario);
        evento.setDataHora(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
        evento.setJustificativa(justificativa);
        evento.setValorAnterior(anterior);
        evento.setValorNovo(novo);
        repository.save(evento);
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponse> listar(String tipo, Long id) {
        return repository.findByTipoRegistroAndRegistroIdOrderByDataHoraAsc(tipo, id)
                .stream().map(AuditoriaResponse::de).toList();
    }
}
