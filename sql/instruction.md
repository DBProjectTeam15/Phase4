# initialization process

This instruction is for `MacOS` / `docker` users.

If you have installed oracle successfully, just run `schema.sql` and `data.sql`.

You can also add user by issuing the SQL statements written in below `Phase 1` section.

## Phase 1 : initialize user

- connect as sys with `SYSDBA` permissions.

```shell
docker exec -it <CONTAINER_NAME> sqlplus sys/oracle as SYSDBA;
```

- create user

```oraclesqlplus
CREATE USER musicbase IDENTIFIED BY musicbase1234;
GRANT CONNECT, RESOURCE TO musicbase;
GRANT UNLIMITED TABLESPACE TO musicbase;
```

## Phase 2 : inject schema and data

```shell
docker cp ./sql/schema.sql <CONTAINER_NAME>:/tmp/schema.sql
docker cp ./sql/data.sql <CONTAINER_NAME>:/tmp/data.sql

docker exec -it <CONTAINER_NAME> sqlplus user1234/password1234 @/tmp/schema.sql
docker exec -it <CONTAINER_NAME> sqlplus user1234/password1234 @/tmp/data.sql
```

MAKE SURE to write `COMMIT` statement.