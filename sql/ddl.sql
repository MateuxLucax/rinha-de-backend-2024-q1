--CREATE UNLOGGED TABLE IF NOT EXISTS clientes (
CREATE TABLE IF NOT EXISTS clientes (
  id SERIAL PRIMARY KEY,
  nome VARCHAR(255) NOT NULL,
  limite INTEGER NOT NULL,
  saldo INTEGER NOT NULL DEFAULT 0
);

--CREATE UNLOGGED TABLE IF NOT EXISTS transacoes (
CREATE TABLE IF NOT EXISTS transacoes (
  id SERIAL PRIMARY KEY,
  cliente_id INTEGER NOT NULL,
  valor INTEGER NOT NULL,
  tipo CHAR  NOT NULL,
  descricao VARCHAR(255) NOT NULL,
  realizada_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

CREATE INDEX IF NOT EXISTS idx_transacoes_cliente_id ON transacoes (cliente_id);

DO $$
BEGIN
  INSERT INTO clientes (id, nome, limite)
  VALUES
    (1, 'o barato sai caro', 1000 * 100),
    (2, 'zan corp ltda', 800 * 100),
    (3, 'les cruders', 10000 * 100),
    (4, 'padaria joia de cocaia', 100000 * 100),
    (5, 'kid mais', 5000 * 100)
  ON CONFLICT DO NOTHING;
END; $$