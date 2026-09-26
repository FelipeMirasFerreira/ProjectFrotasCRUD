package aulas.umc.frota.exception;

/** Registro inexistente ou excluido logicamente. Vira HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String message) {
        super(message);
    }
}
