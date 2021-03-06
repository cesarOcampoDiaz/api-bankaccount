package com.nttdata.api.bankaccount.controller;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import com.nttdata.api.bankaccount.document.Card;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.nttdata.api.bankaccount.document.BankAccount;
import com.nttdata.api.bankaccount.service.IBankAccountService;
import com.nttdata.api.bankaccount.util.Constants;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Clase controlador bankaccount donde se encargara de interactuar
 * con el front end, en esta clase tiene los metodos CRUD.
 */
@RestController
@RequestMapping("/bankaccount")
public class BankAccountController {

    /**
     * Se declara el tipo LOGGER para el control del la clase.
     */
    private static final Logger LOGGER = LogManager.getLogger(BankAccountController.class);

    /**
     * Se declara el servicio de bankAccountService.
     */
    @Autowired
    private IBankAccountService bankAccountService;

    /**
     * @return retorna todas las cuentas bancarias.
     */
    @GetMapping
    public Mono<ResponseEntity<Flux<BankAccount>>> findAll() {
        return Mono
                .just(ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(bankAccountService.findAll()));
    }


    /**
     * @param id recibe como parametro el id de BankAccount
     * @return retarna el objeto buscado BankAccount.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<BankAccount>> findById(@PathVariable String id) {
        return bankAccountService
                .findById(id)
                .map(ba -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ba))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * @param codeClient
     * @param typeClient
     * @return
     */
    @GetMapping("/typeClient/{codeClient}/{typeClient}")
    public Mono<ResponseEntity<Flux<BankAccount>>> findByCodeClientAndTypeClientTypeClient(
            @PathVariable String codeClient, @PathVariable Integer typeClient) {
        return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(bankAccountService.findByCodeClientAndTypeClient(codeClient, typeClient)));
    }


    @GetMapping("/client/{codeClient}")
    public Mono<ResponseEntity<Flux<BankAccount>>> findByCodeClient(
            @PathVariable String codeClient) {
        return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(bankAccountService.findByCodeClient(codeClient)));
    }

    /**
     * @param monoBankAccount
     * @return
     */
    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> addBankAccount(
            @Valid @RequestBody Mono<BankAccount> monoBankAccount) {
        Map<String, Object> response = new HashMap<>();

        return  monoBankAccount.flatMap(bankAccount -> {
            bankAccount.setMembershipDate(new Date());

            return bankAccountService.save(bankAccount).map(ba -> {
                response.put("obj", ba.getObj());
                response.put("message", ba.getMessage());
                response.put("timestamp", new Date());

                return ResponseEntity.created(URI.create("/bankaccount/".concat(bankAccount.getAccountNumber())))
                        .contentType(MediaType.APPLICATION_JSON).body(response);
            });

        });


    }


    /**
     * @param bankAccount
     * @param id
     * @return
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> editBankAccount(@RequestBody BankAccount bankAccount,
                                                        @PathVariable String id) {
        return bankAccountService.findById(id).flatMap(ba -> {
            ba.setBalance(bankAccount.getBalance());
            return bankAccountService.save(ba);
        }).map(ba -> ResponseEntity.created(URI.create("/bankaccount/".concat(bankAccount.getAccountNumber())))
                .contentType(MediaType.APPLICATION_JSON).body(ba.getObj())).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteBankAccount(@PathVariable String id) {
        return bankAccountService.findById(id).flatMap(ba -> {
            return bankAccountService.delete(ba).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));

        }).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }


    @GetMapping("/client/{codeClient}/{typeAccount}")
    public Mono<ResponseEntity<Flux<BankAccount>>> findByCodeClientAndTypeAccount(
            @PathVariable String codeClient, @PathVariable Integer typeAccount) {
        return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(bankAccountService.findByCodeClientAndTypeAccountId(codeClient, typeAccount)));
    }


    /**
     * Realiza la busquedad por cliente y numero de cuenta.
     *
     * @param codeClient
     * @param accountNumber
     * @return
     */
    @PutMapping("/client/{codeClient}/{accountNumber}")
    public Mono<ResponseEntity<BankAccount>> editCard(@RequestBody Card card, @PathVariable String codeClient, @PathVariable String accountNumber) {

        //String codeClient, String accountNumber
        return bankAccountService
                .findByCodeClientAndAccountNumber(codeClient, accountNumber)
                .flatMap(ca -> {
                    ca.setCard(card);
                    return bankAccountService.saveCard(ca);
                }).map(ba -> ResponseEntity.created(URI.create("/bankaccount".concat("/client/").concat(codeClient).concat("/").concat(accountNumber)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(ba))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @GetMapping("/main/{id}/{cardNumber}")
    public Mono<ResponseEntity<BankAccount>> findMainAccount(@PathVariable String id, @PathVariable String cardNumber) {
        return bankAccountService
                .mainAccount(id, cardNumber)
                .map(ba -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ba))
                .defaultIfEmpty(ResponseEntity.notFound().build());

    }

}
