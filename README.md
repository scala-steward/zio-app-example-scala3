[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

[Scala steward config settings](https://github.com/scala-steward-org/scala-steward/blob/main/docs/repo-specific-configuration.md)

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
