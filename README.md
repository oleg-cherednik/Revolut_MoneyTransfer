# Backend Test

Server provides RESTful API for money transfers between accounts. It does not use any framework for handle REST requires and implement dependency
injection. All these are done with very simple manual implementation.


## Build and run

### Build

```
gradlew clean jar
```

## Run

```
java -jar build/libs/money-transfer-1.0.jar
```

## Use script to run an application
### Windows
```
run.bat
```
### Linux
```
run.sh
```

When server starts it prints current address and por to the console like `http://10.6.62.32:8080/`.

## Usage

To create money transfer, you should do following steps:

1. Create if required an account (will be used as source account)
1. Create if required an account (will be used as destination account)
1. Retrieve new `transactionId`; it will be used to work with transactino
1. Using generated `transactionId` and two different account, create new transaction with required cents to transfer

## Swagger

See details in attached `swagger.yaml`
