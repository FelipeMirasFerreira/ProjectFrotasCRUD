package aulas.umc.frota.model;

import aulas.umc.frota.model.valueObjects.Cnh;
import aulas.umc.frota.model.valueObjects.Nome;

import java.time.LocalDate;
import java.util.UUID;

public class Motorista extends Domain {
    private Nome nome;
    private Cnh cnh;
    private CategoriaCnh categoriaCnh;
    private LocalDate validadeCnh;
    private String telefone;

    public Motorista(Nome nome, Cnh cnh, CategoriaCnh categoriaCnh, LocalDate validadeCnh, String telefone) {
        this(null, nome, cnh, categoriaCnh, validadeCnh, telefone, Status.ATIVO);
    }

    public Motorista(UUID id, Nome nome, Cnh cnh, CategoriaCnh categoriaCnh, LocalDate validadeCnh,
                     String telefone, Status status) {
        super(id, status);
        alterarDados(nome, cnh, categoriaCnh, validadeCnh, telefone);
    }

    public void alterarDados(Nome nome, Cnh cnh, CategoriaCnh categoriaCnh, LocalDate validadeCnh, String telefone) {
        if (nome == null || cnh == null || categoriaCnh == null) {
            throw new IllegalArgumentException("Nome, CNH e categoria sao obrigatorios.");
        }
        if (validadeCnh == null) {
            throw new IllegalArgumentException("Validade da CNH obrigatoria.");
        }
        this.nome = nome;
        this.cnh = cnh;
        this.categoriaCnh = categoriaCnh;
        this.validadeCnh = validadeCnh;
        this.telefone = telefone != null && !telefone.isBlank() ? telefone.trim() : null;
    }

    /** A CNH vale ate o ultimo dia da validade (inclusive). */
    public boolean cnhVencidaEm(LocalDate data) {
        return data.isAfter(validadeCnh);
    }

    public boolean estaAtivo() {
        return status == Status.ATIVO;
    }

    public Nome getNome() {
        return nome;
    }

    public Cnh getCnh() {
        return cnh;
    }

    public CategoriaCnh getCategoriaCnh() {
        return categoriaCnh;
    }

    public LocalDate getValidadeCnh() {
        return validadeCnh;
    }

    public String getTelefone() {
        return telefone;
    }
}
