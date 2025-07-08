package ru.mdemidkin.accounts.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.accounts.model.Account;
import ru.mdemidkin.accounts.repository.AccountRepository;
import ru.mdemidkin.accounts.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
}
