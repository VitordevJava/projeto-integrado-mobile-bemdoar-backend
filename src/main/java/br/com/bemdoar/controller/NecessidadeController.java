package br.com.bemdoar.controller;

import br.com.bemdoar.dto.NecessidadeRequest;
import br.com.bemdoar.dto.NecessidadeResponse;
import br.com.bemdoar.dto.NecessidadeAdminResponse;
import br.com.bemdoar.dto.AuditoriaResponse;
import br.com.bemdoar.enums.Prioridade;
import br.com.bemdoar.enums.SituacaoNecessidade;
import br.com.bemdoar.service.NecessidadeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import br.com.bemdoar.security.UsuarioAutenticado;
import java.util.List;

/** RF06 a RF10. */
@RestController
@RequestMapping("/api/necessidades")
public class NecessidadeController {

    private final NecessidadeService necessidadeService;

    public NecessidadeController(NecessidadeService necessidadeService) {
        this.necessidadeService = necessidadeService;
    }

    /**
     * GET /api/necessidades?termo=cobertor&categoriaId=1&prioridade=ALTA&situacao=ABERTA&pagina=0
     * PUBLICO (RF07). Todos os parametros sao opcionais.
     */
    @GetMapping
    public ResponseEntity<Page<NecessidadeResponse>> buscar(
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam(required = false) SituacaoNecessidade situacao,
            @RequestParam(defaultValue = "0") int pagina) {

        return ResponseEntity.ok(
                necessidadeService.buscar(termo, categoriaId, prioridade, situacao, pagina));
    }

    /** GET /api/necessidades/1 - PUBLICO */
    @GetMapping("/{id}")
    public ResponseEntity<NecessidadeResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(necessidadeService.buscarPorId(id));
    }

    @GetMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public NecessidadeAdminResponse buscarAdmin(@PathVariable Long id,
                                                 @AuthenticationPrincipal UsuarioAutenticado logado) {
        return necessidadeService.buscarAdmin(id, logado.getId());
    }

    @GetMapping("/{id}/auditoria")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<AuditoriaResponse> historico(@PathVariable Long id,
                                              @AuthenticationPrincipal UsuarioAutenticado logado) {
        return necessidadeService.historico(id, logado.getId());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<NecessidadeResponse> criar(@RequestBody @Valid NecessidadeRequest request,
                                                      @AuthenticationPrincipal UsuarioAutenticado logado) {
        return ResponseEntity.status(HttpStatus.CREATED).body(necessidadeService.criar(request, logado.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<NecessidadeResponse> atualizar(@PathVariable Long id,
                                                         @RequestBody @Valid NecessidadeRequest request,
                                                         @AuthenticationPrincipal UsuarioAutenticado logado) {
        return ResponseEntity.ok(necessidadeService.atualizar(id, request, logado.getId()));
    }

    @PatchMapping("/{id}/encerrar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<NecessidadeResponse> encerrar(@PathVariable Long id,
                                                        @AuthenticationPrincipal UsuarioAutenticado logado) {
        return ResponseEntity.ok(necessidadeService.encerrar(id, logado.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> excluir(@PathVariable Long id,
                                        @AuthenticationPrincipal UsuarioAutenticado logado) {
        necessidadeService.excluir(id, logado.getId());
        return ResponseEntity.noContent().build();
    }
}
