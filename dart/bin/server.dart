import 'dart:io';

import 'package:rinha_de_backend_2024_q1_dart/database.dart';
import 'package:rinha_de_backend_2024_q1_dart/environment.dart';
import 'package:rinha_de_backend_2024_q1_dart/statement.dart' as s;
import 'package:rinha_de_backend_2024_q1_dart/transaction.dart' as t;

bool isServerRunning = false;
void main(List<String> args) async {
  final server = await HttpServer.bind(
    InternetAddress.anyIPv4,
    Environment.port,
  );
  print('Server running on port ${Environment.port}');

  final limits = await Database.getLimits();
  print('Limits: $limits');

  server.listen((request) {
    print(
      '${DateTime.now().toIso8601String()} | [${request.method}] | ${request.uri}',
    );

    final pathSegments = request.uri.pathSegments;
    if (pathSegments.length >= 3 && pathSegments[0] == 'clientes') {
      switch (pathSegments[2]) {
        case 'transacoes':
          t.Transaction.createTransaction(request, limits);
          break;
        case 'extrato':
          s.Statement.getStatement(request, limits);
          break;
        default:
          request.response.statusCode = HttpStatus.notFound;
          request.response.close();
          break;
      }
    } else {
      request.response.statusCode = HttpStatus.notFound;
      request.response.close();
    }
  });
}
