package br.com.bemdoar.service;

import br.com.bemdoar.dto.*;
import br.com.bemdoar.entity.Doacao;
import br.com.bemdoar.entity.Necessidade;
import br.com.bemdoar.entity.Usuario;
import br.com.bemdoar.enums.PerfilUsuario;
import br.com.bemdoar.enums.SituacaoDoacao;
import br.com.bemdoar.exception.RecursoNaoEncontradoException;
import br.com.bemdoar.exception.RegraNegocioException;
import br.com.bemdoar.repository.DoacaoRepository;
import br.com.bemdoar.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DoacaoService {
    private final DoacaoRepository repository;
    private final UsuarioRepository usuarios;
    private final NecessidadeService necessidades;
    private final VencimentoDoacaoService vencimentos;
    private final AuditoriaService auditoria;
    private final Clock clock;

    public DoacaoService(DoacaoRepository repository, UsuarioRepository usuarios,
                         NecessidadeService necessidades, VencimentoDoacaoService vencimentos,
                         AuditoriaService auditoria, Clock clock) {
        this.repository = repository;
        this.usuarios = usuarios;
        this.necessidades = necessidades;
        this.vencimentos = vencimentos;
        this.auditoria = auditoria;
        this.clock = clock;
    }

    @Transactional
    public DoacaoResponse criar(Long usuarioId, DoacaoRequest request) {
        if (request.necessidadeId() == null || request.campanhaId() != null) {
            throw new RegraNegocioException("Nesta versao, a doacao deve estar vinculada a uma necessidade.");
        }
        Usuario usuario = usuario(usuarioId);
        if (!usuario.isAtivo()) throw new AccessDeniedException("Conta inativa.");
        Necessidade necessidade = necessidades.buscarDisponivelOuFalhar(request.necessidadeId());
        Doacao d = new Doacao();
        d.setUsuario(usuario);
        d.setNecessidade(necessidade);
        d.setItem(request.item().trim());
        d.setQuantidade(request.quantidade());
        d.setObservacao(limpar(request.observacao()));
        d.setFormaEntrega(request.formaEntrega());
        d.setSituacao(SituacaoDoacao.PENDENTE);
        d.setDataRegistro(LocalDateTime.now(clock));
        d.setPrazoAnalise(d.getDataRegistro().plusDays(3));
        // UUID completo: sem dados pessoais, com unicidade adicional no banco.
        d.setProtocolo("BD-" + UUID.randomUUID().toString().toUpperCase());
        return DoacaoResponse.de(repository.save(d));
    }

    @Transactional(readOnly = true)
    public List<DoacaoResponse> listarMinhas(Long usuarioId) {
        usuario(usuarioId);
        return repository.findByUsuarioIdOrderByDataRegistroDesc(usuarioId).stream().map(DoacaoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public DoacaoResponse buscar(Long id, Long usuarioId) {
        Doacao d = buscarEntidade(id);
        validarDonoOuAdmin(d, usuarioId);
        return DoacaoResponse.de(d);
    }

    @Transactional(readOnly = true)
    public List<DoacaoResponse> listarTodas(SituacaoDoacao situacao, Long adminId) {
        exigirAdmin(adminId);
        List<Doacao> lista = situacao == null
                ? repository.findAll(Sort.by(Sort.Direction.DESC, "dataRegistro"))
                : repository.findBySituacaoOrderByDataRegistroDesc(situacao);
        return lista.stream().map(DoacaoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public DoacaoResponse buscarProtocolo(String protocolo, Long adminId) {
        exigirAdmin(adminId);
        return DoacaoResponse.de(repository.findByProtocolo(protocolo.trim().toUpperCase())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Protocolo nao encontrado.")));
    }

    @Transactional
    public DoacaoResponse confirmar(Long id, Long adminId) {
        exigirAdmin(adminId);
        Doacao d = bloquear(id);
        exigirSituacao(d, SituacaoDoacao.PENDENTE);
        vencimentos.exigirNoPrazo(d);
        d.setSituacao(SituacaoDoacao.CONFIRMADA);
        d.setDataConfirmacao(LocalDateTime.now(clock));
        d.setPrazoEntrega(d.getDataConfirmacao().plusDays(15));
        return DoacaoResponse.de(repository.save(d));
    }

    @Transactional
    public DoacaoResponse receber(Long id, Long adminId, RecebimentoRequest request) {
        Usuario admin = exigirAdmin(adminId);
        Doacao d = bloquear(id);
        exigirSituacao(d, SituacaoDoacao.CONFIRMADA);
        vencimentos.exigirNoPrazo(d);
        if (!Boolean.TRUE.equals(request.itemConfere())) {
            throw new RegraNegocioException("Item divergente: cancele a intencao original e crie outra para uma necessidade valida.");
        }
        validarQuantidade(request.quantidadeRecebida(), d.getQuantidade());
        d.setQuantidadeRecebida(request.quantidadeRecebida());
        d.setSituacao(SituacaoDoacao.RECEBIDA);
        d.setDataRecebimento(LocalDateTime.now(clock));
        d.setResponsavelRecebimento(admin);
        repository.saveAndFlush(d);
        if (d.getNecessidade() != null) necessidades.recalcularProgresso(d.getNecessidade().getId());
        auditoria.registrar("DOACAO", id, "RECEBIMENTO", adminId, "Entrega fisica conferida",
                null, String.valueOf(d.getQuantidadeRecebida()));
        return DoacaoResponse.de(d);
    }

    @Transactional
    public DoacaoResponse retificar(Long id, Long adminId, RetificacaoRecebimentoRequest request) {
        exigirAdmin(adminId);
        Doacao d = bloquear(id);
        exigirSituacao(d, SituacaoDoacao.RECEBIDA);
        validarQuantidade(request.quantidadeRecebida(), d.getQuantidade());
        int anterior = d.getQuantidadeRecebida();
        d.setQuantidadeRecebida(request.quantidadeRecebida());
        repository.saveAndFlush(d);
        if (d.getNecessidade() != null) necessidades.recalcularProgresso(d.getNecessidade().getId());
        auditoria.registrar("DOACAO", id, "RETIFICACAO", adminId, request.justificativa().trim(),
                String.valueOf(anterior), String.valueOf(d.getQuantidadeRecebida()));
        return DoacaoResponse.de(d);
    }

    @Transactional
    public DoacaoResponse anular(Long id, Long adminId, AnulacaoRecebimentoRequest request) {
        exigirAdmin(adminId);
        Doacao d = bloquear(id);
        exigirSituacao(d, SituacaoDoacao.RECEBIDA);
        int anterior = d.getQuantidadeRecebida();
        d.setSituacao(SituacaoDoacao.CANCELADA);
        d.setMotivoCancelamento("RECEBIMENTO_INDEVIDO");
        repository.saveAndFlush(d);
        if (d.getNecessidade() != null) necessidades.recalcularProgresso(d.getNecessidade().getId());
        auditoria.registrar("DOACAO", id, "ANULACAO", adminId, request.justificativa().trim(),
                String.valueOf(anterior), "0");
        return DoacaoResponse.de(d);
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponse> historico(Long id, Long adminId) {
        exigirAdmin(adminId);
        buscarEntidade(id);
        return auditoria.listar("DOACAO", id);
    }

    @Transactional
    public DoacaoResponse cancelar(Long id, Long usuarioId, CancelamentoRequest request) {
        Doacao d = bloquear(id);
        Usuario solicitante = validarDonoOuAdmin(d, usuarioId);
        if (d.getSituacao() != SituacaoDoacao.PENDENTE && d.getSituacao() != SituacaoDoacao.CONFIRMADA) {
            transicaoInvalida();
        }
        String motivo = request == null ? null : limpar(request.motivo());
        if (solicitante.getPerfil() == PerfilUsuario.ADMINISTRADOR && motivo == null) {
            throw new RegraNegocioException("Informe o motivo do cancelamento.");
        }
        d.setMotivoCancelamento(motivo == null ? "CANCELADA_PELO_TITULAR" : motivo);
        d.setSituacao(SituacaoDoacao.CANCELADA);
        return DoacaoResponse.de(repository.save(d));
    }

    private Usuario usuario(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado."));
    }

    private Usuario exigirAdmin(Long id) {
        Usuario u = usuario(id);
        if (!u.isAtivo() || u.getPerfil() != PerfilUsuario.ADMINISTRADOR) {
            throw new AccessDeniedException("Acesso administrativo necessario.");
        }
        return u;
    }

    private Usuario validarDonoOuAdmin(Doacao d, Long usuarioId) {
        Usuario u = usuario(usuarioId);
        if (!u.isAtivo() || (u.getPerfil() != PerfilUsuario.ADMINISTRADOR
                && !d.getUsuario().getId().equals(usuarioId))) {
            throw new AccessDeniedException("Sem permissao.");
        }
        return u;
    }

    private Doacao buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Doacao nao encontrada."));
    }

    private Doacao bloquear(Long id) {
        return repository.bloquearPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Doacao nao encontrada."));
    }

    private void exigirSituacao(Doacao d, SituacaoDoacao situacao) {
        if (d.getSituacao() != situacao) transicaoInvalida();
    }

    private void transicaoInvalida() {
        throw new RegraNegocioException("Esta transicao nao e permitida.");
    }

    private void validarQuantidade(Integer recebida, Integer prometida) {
        if (recebida == null || recebida <= 0 || recebida > prometida) {
            throw new RegraNegocioException("Quantidade recebida deve ser positiva e nao superar a prometida.");
        }
    }

    private String limpar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
