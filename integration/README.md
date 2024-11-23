# Integration Tests

To run Integration tests: ```sbt 'integration / test'```
```bash
sbt 'integration / testOnly *UserRepositoryITSpec -- -z "deleteUserByUserName"'
```
