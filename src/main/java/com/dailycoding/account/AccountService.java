package com.dailycoding.account;

import com.dailycoding.config.AppProperties;
import com.dailycoding.domain.Account;
import com.dailycoding.domain.Tag;
import com.dailycoding.mail.EmailMessage;
import com.dailycoding.mail.EmailService;
import com.dailycoding.settings.form.Notifications;
import com.dailycoding.settings.form.Profile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

//    private Account saveNewAccount(@Valid SignUpForm signUpForm) {
//        Account account = Account.builder()
//                .email(signUpForm.getEmail())
//                .nickname(signUpForm.getNickname())
//                .password(passwordEncoder.encode(signUpForm.getPassword()))
//                .studyCreateByWeb(true)
//                .studyEnrollmentResultByWeb(true)
//                .studyUpdateByWeb(true)
//                .build();
//
//        return accountRepository.save(account);
//    }

    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        sendSingUpConfirmEmail(newAccount);
        return newAccount;
    }

    //refactoring
    private Account saveNewAccount(@Valid SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm, Account.class);
        account.generateEmailCheckToken();
        return accountRepository.save(account);
    }

    public void sendSingUpConfirmEmail(Account newAccount) {

        Context context = new Context();
        context.setVariable("link", "/check-email-token?token=" + newAccount.getEmailCheckToken() + "&email=" + newAccount.getEmail());
        context.setVariable("nickname", newAccount.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "스터디올래 서비스를 사용하려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("스터디올래, 회원 가입 인증")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
        //refactoring
//        SimpleMailMessage mailMessage = new SimpleMailMessage();
//        mailMessage.setTo(newAccount.getEmail());
//        mailMessage.setSubject("스터디 올레 회원 가입 인증");
//        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() + "&email=" + newAccount.getEmail());
//        javaMailSender.send(mailMessage);
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

    public void updateProfile(Account account, Profile profile) {
        //객체 필드명이 동일한 경우 맵핑해준다.
        modelMapper.map(profile, account);
        /*account.setUrl(profile.getUrl());
        account.setOccupation(profile.getOccupation());
        account.setLocation(profile.getLocation());
        account.setBio(profile.getBio());
        account.setProfileImage(profile.getProfileImage());*/
        accountRepository.save(account);
    }

    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account); //merge
    }

    public void updateNotifications(Account account, Notifications notifications) {
        modelMapper.map(notifications, account);
        /*account.setStudyCreateByEmail(notifications.isStudyCreatedByEmail());
        account.setStudyCreateByWeb(notifications.isStudyCreatedByWeb());
        account.setStudyEnrollmentResultByEmail(notifications.isStudyEnrollmentResultByEmail());
        account.setStudyEnrollmentResultByWeb(notifications.isStudyEnrollmentResultByWeb());
        account.setStudyUpdateByWeb(notifications.isStudyUpdatedByWeb());
        account.setStudyUpdateByEmail(notifications.isStudyUpdatedByEmail());*/
        accountRepository.save(account);
    }

    public void updateNickname(Account account, String nickname) {
        account.setNickname(nickname);
        accountRepository.save(account);
        login(account);
    }

    public void sendLoginLink(Account account) {
//        account.generateEmailCheckToken();
//        SimpleMailMessage mailMessage = new SimpleMailMessage();
//        mailMessage.setTo(account.getEmail());
//        mailMessage.setSubject("스터디올래, 로그인 링크");
//        mailMessage.setText("/login-by-email?token=" + account.getEmailCheckToken() + "&email=" + account.getEmail());
//        javaMailSender.send(mailMessage);

        Context context = new Context();
        context.setVariable("link", "/login-by-email?token=" + account.getEmailCheckToken() + "&email=" + account.getEmail());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "이메일로 로그인하기");
        context.setVariable("message", "로그인 하려면 아래 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("스터디올래, 로그인 링크")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    public void addTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().add(tag));

        //레이지로딩 필요한 순간에만 읽어옴 -> EntitiyMananger
        //accountRepository.getOne();
    }

    public Set<Tag> getTags(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getTags();
    }

    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().remove(tag));
    }
}
