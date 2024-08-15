# To run the application:

To spin up the postgres dependency:
```bash
docker compose up -d postgres
```

To stop the running postgres container:
```bash
docker compose down postgres
```

To run the application:
```bash
sbt "runMain runner.Main"
```

and

```bash
curl --location 'http://localhost:8080/status'
````
or
```bash
curl --location 'http://localhost:8080/hello'
```

## To reach the open api/swagger docs use this url: 
`http://localhost:8080/docs/openapi`