-- Script idempotente: executado a cada inicializacao (spring.sql.init.mode=always).
-- Compativel com PostgreSQL e H2 (modo PostgreSQL).
-- status: 1 = ativo, 2 = inativo, 3 = excluido (exclusao logica, como no projeto base)

CREATE TABLE IF NOT EXISTS veiculo (
  id UUID PRIMARY KEY,
  placa VARCHAR(7) NOT NULL,
  marca VARCHAR(60) NOT NULL,
  modelo VARCHAR(80) NOT NULL,
  ano INTEGER NOT NULL,
  quilometragem_atual BIGINT NOT NULL CHECK (quilometragem_atual >= 0),
  intervalo_revisao_km INTEGER NOT NULL CHECK (intervalo_revisao_km > 0),
  km_ultima_revisao BIGINT NOT NULL CHECK (km_ultima_revisao >= 0),
  data_ultima_revisao DATE,
  status SMALLINT NOT NULL DEFAULT 1 CHECK (status IN (1,2,3)),
  criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS motorista (
  id UUID PRIMARY KEY,
  nome VARCHAR(120) NOT NULL,
  cnh VARCHAR(11) NOT NULL,
  categoria_cnh VARCHAR(2) NOT NULL,
  validade_cnh DATE NOT NULL,
  telefone VARCHAR(20),
  status SMALLINT NOT NULL DEFAULT 1 CHECK (status IN (1,2,3)),
  criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS abastecimento (
  id UUID PRIMARY KEY,
  veiculo_id UUID NOT NULL REFERENCES veiculo(id),
  motorista_id UUID NOT NULL REFERENCES motorista(id),
  data DATE NOT NULL,
  quilometragem BIGINT NOT NULL CHECK (quilometragem >= 0),
  litros NUMERIC(10,3) NOT NULL CHECK (litros > 0),
  valor_total NUMERIC(12,2) NOT NULL CHECK (valor_total > 0),
  combustivel VARCHAR(10) NOT NULL,
  status SMALLINT NOT NULL DEFAULT 1 CHECK (status IN (1,2,3)),
  criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS manutencao (
  id UUID PRIMARY KEY,
  veiculo_id UUID NOT NULL REFERENCES veiculo(id),
  data DATE NOT NULL,
  quilometragem BIGINT NOT NULL CHECK (quilometragem >= 0),
  tipo VARCHAR(12) NOT NULL,
  descricao VARCHAR(500) NOT NULL,
  custo NUMERIC(12,2) NOT NULL CHECK (custo >= 0),
  oficina VARCHAR(120),
  status SMALLINT NOT NULL DEFAULT 1 CHECK (status IN (1,2,3)),
  criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_veiculo_placa ON veiculo(placa);
CREATE INDEX IF NOT EXISTS idx_motorista_cnh ON motorista(cnh);
CREATE INDEX IF NOT EXISTS idx_abastecimento_veiculo ON abastecimento(veiculo_id);
CREATE INDEX IF NOT EXISTS idx_manutencao_veiculo ON manutencao(veiculo_id);
