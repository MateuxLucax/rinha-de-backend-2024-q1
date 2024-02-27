import 'dart:io';

class Environment {
  static int port =
      int.tryParse(Platform.environment['port'] ?? '9999') ?? 9999;

  static String databaseHost =
      Platform.environment['databaseHost'] ?? "localhost";
  static String databaseName = Platform.environment['databaseName'] ?? "rinha";
  static String databaseUsername =
      Platform.environment['databaseUsername'] ?? "admin";
  static String databasePassword =
      Platform.environment['databasePassword'] ?? "123";
  static int connectTimeout =
      int.tryParse(Platform.environment['connectTimeout'] ?? '15') ?? 15;
  static int queryTimeout =
      int.tryParse(Platform.environment['queryTimeout'] ?? '30') ?? 30;
  static int maxConnectionCount =
      int.tryParse(Platform.environment['maxConnectionCount'] ?? '64') ?? 64;
}
