package aulas.umc.frota.model;

import java.util.Arrays;

final class Enums {

    private Enums() {
    }

    /** Converte texto em enum com mensagem amigavel, aceitando minusculas e espacos. */
    static <E extends Enum<E>> E de(Class<E> tipo, String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " obrigatorio.");
        }
        try {
            return Enum.valueOf(tipo, valor.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(campo + " invalido: " + valor
                    + ". Valores aceitos: " + Arrays.toString(tipo.getEnumConstants()));
        }
    }
}
