package com.google.clusterfuzz.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents system configuration in the ClusterFuzz system.
 * This is the Java equivalent of the Python Config model.
 */
@Entity
@Table(name = "configs")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "previous_hash")
    private String previousHash = "";

    @Column(name = "url")
    private String url = "";

    @Lob
    @Column(name = "client_credentials")
    private String clientCredentials = "";

    @Column(name = "jira_url")
    private String jiraUrl = "";

    @Lob
    @Column(name = "jira_credentials")
    private String jiraCredentials = "";

    @Lob
    @Column(name = "build_apiary_service_account_private_key")
    private String buildApiaryServiceAccountPrivateKey = "";

    @Column(name = "test_account_email")
    private String testAccountEmail = "";

    @Column(name = "test_account_password")
    private String testAccountPassword = "";

    @Lob
    @Column(name = "privileged_users")
    private String privilegedUsers = "";

    @Lob
    @Column(name = "blacklisted_users")
    private String blacklistedUsers = "";

    @Column(name = "contact_string")
    private String contactString = "";

    @Lob
    @Column(name = "component_repository_mappings")
    private String componentRepositoryMappings = "";

    @Column(name = "reproduction_help_url")
    private String reproductionHelpUrl = "";

    @Column(name = "documentation_url")
    private String documentationUrl = "";

    @Column(name = "bug_report_url")
    private String bugReportUrl = "";

    @Lob
    @Column(name = "platform_group_mappings")
    private String platformGroupMappings = "";

    @Column(name = "relax_testcase_restrictions")
    private Boolean relaxTestcaseRestrictions = false;

    @Column(name = "relax_security_bug_restrictions")
    private Boolean relaxSecurityBugRestrictions = false;

    @CreatedDate
    @Column(name = "created", updatable = false)
    private LocalDateTime created;

    @LastModifiedDate
    @Column(name = "updated")
    private LocalDateTime updated;

    // Constructors
    public Config() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash != null ? previousHash : "";
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url != null ? url : "";
    }

    public String getClientCredentials() {
        return clientCredentials;
    }

    public void setClientCredentials(String clientCredentials) {
        this.clientCredentials = clientCredentials != null ? clientCredentials : "";
    }

    public String getJiraUrl() {
        return jiraUrl;
    }

    public void setJiraUrl(String jiraUrl) {
        this.jiraUrl = jiraUrl != null ? jiraUrl : "";
    }

    public String getJiraCredentials() {
        return jiraCredentials;
    }

    public void setJiraCredentials(String jiraCredentials) {
        this.jiraCredentials = jiraCredentials != null ? jiraCredentials : "";
    }

    public String getBuildApiaryServiceAccountPrivateKey() {
        return buildApiaryServiceAccountPrivateKey;
    }

    public void setBuildApiaryServiceAccountPrivateKey(String buildApiaryServiceAccountPrivateKey) {
        this.buildApiaryServiceAccountPrivateKey = buildApiaryServiceAccountPrivateKey != null ? buildApiaryServiceAccountPrivateKey : "";
    }

    public String getTestAccountEmail() {
        return testAccountEmail;
    }

    public void setTestAccountEmail(String testAccountEmail) {
        this.testAccountEmail = testAccountEmail != null ? testAccountEmail : "";
    }

    public String getTestAccountPassword() {
        return testAccountPassword;
    }

    public void setTestAccountPassword(String testAccountPassword) {
        this.testAccountPassword = testAccountPassword != null ? testAccountPassword : "";
    }

    public String getPrivilegedUsers() {
        return privilegedUsers;
    }

    public void setPrivilegedUsers(String privilegedUsers) {
        this.privilegedUsers = privilegedUsers != null ? privilegedUsers : "";
    }

    public String getBlacklistedUsers() {
        return blacklistedUsers;
    }

    public void setBlacklistedUsers(String blacklistedUsers) {
        this.blacklistedUsers = blacklistedUsers != null ? blacklistedUsers : "";
    }

    public String getContactString() {
        return contactString;
    }

    public void setContactString(String contactString) {
        this.contactString = contactString != null ? contactString : "";
    }

    public String getComponentRepositoryMappings() {
        return componentRepositoryMappings;
    }

    public void setComponentRepositoryMappings(String componentRepositoryMappings) {
        this.componentRepositoryMappings = componentRepositoryMappings != null ? componentRepositoryMappings : "";
    }

    public String getReproductionHelpUrl() {
        return reproductionHelpUrl;
    }

    public void setReproductionHelpUrl(String reproductionHelpUrl) {
        this.reproductionHelpUrl = reproductionHelpUrl != null ? reproductionHelpUrl : "";
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl != null ? documentationUrl : "";
    }

    public String getBugReportUrl() {
        return bugReportUrl;
    }

    public void setBugReportUrl(String bugReportUrl) {
        this.bugReportUrl = bugReportUrl != null ? bugReportUrl : "";
    }

    public String getPlatformGroupMappings() {
        return platformGroupMappings;
    }

    public void setPlatformGroupMappings(String platformGroupMappings) {
        this.platformGroupMappings = platformGroupMappings != null ? platformGroupMappings : "";
    }

    public Boolean getRelaxTestcaseRestrictions() {
        return relaxTestcaseRestrictions;
    }

    public void setRelaxTestcaseRestrictions(Boolean relaxTestcaseRestrictions) {
        this.relaxTestcaseRestrictions = relaxTestcaseRestrictions != null ? relaxTestcaseRestrictions : false;
    }

    public Boolean getRelaxSecurityBugRestrictions() {
        return relaxSecurityBugRestrictions;
    }

    public void setRelaxSecurityBugRestrictions(Boolean relaxSecurityBugRestrictions) {
        this.relaxSecurityBugRestrictions = relaxSecurityBugRestrictions != null ? relaxSecurityBugRestrictions : false;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    // Utility methods
    public boolean hasJiraIntegration() {
        return jiraUrl != null && !jiraUrl.isEmpty() && 
               jiraCredentials != null && !jiraCredentials.isEmpty();
    }

    public boolean hasTestAccount() {
        return testAccountEmail != null && !testAccountEmail.isEmpty() && 
               testAccountPassword != null && !testAccountPassword.isEmpty();
    }

    public boolean hasPrivilegedUsers() {
        return privilegedUsers != null && !privilegedUsers.isEmpty();
    }

    public boolean hasBlacklistedUsers() {
        return blacklistedUsers != null && !blacklistedUsers.isEmpty();
    }

    public boolean hasDocumentationUrls() {
        return (documentationUrl != null && !documentationUrl.isEmpty()) ||
               (reproductionHelpUrl != null && !reproductionHelpUrl.isEmpty()) ||
               (bugReportUrl != null && !bugReportUrl.isEmpty());
    }

    public boolean isRestrictionsRelaxed() {
        return Boolean.TRUE.equals(relaxTestcaseRestrictions) || 
               Boolean.TRUE.equals(relaxSecurityBugRestrictions);
    }

    // equals, hashCode, and toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Config config = (Config) o;
        return Objects.equals(id, config.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Config{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", hasJiraIntegration=" + hasJiraIntegration() +
                ", hasTestAccount=" + hasTestAccount() +
                ", relaxTestcaseRestrictions=" + relaxTestcaseRestrictions +
                ", relaxSecurityBugRestrictions=" + relaxSecurityBugRestrictions +
                ", created=" + created +
                '}';
    }
}