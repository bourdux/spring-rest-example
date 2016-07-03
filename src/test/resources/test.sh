#!/bin/bash

#create a transaction
echo "PUT /transactionservice/transaction/10 { "amount": 5000, "type": "cars" } => "
curl -X PUT --data '{"amount": 5000, "type": "cars"}' -H "Content-Type: application/json;charset=utf-8" localhost:8080/transactionservice/transaction/10
echo "\n"

#create a child transaction
echo "PUT /transactionservice/transaction/11 { "amount": 10000, "type": "shopping", "parent_id": 10 } => "
curl -X PUT --data '{"amount": 10000, "type": "shopping", "parent_id": 10}' -H "Content-Type: application/json;charset=utf-8" localhost:8080/transactionservice/transaction/11
echo "\n"

#Get transaction of type "cars"
echo "GET /transactionservice/types/cars => "
curl localhost:8080/transactionservice/types/cars
echo "\n"

#Get sum of parent transaction
echo "GET /transactionservice/sum/10 => "
curl localhost:8080/transactionservice/sum/10
echo "\n"

#Get sum of child transaction
echo "GET /transactionservice/sum/11 => "
curl localhost:8080/transactionservice/sum/11
echo "\n"

#Try to make a cycle
echo "PUT /transactionservice/transaction/10 { "amount": 5000, "type": "cars", "parent_id": 11 } => "
curl -X PUT --data '{"amount": 5000, "type": "cars", "parent_id": 11}' -H "Content-Type: application/json;charset=utf-8" localhost:8080/transactionservice/transaction/10
echo "\n"

#Cleanup
echo "DELETE /transactionservice/transaction/11 => "
curl -X DELETE localhost:8080/transactionservice/transaction/11
echo "\n"

echo "DELETE /transactionservice/transaction/10 => "
curl -X DELETE localhost:8080/transactionservice/transaction/10
echo "\n"
