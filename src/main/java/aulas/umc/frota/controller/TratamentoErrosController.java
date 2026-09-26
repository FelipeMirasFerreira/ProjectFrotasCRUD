package aulas.umc.frota.controller;

import aulas.umc.frota.DTO.ErroResponse;
import aulas.umc.frota.exception.RecursoNaoEncontradoException;
import aulas.umc.frota.exception.RegraNegocioException;
import aulas.umc.frota.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Converte excecoes em respostas JSON com mensagem, em vez do 400 vazio do projeto base:
 * o frontend (e quem usa o Swagger) consegue mostrar o motivo do erro.
 */
@RestControllerAdvice
public class TratamentoErrosController {

    private static final Logger log = LoggerFactory.getLogger(TratamentoErrosController.class);

    /** Dado invalido (Value Objects e validacoes de campos). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> dadoInvalido(IllegalArgumentException ex) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException ex) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> jsonInvalido(HttpMessageNotReadableException ex) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Corpo da requisicao invalido. Confira o JSON, datas (aaaa-mm-dd), numeros e ids.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Parametro invalido: " + ex.getName() + " = " + ex.getValue());
    }

    @ExceptionHandler(RepositoryException.class)
    public ResponseEntity<ErroResponse> erroBanco(RepositoryException ex) {
        log.error("Erro de banco de dados", ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao acessar o banco de dados.");
    }

    private static ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(ErroResponse.de(status.value(), status.getReasonPhrase(), mensagem));
    }
}
