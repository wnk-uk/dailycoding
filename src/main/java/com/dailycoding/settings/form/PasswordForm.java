package com.dailycoding.settings;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class PasswordForm {

        @Length(max = 50, min = 8)
        private String newPassword;

        @Length(max = 50, min = 8)
        private String newPasswordConfirm;
}
