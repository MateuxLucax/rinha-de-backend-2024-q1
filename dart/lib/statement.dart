import 'dart:io';

import 'package:postgres/postgres.dart';
import 'package:rinha_de_backend_2024_q1_dart/database.dart';
import 'package:rinha_de_backend_2024_q1_dart/response.dart';

class Statement {
  static Future<void> getStatement(
    HttpRequest request,
    Map<int, int> limits,
  ) async {
    final int id = int.tryParse(request.uri.pathSegments[1]) ?? 0;

    if (id > 5 || id < 1) {
      return Response.status(HttpStatus.notFound, request);
    }

    Database.pool.withConnection((connection) async {
      final result = await Future.wait([
        connection.execute(
          Sql.named('SELECT saldo FROM clientes WHERE id = @id'),
          parameters: {
            'id': id,
          },
        ),
        connection.execute(
          Sql.named(
            'SELECT tipo, valor, descricao, realizada_em FROM transacoes WHERE cliente_id = @id ORDER BY realizada_em DESC LIMIT 10',
          ),
          parameters: {
            'id': id,
          },
        )
      ]);

      Response.json({
        'saldo': {
          'limite': limits[id] as int,
          'total': result.first[0][0] as int,
          'data_extração': DateTime.now().toIso8601String(),
        },
        'transacoes': result.last
            .map(
              (transaction) => {
                'tipo': transaction[0] as String,
                'valor': transaction[1] as int,
                'descricao': transaction[2] as String,
                'realizada_em': DateTime.fromMillisecondsSinceEpoch(
                  transaction[3] as int,
                ).toIso8601String(),
              },
            )
            .toList(),
      }, request);
    }).catchError((error, stackTrace) {
      print(error);
      Response.status(HttpStatus.unprocessableEntity, request);
    });
  }
}
