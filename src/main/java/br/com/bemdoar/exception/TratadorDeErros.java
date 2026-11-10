package br.com.bemdoar.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Traduz excecoes em respostas HTTP com mensagem legivel.
 *
 * RNF09 - o usuario nunca ve stack trace, SQL ou detalhe interno.
 *
 * Esta classe ja esta pronta. NINGUEM precisa alterar este arquivo.
 */
@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, Object>> regraNegocio(RegraNegocioException ex) {
        return montar(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> naoEncontrado(RecursoNaoEncontradoException ex) {
        return montar(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> acessoNegado(AccessDeniedException ex) {
        return montar(HttpStatus.FORBIDDEN, "Voce nao possui permissao para esta operacao.");
    }

    /** Erros das anotacoes @NotBlank, @Positive, @Email etc. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new HashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.put(erro.getField(), erro.getDefaultMessage());
        }
        Map<String, Object> corpo = base(HttpStatus.BAD_REQUEST, "Verifique os campos informados.");
        corpo.put("campos", campos);
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> corpoInvalido(HttpMessageNotReadableException ex) {
        return montar(HttpStatus.BAD_REQUEST, "Corpo da requisicao ausente ou invalido.");
    }

    /** Rede de seguranca: qualquer erro nao previsto vira 500 sem vazar detalhe. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> generico(Exception ex) {
        ex.printStackTrace(); // fica no terminal do desenvolvedor, nao na resposta
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Nao foi possivel concluir a operacao.");
    }

    private ResponseEntity<Map<String, Object>> montar(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(base(status, mensagem));
    }

    private Map<String, Object> base(HttpStatus status, String mensagem) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("momento", LocalDateTime.now().toString());
        corpo.put("status", status.value());
        corpo.put("mensagem", mensagem);
        return corpo;
    }
}
