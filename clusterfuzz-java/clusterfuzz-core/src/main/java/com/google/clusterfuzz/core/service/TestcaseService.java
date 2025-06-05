package com.google.clusterfuzz.core.service;

import com.google.clusterfuzz.core.entity.Testcase;
import com.google.clusterfuzz.core.repository.TestcaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Testcase operations.
 * Provides business logic for testcase management.
 */
@Service
@Transactional
public class TestcaseService {

    private static final Logger logger = LoggerFactory.getLogger(TestcaseService.class);

    private final TestcaseRepository testcaseRepository;

    @Autowired
    public TestcaseService(TestcaseRepository testcaseRepository) {
        this.testcaseRepository = testcaseRepository;
    }

    // Basic CRUD operations
    public Testcase save(Testcase testcase) {
        logger.debug("Saving testcase: {}", testcase);
        return testcaseRepository.save(testcase);
    }

    @Transactional(readOnly = true)
    public Optional<Testcase> findById(Long id) {
        return testcaseRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Testcase> findAll() {
        return testcaseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Testcase> findAll(Pageable pageable) {
        return testcaseRepository.findAll(pageable);
    }

    public void deleteById(Long id) {
        logger.info("Deleting testcase with id: {}", id);
        testcaseRepository.deleteById(id);
    }

    // Business logic methods
    @Transactional(readOnly = true)
    public List<Testcase> findOpenTestcases() {
        return testcaseRepository.findByOpenTrue();
    }

    @Transactional(readOnly = true)
    public List<Testcase> findSecurityBugs() {
        return testcaseRepository.findBySecurityFlagTrue();
    }

    @Transactional(readOnly = true)
    public List<Testcase> findOpenSecurityBugs() {
        return testcaseRepository.findBySecurityFlagTrueAndOpenTrue();
    }

    @Transactional(readOnly = true)
    public List<Testcase> findHighSeveritySecurityBugs(Integer minSeverity) {
        return testcaseRepository.findHighSeveritySecurityBugs(minSeverity);
    }

    @Transactional(readOnly = true)
    public List<Testcase> findByJobType(String jobType) {
        return testcaseRepository.findByJobType(jobType);
    }

    @Transactional(readOnly = true)
    public List<Testcase> findByFuzzer(String fuzzerName) {
        return testcaseRepository.findByFuzzerName(fuzzerName);
    }

    @Transactional(readOnly = true)
    public List<Testcase> findByProject(String projectName) {
        return testcaseRepository.findByProjectName(projectName);
    }

    @Transactional(readOnly = true)
    public List<Testcase> findByPlatform(String platform) {
        return testcaseRepository.findByPlatform(platform);
    }

    @Transactional(readOnly = true)
    public List<Testcase> findByCrashType(String crashType) {
        return testcaseRepository.findByCrashType(crashType);
    }

    @Transactional(readOnly = true)
    public List<Testcase> findByCrashTypeAndState(String crashType, String crashState) {
        return testcaseRepository.findByCrashTypeAndState(crashType, crashState);
    }

    @Transactional(readOnly = true)
    public List<Testcase> findGroupLeaders() {
        return testcaseRepository.findByIsLeaderTrue();
    }

    @Transactional(readOnly = true)
    public List<Testcase> findGroupMembers(Long groupId) {
        return testcaseRepository.findGroupMembers(groupId);
    }

    @Transactional(readOnly = true)
    public List<Testcase> findDuplicates() {
        return testcaseRepository.findByIsADuplicateFlagTrue();
    }

    @Transactional(readOnly = true)
    public List<Testcase> findUntriagedTestcases() {
        return testcaseRepository.findUntriagedProcessedTestcases();
    }

    @Transactional(readOnly = true)
    public List<Testcase> findFlakyTestcases() {
        return testcaseRepository.findFlakyTestcases();
    }

    @Transactional(readOnly = true)
    public List<Testcase> findRecentTestcases(LocalDateTime since) {
        return testcaseRepository.findByTimestampAfter(since);
    }

    @Transactional(readOnly = true)
    public List<Testcase> findRecentTestcasesByFuzzer(String fuzzerName, LocalDateTime since) {
        return testcaseRepository.findRecentTestcasesByFuzzer(fuzzerName, since);
    }

    @Transactional(readOnly = true)
    public List<Testcase> findRecentOneTimeCrashers(LocalDateTime since) {
        return testcaseRepository.findRecentOneTimeCrashers(since);
    }

    // Search functionality
    @Transactional(readOnly = true)
    public List<Testcase> searchTestcases(String searchTerm) {
        return testcaseRepository.searchTestcases(searchTerm);
    }

    @Transactional(readOnly = true)
    public Page<Testcase> searchTestcases(String searchTerm, Pageable pageable) {
        return testcaseRepository.searchTestcases(searchTerm, pageable);
    }

    // Statistics methods
    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        return testcaseRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countSecurityBugs() {
        return testcaseRepository.countSecurityBugs();
    }

    @Transactional(readOnly = true)
    public long countOpenTestcases() {
        return testcaseRepository.countOpenTestcases();
    }

    @Transactional(readOnly = true)
    public long countByProject(String projectName) {
        return testcaseRepository.countByProject(projectName);
    }

    @Transactional(readOnly = true)
    public long countByFuzzer(String fuzzerName) {
        return testcaseRepository.countByFuzzer(fuzzerName);
    }

    @Transactional(readOnly = true)
    public long countRecentTestcases(LocalDateTime since) {
        return testcaseRepository.countRecentTestcases(since);
    }

    // Testcase lifecycle methods
    public Testcase createTestcase(String crashType, String crashState, String fuzzerName, String jobType) {
        Testcase testcase = new Testcase(crashType, crashState, fuzzerName, jobType);
        logger.info("Creating new testcase: crashType={}, fuzzerName={}, jobType={}", 
                   crashType, fuzzerName, jobType);
        return save(testcase);
    }

    public Testcase markAsTriaged(Long testcaseId) {
        Optional<Testcase> optionalTestcase = findById(testcaseId);
        if (optionalTestcase.isPresent()) {
            Testcase testcase = optionalTestcase.get();
            testcase.setTriaged(true);
            logger.info("Marking testcase {} as triaged", testcaseId);
            return save(testcase);
        }
        throw new IllegalArgumentException("Testcase not found with id: " + testcaseId);
    }

    public Testcase closeTestcase(Long testcaseId) {
        Optional<Testcase> optionalTestcase = findById(testcaseId);
        if (optionalTestcase.isPresent()) {
            Testcase testcase = optionalTestcase.get();
            testcase.setOpen(false);
            logger.info("Closing testcase {}", testcaseId);
            return save(testcase);
        }
        throw new IllegalArgumentException("Testcase not found with id: " + testcaseId);
    }

    public Testcase reopenTestcase(Long testcaseId) {
        Optional<Testcase> optionalTestcase = findById(testcaseId);
        if (optionalTestcase.isPresent()) {
            Testcase testcase = optionalTestcase.get();
            testcase.setOpen(true);
            logger.info("Reopening testcase {}", testcaseId);
            return save(testcase);
        }
        throw new IllegalArgumentException("Testcase not found with id: " + testcaseId);
    }

    public Testcase markAsDuplicate(Long testcaseId, Long duplicateOfId) {
        Optional<Testcase> optionalTestcase = findById(testcaseId);
        if (optionalTestcase.isPresent()) {
            Testcase testcase = optionalTestcase.get();
            testcase.setDuplicateOf(duplicateOfId);
            testcase.setIsADuplicateFlag(true);
            logger.info("Marking testcase {} as duplicate of {}", testcaseId, duplicateOfId);
            return save(testcase);
        }
        throw new IllegalArgumentException("Testcase not found with id: " + testcaseId);
    }

    public Testcase setSecurityFlag(Long testcaseId, boolean isSecurityBug, Integer severity) {
        Optional<Testcase> optionalTestcase = findById(testcaseId);
        if (optionalTestcase.isPresent()) {
            Testcase testcase = optionalTestcase.get();
            testcase.setSecurityFlag(isSecurityBug);
            if (isSecurityBug && severity != null) {
                testcase.setSecuritySeverity(severity);
            }
            logger.info("Setting security flag for testcase {} to {} with severity {}", 
                       testcaseId, isSecurityBug, severity);
            return save(testcase);
        }
        throw new IllegalArgumentException("Testcase not found with id: " + testcaseId);
    }

    public Testcase updateBugInformation(Long testcaseId, String bugInformation) {
        Optional<Testcase> optionalTestcase = findById(testcaseId);
        if (optionalTestcase.isPresent()) {
            Testcase testcase = optionalTestcase.get();
            testcase.setBugInformation(bugInformation);
            testcase.setHasBugFlag(bugInformation != null && !bugInformation.isEmpty());
            logger.info("Updating bug information for testcase {}", testcaseId);
            return save(testcase);
        }
        throw new IllegalArgumentException("Testcase not found with id: " + testcaseId);
    }

    // Group management methods
    public Testcase setAsGroupLeader(Long testcaseId, Long groupId) {
        Optional<Testcase> optionalTestcase = findById(testcaseId);
        if (optionalTestcase.isPresent()) {
            Testcase testcase = optionalTestcase.get();
            testcase.setGroupId(groupId);
            testcase.setIsLeader(true);
            logger.info("Setting testcase {} as leader of group {}", testcaseId, groupId);
            return save(testcase);
        }
        throw new IllegalArgumentException("Testcase not found with id: " + testcaseId);
    }

    public Testcase addToGroup(Long testcaseId, Long groupId) {
        Optional<Testcase> optionalTestcase = findById(testcaseId);
        if (optionalTestcase.isPresent()) {
            Testcase testcase = optionalTestcase.get();
            testcase.setGroupId(groupId);
            testcase.setIsLeader(false);
            logger.info("Adding testcase {} to group {}", testcaseId, groupId);
            return save(testcase);
        }
        throw new IllegalArgumentException("Testcase not found with id: " + testcaseId);
    }

    // Batch operations
    public List<Testcase> saveAll(List<Testcase> testcases) {
        logger.info("Saving {} testcases", testcases.size());
        return testcaseRepository.saveAll(testcases);
    }

    public void deleteAll(List<Testcase> testcases) {
        logger.info("Deleting {} testcases", testcases.size());
        testcaseRepository.deleteAll(testcases);
    }

    // Validation methods
    public boolean exists(Long id) {
        return testcaseRepository.existsById(id);
    }

    public boolean existsByCrashTypeAndState(String crashType, String crashState) {
        return testcaseRepository.existsByCrashTypeAndCrashState(crashType, crashState);
    }

    public boolean existsByFuzzerAndJob(String fuzzerName, String jobType) {
        return testcaseRepository.existsByFuzzerNameAndJobType(fuzzerName, jobType);
    }
}