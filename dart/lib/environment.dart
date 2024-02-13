class Environment {
  static const int port = int.fromEnvironment(
    "PORT",
    defaultValue: 8080,
  );

  static const int isolates = int.fromEnvironment(
    "ISOLATES",
    defaultValue: 7,
  );

  static const String databaseHost = String.fromEnvironment(
    "DATABASE_HOST",
    defaultValue: "localhost",
  );
  static const String databaseName = String.fromEnvironment(
    "DATABASE_NAME",
    defaultValue: "rinha",
  );
  static const String databaseUsername = String.fromEnvironment(
    "DATABASE_USERNAME",
    defaultValue: "admin",
  );
  static const String databasePassword = String.fromEnvironment(
    "DATABASE_PASSWORD",
    defaultValue: "123",
  );
  static const int connectTimeout = int.fromEnvironment(
    "CONNECT_TIMEOUT",
    defaultValue: 15,
  );
  static const int queryTimeout = int.fromEnvironment(
    "QUERY_TIMEOUT",
    defaultValue: 30,
  );
  static const int maxConnectionCount = int.fromEnvironment(
    "MAX_CONNECTION_COUNT",
    defaultValue: 64,
  );
}
