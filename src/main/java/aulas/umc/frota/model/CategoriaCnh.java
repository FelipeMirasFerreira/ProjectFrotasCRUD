package aulas.umc.frota.model;

public enum CategoriaCnh {
    A, B, C, D, E, AB, AC, AD, AE;

    public static CategoriaCnh de(String valor) {
        return Enums.de(CategoriaCnh.class, valor, "Categoria da CNH");
    }
}
