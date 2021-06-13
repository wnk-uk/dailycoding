package com.dailycoding.account;

import com.dailycoding.ConsoleMailSender;
import com.dailycoding.domain.Account;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFromValidator signUpFromValidator;
    private final AccountService accountService;

    //해당 타입의 CamelCase 를 따라감
    //즉 하기의 sign-up form을 받을때 하기의 로직으로 검증
    @InitBinder("signUpFrom")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFromValidator);
    }

    @GetMapping("sign-up")
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors){
        if (errors.hasErrors()) {
            return "account/sign-up";
        }
       accountService.processNewAccount(signUpForm);

        return "redirect:/";
    }



}
