package aulas.umc.frota.model;

public enum Status {
    ATIVO((short) 1),
    INATIVO((short) 2),
    EXCLUIDO((short) 3);

    private final short codigo;

    Status(short codigo) {
        this.codigo = codigo;
    }

    public short getCodigo() {
        return codigo;
    }

    public static Status deCodigo(short codigo) {
        for (Status status : values()) {
            if (status.codigo == codigo) {
                return status;
            }
        }
        throw new IllegalArgumentException("Status invalido: " + codigo);
    }
}
