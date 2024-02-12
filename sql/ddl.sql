CREATE TABLE IF NOT EXISTS clientes (
  id SERIAL PRIMARY KEY,
  limite INTEGER NOT NULL,
  saldo INTEGER NOT NULL DEFAULT 0
) WITH (autovacuum_enabled = false);

CREATE TABLE IF NOT EXISTS transacoes (
  id SERIAL PRIMARY KEY NOT NULl,
  cliente_id INTEGER NOT NULL,
  valor INTEGER NOT NULL,
  tipo CHAR(1)  NOT NULL,
  descricao VARCHAR(10) NOT NULL,
  realizada_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cliente_id) REFERENCES clientes (id)
) WITH (autovacuum_enabled = false);

CREATE INDEX IF NOT EXISTS idx_transacoes_cliente_id ON transacoes (cliente_id);

INSERT INTO clientes (id, limite)
VALUES
(1, 1000 * 100),
(2, 800 * 100),
(3, 10000 * 100),
(4, 100000 * 100),
(5, 5000 * 100)
ON CONFLICT DO NOTHING;

--CREATE OR REPLACE FUNCTION atualiza_saldo()
--RETURNS TRIGGER AS $$
--DECLARE
--    saldo_atual INTEGER;
--    limite_atual INTEGER;
--BEGIN
--    SELECT saldo, limite
--      INTO saldo_atual, limite_atual
--      FROM clientes
--     WHERE id = NEW.cliente_id
--       FOR UPDATE;
--
--    IF NEW.tipo = 'd' AND (saldo_atual - NEW.valor) < -limite_atual THEN
--        RAISE EXCEPTION 'Saldo insuficiente para a transação de ID %', NEW.id;
--    END IF;
--
--    IF NEW.tipo = 'd' THEN
--        UPDATE clientes SET saldo = saldo - NEW.valor WHERE id = NEW.cliente_id;
--    ELSE
--        UPDATE clientes SET saldo = saldo + NEW.valor WHERE id = NEW.cliente_id;
--    END IF;
--
--    RETURN NEW;
--END;
--$$ LANGUAGE plpgsql;
--
--CREATE TRIGGER atualiza_saldo_trigger
--AFTER INSERT ON transacoes
--FOR EACH ROW
--EXECUTE FUNCTION atualiza_saldo();