CREATE TABLE IF NOT EXISTS clientes (
  id SMALLINT PRIMARY KEY,
  limite INT NOT NULL,
  saldo INT NOT NULL DEFAULT 0
);

CREATE INDEX pk_cliente_idx ON clientes (id) INCLUDE (saldo);

INSERT INTO clientes (id, limite)
VALUES (1, 1000 * 100),
       (2, 800 * 100),
       (3, 10000 * 100),
       (4, 100000 * 100),
       (5, 5000 * 100)
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS transacoes (
  cliente_id SMALLINT NOT NULL,
  valor INT NOT NULL,
  tipo CHAR(1) NOT NULL,
  descricao VARCHAR(10) NOT NULL,
  realizada_em TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_transacoes_cliente_realizada_em ON transacoes (cliente_id, realizada_em DESC);

CREATE TYPE adiciona_transacao_retorno AS (saldo INT, limite INT);

/**
  * Adiciona uma transação para um cliente.
  * 
  * @param {SMALLINT}    idCliente - ID do cliente.
  * @param {INT}         valor     - Valor da transação. Deve ser positivo para crédito e negativo para débito.
  * @param {CHAR(1)}     tipo      - Tipo da transação (C para crédito e D para débito).
  * @param {VARCHAR(10)} descricao - Descrição da transação.
  * @returns {TABLE}               - Retorna o novo saldo e o limite do cliente.
  */
CREATE OR REPLACE FUNCTION adiciona_transacao(idCliente SMALLINT, valor INT, tipo CHAR(1), descricao VARCHAR(10))
RETURNS adiciona_transacao_retorno AS $$
DECLARE payload adiciona_transacao_retorno;
BEGIN

   UPDATE clientes
      SET saldo = saldo + valor
    WHERE id = idCliente
      AND (valor > 0 OR saldo + valor >= -limite)
RETURNING saldo, limite
     INTO payload;

  IF NOT FOUND THEN
    SELECT -1, -1 INTO payload;
    RETURN payload;
  END IF;

  INSERT INTO transacoes (cliente_id, valor, tipo, descricao, realizada_em)
  VALUES (idCliente, valor, tipo, descricao, NOW() AT TIME ZONE 'utc');

  RETURN payload;

END;
$$ LANGUAGE plpgsql;
