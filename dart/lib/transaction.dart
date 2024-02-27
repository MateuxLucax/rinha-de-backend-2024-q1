import 'dart:convert';
import 'dart:io';

import 'package:rinha_de_backend_2024_q1_dart/database.dart';
import 'package:rinha_de_backend_2024_q1_dart/response.dart';

class Transaction {
  static Future<void> createTransaction(HttpRequest request) async {
    final int id = int.tryParse(request.uri.pathSegments[1]) ?? 0;

    if (id > 5 || id < 1) {
      return Response.status(HttpStatus.notFound, request);
    }

    final List<int> bodyBytes = await request.fold<List<int>>(
      <int>[],
      (prev, chunk) => prev..addAll(chunk),
    );

    final String requestBody = utf8.decode(bodyBytes);
    final Map<String, dynamic> body = jsonDecode(requestBody);
    final String? type = body['tipo'];
    final dynamic value = body['valor'];
    final String? description = body['descricao'];

    if (type == null ||
        value == null ||
        description == null ||
        value is! int ||
        value < 1 ||
        description.isEmpty ||
        description.length > 10 ||
        (type != 'c' && type != 'd')) {
      return Response.status(HttpStatus.unprocessableEntity, request);
    }

    Database.pool.withConnection(
      (connection) async {
        final response = await connection.execute(
          "CALL adiciona_transacao($id::INT2, $value::INT4, ${type == 'c' ? value : -value}::INT4, '$type'::CHAR(1), '$description'::VARCHAR(10), NULL, NULL)",
        );

        return Response.json(
          {
            'saldo': response.first.first as int,
            'limite': response.first.last as int,
          },
          request,
        );
      },
    ).onError((_, __) {
      return Response.status(HttpStatus.unprocessableEntity, request);
    });
  }
}
