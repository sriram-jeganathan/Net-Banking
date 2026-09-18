package main

import (
	"encoding/json"
	"fmt"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

// Account is the world-state record for a banking account.
type Account struct {
	ID      string `json:"id"`
	Balance int64  `json:"balance"`
}

// SmartContract implements the same banking operations as the PostgreSQL
// mechanisms, so the workload is logically equivalent across all three
// mechanisms in the study. Integrity here comes from Fabric's endorsement,
// ordering, and validation across peers in different organizations, not from an
// application-level chain or a database trigger.
type SmartContract struct {
	contractapi.Contract
}

func (s *SmartContract) CreateAccount(ctx contractapi.TransactionContextInterface, id string, initial int64) error {
	exists, err := s.exists(ctx, id)
	if err != nil {
		return err
	}
	if exists {
		return fmt.Errorf("account %s already exists", id)
	}
	return s.put(ctx, Account{ID: id, Balance: initial})
}

func (s *SmartContract) Deposit(ctx contractapi.TransactionContextInterface, id string, amount int64) error {
	a, err := s.get(ctx, id)
	if err != nil {
		return err
	}
	a.Balance += amount
	return s.put(ctx, *a)
}

func (s *SmartContract) Withdraw(ctx contractapi.TransactionContextInterface, id string, amount int64) error {
	a, err := s.get(ctx, id)
	if err != nil {
		return err
	}
	a.Balance -= amount
	return s.put(ctx, *a)
}

func (s *SmartContract) Transfer(ctx contractapi.TransactionContextInterface, from, to string, amount int64) error {
	src, err := s.get(ctx, from)
	if err != nil {
		return err
	}
	dst, err := s.get(ctx, to)
	if err != nil {
		return err
	}
	src.Balance -= amount
	dst.Balance += amount
	if err := s.put(ctx, *src); err != nil {
		return err
	}
	return s.put(ctx, *dst)
}

func (s *SmartContract) Balance(ctx contractapi.TransactionContextInterface, id string) (int64, error) {
	a, err := s.get(ctx, id)
	if err != nil {
		return 0, err
	}
	return a.Balance, nil
}

func (s *SmartContract) get(ctx contractapi.TransactionContextInterface, id string) (*Account, error) {
	b, err := ctx.GetStub().GetState(id)
	if err != nil {
		return nil, fmt.Errorf("state read failed: %w", err)
	}
	if b == nil {
		return nil, fmt.Errorf("account %s not found", id)
	}
	var a Account
	if err := json.Unmarshal(b, &a); err != nil {
		return nil, err
	}
	return &a, nil
}

func (s *SmartContract) put(ctx contractapi.TransactionContextInterface, a Account) error {
	b, err := json.Marshal(a)
	if err != nil {
		return err
	}
	return ctx.GetStub().PutState(a.ID, b)
}

func (s *SmartContract) exists(ctx contractapi.TransactionContextInterface, id string) (bool, error) {
	b, err := ctx.GetStub().GetState(id)
	if err != nil {
		return false, err
	}
	return b != nil, nil
}

func main() {
	cc, err := contractapi.NewChaincode(&SmartContract{})
	if err != nil {
		panic(fmt.Sprintf("chaincode create failed: %v", err))
	}
	if err := cc.Start(); err != nil {
		panic(fmt.Sprintf("chaincode start failed: %v", err))
	}
}
