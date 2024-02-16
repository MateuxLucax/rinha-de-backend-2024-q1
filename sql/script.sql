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

CREATE INDEX ON transacoes (cliente_id, realizada_em DESC);

CREATE OR REPLACE FUNCTION update_saldo_cliente(id INT, valor INT, tipo VARCHAR, descricao VARCHAR)
RETURNS TABLE(new_saldo INT, limite INT) AS $$
DECLARE
  saldo INTEGER;
  limite INTEGER;
  new_saldo INTEGER;
BEGIN
  SELECT c.saldo, c.limite INTO saldo, limite
  FROM clientes c
  WHERE c.id = update_saldo_cliente.id FOR UPDATE;

  IF NOT FOUND THEN
    RAISE EXCEPTION 'Cliente with ID % does not exist', id USING ERRCODE = 'P0002';
  END IF;

  IF update_saldo_cliente.tipo = 'd' THEN
    new_saldo := saldo - update_saldo_cliente.valor;
    IF new_saldo + limite < 0 THEN
      RAISE EXCEPTION 'Updating saldo failed: new saldo exceeds the limit' USING ERRCODE = 'P0000';
    END IF;
  ELSE
    new_saldo := saldo + update_saldo_cliente.valor;
  END IF;

  UPDATE clientes c SET saldo = new_saldo WHERE c.id = update_saldo_cliente.id;

  INSERT INTO transacoes (cliente_id, tipo, valor, descricao, realizada_em)
  VALUES (
    update_saldo_cliente.id,
    update_saldo_cliente.tipo,
    update_saldo_cliente.valor,
    update_saldo_cliente.descricao,
    CURRENT_TIMESTAMP
  );

  RETURN QUERY SELECT new_saldo, limite;
END;
$$ LANGUAGE plpgsql;