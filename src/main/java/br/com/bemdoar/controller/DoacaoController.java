package br.com.bemdoar.controller;

import br.com.bemdoar.dto.*;
import br.com.bemdoar.enums.SituacaoDoacao;
import br.com.bemdoar.security.UsuarioAutenticado;
import br.com.bemdoar.service.DoacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/doacoes")
public class DoacaoController {
    private final DoacaoService service;

    public DoacaoController(DoacaoService service) { this.service = service; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public DoacaoResponse criar(@Valid @RequestBody DoacaoRequest request,
                                @AuthenticationPrincipal UsuarioAutenticado logado) {
        return service.criar(logado.getId(), request);
    }

    @GetMapping("/minhas")
    public List<DoacaoResponse> minhas(@AuthenticationPrincipal UsuarioAutenticado logado) {
        return service.listarMinhas(logado.getId());
    }

    @GetMapping("/protocolo/{protocolo}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public DoacaoResponse buscarProtocolo(@PathVariable String protocolo,
                                          @AuthenticationPrincipal UsuarioAutenticado logado) {
        return service.buscarProtocolo(protocolo, logado.getId());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<DoacaoResponse> listar(@RequestParam(required = false) SituacaoDoacao situacao,
                                       @AuthenticationPrincipal UsuarioAutenticado logado) {
        return service.listarTodas(situacao, logado.getId());
    }

    @GetMapping("/{id}")
    public DoacaoResponse buscar(@PathVariable Long id,
                                 @AuthenticationPrincipal UsuarioAutenticado logado) {
        return service.buscar(id, logado.getId());
    }

    @GetMapping("/{id}/auditoria")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<AuditoriaResponse> auditoria(@PathVariable Long id,
                                             @AuthenticationPrincipal UsuarioAutenticado logado) {
        return service.historico(id, logado.getId());
    }

    @PatchMapping("/{id}/confirmar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public DoacaoResponse confirmar(@PathVariable Long id,
                                    @AuthenticationPrincipal UsuarioAutenticado logado) {
        return service.confirmar(id, logado.getId());
    }

    @PatchMapping("/{id}/receber")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public DoacaoResponse receber(@PathVariable Long id, @Valid @RequestBody RecebimentoRequest request,
                                  @AuthenticationPrincipal UsuarioAutenticado logado) {
        return service.receber(id, logado.getId(), request);
    }

    @PatchMapping("/{id}/retificar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public DoacaoResponse retificar(@PathVariable Long id, @Valid @RequestBody RetificacaoRecebimentoRequest request,
                                    @AuthenticationPrincipal UsuarioAutenticado logado) {
        return service.retificar(id, logado.getId(), request);
    }

    @PatchMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public DoacaoResponse anular(@PathVariable Long id, @Valid @RequestBody AnulacaoRecebimentoRequest request,
                                 @AuthenticationPrincipal UsuarioAutenticado logado) {
        return service.anular(id, logado.getId(), request);
    }

    @PatchMapping("/{id}/cancelar")
    public DoacaoResponse cancelar(@PathVariable Long id, @Valid @RequestBody(required = false) CancelamentoRequest request,
                                   @AuthenticationPrincipal UsuarioAutenticado logado) {
        return service.cancelar(id, logado.getId(), request);
    }
}
