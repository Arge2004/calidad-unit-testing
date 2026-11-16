function fn() {
  // El puerto lo entrega PasswordApiKarateTest; 8080 sirve para apuntar a `gradle bootRun`
  var port = karate.properties['api.port'] || '8080';
  return {
    baseUrl: 'http://localhost:' + port
  };
}
