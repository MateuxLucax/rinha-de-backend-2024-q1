import 'package:postgres/postgres.dart';
import 'package:rinha_de_backend_2024_q1_dart/environment.dart';

class Database {
  static final Pool pool = Pool.withEndpoints(
    [
      Endpoint(
        host: Environment.databaseHost,
        database: Environment.databaseName,
        username: Environment.databaseUsername,
        password: Environment.databasePassword,
      )
    ],
    settings: PoolSettings(
      sslMode: SslMode.disable,
      applicationName: 'rinha_de_backend_2024_q1_dart',
      connectTimeout: Duration(seconds: Environment.connectTimeout),
      queryTimeout: Duration(seconds: Environment.queryTimeout),
      maxConnectionCount: Environment.maxConnectionCount,
    ),
  );
}
