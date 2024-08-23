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

# Todo:
* add scala-steward