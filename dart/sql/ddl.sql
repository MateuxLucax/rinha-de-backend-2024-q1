ALTER DATABASE rinha SET SYNCHRONOUS_COMMIT=OFF;
SET CLIENT_MIN_MESSAGES = WARNING;
SET ROW_SECURITY = OFF;

CREATE UNLOGGED TABLE IF NOT EXISTS clientes (
  id SMALLINT,
  limite INT NOT NULL,
  saldo INT DEFAULT 0
);
CREATE INDEX pk_cliente_idx ON clientes (id) INCLUDE (saldo);
CLUSTER clientes USING pk_cliente_idx;


INSERT INTO clientes (id, limite)
VALUES
(1, 1000 * 100),
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
    realizada_em BIGINT default (extract(epoch from now()) * 1000)
);

CREATE INDEX cliente_idx ON transacoes (cliente_id);
CREATE INDEX realizada_em_idx ON transacoes (realizada_em DESC);
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
    PERFORM pg_advisory_lock(cliente_id);

    UPDATE clientes
       SET saldo = saldo - valor
     WHERE id = cliente_id
       AND saldo - valor >= - limite_

    RETURNING saldo INTO novo_saldo;

    PERFORM pg_advisory_unlock(cliente_id);

    IF novo_saldo IS NULL THEN RETURN; END IF;

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

    INSERT INTO transacoes (cliente_id, valor, TIPO, descricao)
         VALUES (cliente_id, valor, 'c', descricao);

    PERFORM pg_advisory_lock(cliente_id);

    UPDATE clientes
       SET saldo = saldo + valor
     WHERE id = cliente_id

    RETURNING saldo INTO novo_saldo;
    PERFORM pg_advisory_unlock(cliente_id);

END;
$$;
