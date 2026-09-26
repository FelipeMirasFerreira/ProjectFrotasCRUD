package aulas.umc.frota.exception;

/** Violacao de uma regra do negocio (ex.: quilometragem menor que a atual). Vira HTTP 422. */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String message) {
        super(message);
    }
}
