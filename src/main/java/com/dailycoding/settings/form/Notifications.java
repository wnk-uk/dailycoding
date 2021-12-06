package com.dailycoding.settings.form;

import com.dailycoding.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
/*@NoArgsConstructor*/
public class Notifications {

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb;

    /*public Notifications(Account account) {
        this.studyCreatedByEmail = account.isStudyCreateByEmail();
        this.studyCreatedByWeb = account.isStudyCreateByWeb();
        this.studyEnrollmentResultByEmail = account.isStudyEnrollmentResultByEmail();
        this.studyEnrollmentResultByWeb = account.isStudyEnrollmentResultByWeb();
        this.studyUpdatedByWeb = account.isStudyUpdateByWeb();
        this.studyUpdatedByEmail = account.isStudyUpdateByEmail();
    }*/
}
