package com.dailycoding.account;

import com.dailycoding.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    private Account saveNewAccount(@Valid SignUpForm signUpForm) {
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .studyCreateByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdateByWeb(true)
                .build();

        return accountRepository.save(account);
    }

    public void sendSingUpConfirmEmail(Account newAccount) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("스터디 올레 회원 가입 인증");
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() + "&email=" + newAccount.getEmail());
        javaMailSender.send(mailMessage);
    }


    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        newAccount.generateEmailCheckToken();
        sendSingUpConfirmEmail(newAccount);
        return newAccount;
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                                        new UserAccount(account),
                                        account.getPassword(),
                                        List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailOrNickName) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickName);

        if (account == null) {
            account = accountRepository.findByNickname(emailOrNickName);
        }

        if(account == null) {
            throw new UsernameNotFoundException(emailOrNickName);
        }

        return new UserAccount(account);
    }

    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account);
    }
}
