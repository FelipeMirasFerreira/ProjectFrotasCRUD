package aulas.umc.frota.model;

import java.util.UUID;

public abstract class Domain {
    protected final UUID id;
    protected Status status;

    protected Domain(UUID id, Status status) {
        this.id = id != null ? id : UUID.randomUUID();
        this.status = status != null ? status : Status.ATIVO;
    }

    public UUID getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }
}
