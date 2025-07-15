package ru.mdemidkin.accounts.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.accounts.exception.AccountNotFoundException;
import ru.mdemidkin.accounts.model.Account;
import ru.mdemidkin.accounts.repository.AccountRepository;
import ru.mdemidkin.accounts.repository.UserRepository;
import ru.mdemidkin.libdto.cash.CashAction;
import ru.mdemidkin.libdto.cash.CashRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public Mono<Void> updateAccounts(String login, List<String> currencies) {
        return userRepository.findByLogin(login)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Пользователь не найден: " + login)))
                .flatMap(user -> accountRepository.findByUserId(user.getId())
                        .map(Account::getCurrency)
                        .collectList()
                        .flatMap(existingCurrencies -> {
                            List<String> toAdd = currencies.stream()
                                    .filter(c -> !existingCurrencies.contains(c))
                                    .toList();

                            List<String> toRemove = existingCurrencies.stream()
                                    .filter(c -> !currencies.contains(c))
                                    .toList();

                            Flux<Account> addOperations = Flux.fromIterable(toAdd)
                                    .flatMap(currency -> createAccount(user.getId(), currency));

                            Flux<Void> removeOperations = Flux.fromIterable(toRemove)
                                    .flatMap(currency -> accountRepository.deleteByUserIdAndCurrency(user.getId(), currency));

                            return Flux.merge(addOperations, removeOperations)
                                    .then();
                        }));
    }

    private Mono<Account> createAccount(Long userId, String currency) {
        Account account = Account.builder()
                .userId(userId)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return accountRepository.save(account);
    }

    public Flux<Account> getUserAccounts(String login) {
        return userRepository.findByLogin(login)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Пользователь не найден: " + login)))
                .flatMapMany(user -> accountRepository.findByUserId(user.getId()));
    }

    public Mono<Account> getAccount(String login, String currency) {
        return userRepository.findByLogin(login)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Пользователь не найден: " + login)))
                .flatMap(user -> accountRepository.findByUserIdAndCurrency(user.getId(), currency));
    }

    public Mono<Void> updateCashBalance(String login, CashRequest cashRequest) {
        return userRepository.findByLogin(login)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Пользователь не найден: " + login)))
                .flatMap(user -> accountRepository.findByUserIdAndCurrency(user.getId(), cashRequest.getCurrency())
                        .switchIfEmpty(Mono.error(new AccountNotFoundException("Счет не найден для валюты: " + cashRequest.getCurrency())))
                        .flatMap(account -> {
                            BigDecimal newBalance = getUpdatedBalance(cashRequest, account);
                            account.setBalance(newBalance);
                            account.setUpdatedAt(LocalDateTime.now());
                            return accountRepository.save(account);
                        })
                )
                .then();
    }

    private static BigDecimal getUpdatedBalance(CashRequest cashRequest, Account account) {
        CashAction action = cashRequest.getAction();
        BigDecimal newBalance;
        if (Objects.requireNonNull(action) == CashAction.PUT) {
            newBalance = account.getBalance().add(new BigDecimal(cashRequest.getValue()));
        } else {
            newBalance = account.getBalance().subtract(new BigDecimal(cashRequest.getValue()));
        }
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return newBalance;
    }
}
