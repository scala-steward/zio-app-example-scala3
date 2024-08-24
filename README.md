# To run the application:

### To spin up the postgres dependency:
```bash
docker compose up -d postgres
```

To stop the running postgres container:
```bash
docker compose down postgres
```

### To run the application:
```bash
sbt "runMain runner.Main"
```

## To reach the open api/swagger documentation, use this url: 
`http://localhost:8080/docs/openapi`

---

# Todo:
* add scala-steward

---
## Useful links

* https://github.com/zio/zio-http/blob/283934e5282fc7dbb8f11f955d5bd733030005e2/zio-http-example/src/main/scala/example/endpoint/style/DeclarativeProgrammingExample.scala#L37
* https://github.com/zio/zio-http/blob/283934e5282fc7dbb8f11f955d5bd733030005e2/zio-http-example/src/main/scala/example/endpoint/EndpointWithMultipleErrorsUsingEither.scala#L46
* https://github.com/zio/zio-http/blob/283934e5282fc7dbb8f11f955d5bd733030005e2/zio-http-example/src/main/scala/example/endpoint/EndpointWithMultipleUnifiedErrors.scala
