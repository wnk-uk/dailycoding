package com.dailycoding.study;

import com.dailycoding.domain.Account;
import com.dailycoding.domain.Study;
import com.dailycoding.domain.Tag;
import com.dailycoding.domain.Zone;
import com.dailycoding.study.form.StudyDescriptionForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.dailycoding.study.form.StudyForm.VALID_PATH_PATTERN;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository repository;
    private final ModelMapper modelMapper;

    public Study createNewStudy(Study study, Account account) {
        Study newStudy = repository.save(study);
        newStudy.addManager(account);

        return newStudy;
    }

    public Study getStudyToUpdate(Account account, String path) {
        Study study = this.getStudy(path);
        if(!study.isManagedBy(account)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
        return study;
    }

    public Study getStudy(String path) {
        Study study = this.repository.findByPath(path);
        if(study == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
        return study;
    }

    public void updateStudyDescription(Study study, StudyDescriptionForm studyDescriptionForm) {
        modelMapper.map(studyDescriptionForm, study);
    }

    public void updateStudyImage(Study study, String image) {
        study.setImage(image);
    }

    public void enableStudyBanner(Study study) {
        study.setUseBanner(true);
    }

    public void disableStudyBanner(Study study) {
        study.setUseBanner(false);
    }

    public Study getStudyToUpdateTag(Account account, String path) {
        Study study = repository.findStudyWithTagsByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    private void checkIfManager(Account account, Study study) {
        if (!study.isManagedBy(account)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    private void checkIfExistingStudy(String path, Study study) {
        if(study == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
    }

    public void addTag(Study study, Tag tag) {
        study.getTags().add(tag);
    }

    public void removeTag(Study study, Tag tag) {
        study.getTags().remove(tag);
    }

    public void addZone(Study study, Zone zone) {
        study.getZones().add(zone);
    }

    public void removeZone(Study study, Zone zone) {
        study.getZones().remove(zone);
    }

    public Study getStudyToUpdateZone(Account account, String path) {
        Study study = repository.findStudyWithZonesByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    public Study getStudyToUpdateStatus(Account account, String path) {
        Study study = repository.findStudyWithManagersByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    public void publish(Study study) {
        study.publish();
    }

    public void close(Study study) {
        study.close();
    }

    public void startRecurit(Study study) {
        study.startRecurit();
    }

    public void stopRecurit(Study study) {
        study.stopRecurit();
    }

    public boolean isValidPath(String newPath) {
        if (!newPath.matches(VALID_PATH_PATTERN)) {
            return false;
        }

        return !repository.existsByPath(newPath);
    }

    public void updateStudyPath(Study study, String newPath) {
        study.setPath(newPath);
    }

    public boolean isValidTitle(String newTtile) {
        return newTtile.length() <= 50;
    }

    public void updateStudyTitle(Study study, String newTitle) {
        study.setTitle(newTitle);
    }
}
