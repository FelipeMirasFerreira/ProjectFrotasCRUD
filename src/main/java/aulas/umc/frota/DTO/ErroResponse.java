package aulas.umc.frota.DTO;

import java.time.OffsetDateTime;

public record ErroResponse(int status, String erro, String mensagem, OffsetDateTime timestamp) {

    public static ErroResponse de(int status, String erro, String mensagem) {
        return new ErroResponse(status, erro, mensagem, OffsetDateTime.now());
    }
}
