ALTER DATABASE rinha SET SYNCHRONOUS_COMMIT=OFF;
SET CLIENT_MIN_MESSAGES = WARNING;
SET ROW_SECURITY = OFF;

CREATE UNLOGGED TABLE IF NOT EXISTS clientes (
  id SMALLINT,
  limite INT NOT NULL,
  saldo INT NOT NULL DEFAULT 0
);

CREATE INDEX pk_cliente_idx ON clientes (id) INCLUDE (saldo);
CLUSTER clientes USING pk_cliente_idx;

INSERT INTO clientes (id, limite)
VALUES (1, 1000 * 100),
       (2, 800 * 100),
       (3, 10000 * 100),
       (4, 100000 * 100),
       (5, 5000 * 100)
ON CONFLICT DO NOTHING;

CREATE UNLOGGED TABLE IF NOT EXISTS transacoes (
  cliente_id SMALLINT NOT NULL,
  valor INT NOT NULL,
  tipo CHAR(1) NOT NULL,
  descricao VARCHAR(10) NOT NULL,
  realizada_em BIGINT DEFAULT (EXTRACT(epoch FROM NOW()))
);

CREATE INDEX cliente_idx ON transacoes (cliente_id);
CREATE INDEX realizada_em_idx ON transacoes (cliente_id, realizada_em DESC);
CREATE INDEX transacoes_covering_idx ON transacoes (cliente_id, realizada_em DESC, tipo, valor, descricao);
CLUSTER transacoes USING cliente_idx;

CREATE FUNCTION debitar(
  cliente_id SMALLINT,
  valor INT,
  descricao TEXT,
  limite_ INT,
  OUT novo_saldo INT)
  LANGUAGE plpgsql
AS $$
BEGIN

  PERFORM pg_advisory_lock(cliente_id * 10);

  UPDATE clientes
     SET saldo = saldo - valor
   WHERE id = cliente_id
     AND saldo + limite_ > valor
     RETURNING saldo INTO novo_saldo;

  PERFORM pg_advisory_unlock(cliente_id * 10);

  IF novo_saldo IS NULL THEN 
    RETURN;
  END IF;

  INSERT INTO transacoes (cliente_id, valor, tipo, descricao)
       VALUES (cliente_id, valor, 'd', descricao);

END;
$$;

CREATE FUNCTION creditar(
  cliente_id SMALLINT,
  valor INT,
  descricao TEXT,
  OUT novo_saldo INT)
  LANGUAGE plpgsql
AS $$
BEGIN

  PERFORM pg_advisory_lock(cliente_id * 10);

  UPDATE clientes
      SET saldo = saldo + valor
    WHERE id = cliente_id
  RETURNING saldo INTO novo_saldo;

  PERFORM pg_advisory_unlock(cliente_id * 10);

  INSERT INTO transacoes (cliente_id, valor, tipo, descricao)
       VALUES (cliente_id, valor, 'c', descricao);

END;
$$;
