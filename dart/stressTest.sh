#!/bin/bash

shutdownDockers() {
  docker compose -f sql/docker-compose.yml down
  rm -rf sql/data/db
}

RESULTS_WORKSPACE="$(pwd)/load-test/user-files/results"
GATLING_BIN_DIR=$HOME/Developer/gatling/bin
GATLING_WORKSPACE="$(pwd)/load-test/user-files"

runGatling() {
    sh "$GATLING_BIN_DIR"/gatling.sh -rm local -s RinhaBackendCrebitosSimulation \
        -rd "Rinha de Backend - 2024/Q1: Crébito" \
        -rf "$RESULTS_WORKSPACE" \
        -sf "$GATLING_WORKSPACE/simulations"
}

startTest() {
 shutdownDockers
 docker compose -f sql/docker-compose.yml up -d
 sleep 10 # wait for database to be ready before running the tests

  for i in {1..20}; do
      curl --fail http://localhost:9999/clientes/1/extrato && \
      echo "" && \
      curl --fail http://localhost:9999/clientes/1/extrato && \
      echo "" && \
      runGatling && \
      break || sleep 2;
  done

 shutdownDockers
}

startTest
