package com.google.clusterfuzz.core.repository;

import com.google.clusterfuzz.core.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Job entities.
 * Provides data access methods for job operations.
 */
@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    // Basic queries
    Optional<Job> findByName(String name);
    
    List<Job> findByPlatform(String platform);
    
    List<Job> findByProject(String project);
    
    // External job queries
    @Query("SELECT j FROM Job j WHERE j.externalReproductionTopic IS NOT NULL AND j.externalReproductionTopic != ''")
    List<Job> findExternalJobs();
    
    @Query("SELECT j FROM Job j WHERE j.externalReproductionTopic IS NULL OR j.externalReproductionTopic = ''")
    List<Job> findInternalJobs();
    
    @Query("SELECT j FROM Job j WHERE j.externalReproductionTopic = :topic")
    Optional<Job> findByExternalReproductionTopic(@Param("topic") String topic);
    
    @Query("SELECT j FROM Job j WHERE j.externalUpdatesSubscription = :subscription")
    Optional<Job> findByExternalUpdatesSubscription(@Param("subscription") String subscription);
    
    // Custom binary queries
    @Query("SELECT j FROM Job j WHERE j.customBinaryKey IS NOT NULL AND j.customBinaryKey != ''")
    List<Job> findJobsWithCustomBinary();
    
    @Query("SELECT j FROM Job j WHERE j.customBinaryKey IS NULL OR j.customBinaryKey = ''")
    List<Job> findJobsWithoutCustomBinary();
    
    @Query("SELECT j FROM Job j WHERE j.customBinaryRevision = :revision")
    List<Job> findByCustomBinaryRevision(@Param("revision") Long revision);
    
    // Template-based queries
    @Query("SELECT DISTINCT j FROM Job j JOIN j.templates t WHERE t = :templateName")
    List<Job> findByTemplate(@Param("templateName") String templateName);
    
    @Query("SELECT j FROM Job j WHERE j.templates IS NOT EMPTY")
    List<Job> findJobsWithTemplates();
    
    @Query("SELECT j FROM Job j WHERE j.templates IS EMPTY OR j.templates IS NULL")
    List<Job> findJobsWithoutTemplates();
    
    // Platform and project combinations
    @Query("SELECT j FROM Job j WHERE j.platform = :platform AND j.project = :project")
    List<Job> findByPlatformAndProject(@Param("platform") String platform, 
                                      @Param("project") String project);
    
    // Search queries
    @Query("SELECT j FROM Job j WHERE " +
           "LOWER(j.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(j.platform) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(j.project) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Job> searchJobs(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT j FROM Job j WHERE " +
           "LOWER(j.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(j.platform) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(j.project) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Job> searchJobs(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Keyword-based search
    @Query("SELECT DISTINCT j FROM Job j JOIN j.keywords k WHERE LOWER(k) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Job> findByKeywordContaining(@Param("keyword") String keyword);
    
    // Pagination queries
    Page<Job> findByPlatform(String platform, Pageable pageable);
    
    Page<Job> findByProject(String project, Pageable pageable);
    
    // Statistics queries
    @Query("SELECT COUNT(j) FROM Job j WHERE j.platform = :platform")
    long countByPlatform(@Param("platform") String platform);
    
    @Query("SELECT COUNT(j) FROM Job j WHERE j.project = :project")
    long countByProject(@Param("project") String project);
    
    @Query("SELECT COUNT(j) FROM Job j WHERE j.externalReproductionTopic IS NOT NULL AND j.externalReproductionTopic != ''")
    long countExternalJobs();
    
    @Query("SELECT COUNT(j) FROM Job j WHERE j.customBinaryKey IS NOT NULL AND j.customBinaryKey != ''")
    long countJobsWithCustomBinary();
    
    // Distinct value queries for filtering
    @Query("SELECT DISTINCT j.platform FROM Job j WHERE j.platform IS NOT NULL ORDER BY j.platform")
    List<String> findDistinctPlatforms();
    
    @Query("SELECT DISTINCT j.project FROM Job j WHERE j.project IS NOT NULL ORDER BY j.project")
    List<String> findDistinctProjects();
    
    // Environment-related queries
    @Query("SELECT j FROM Job j WHERE j.environmentString LIKE CONCAT('%', :envVar, '%')")
    List<Job> findByEnvironmentVariable(@Param("envVar") String envVar);
    
    // Batch operations support
    @Query("SELECT j FROM Job j WHERE j.name IN :names")
    List<Job> findByNameIn(@Param("names") List<String> names);
    
    @Query("SELECT j FROM Job j WHERE j.id IN :ids")
    List<Job> findByIdIn(@Param("ids") List<Long> ids);
    
    // Exists queries for performance
    boolean existsByName(String name);
    
    boolean existsByPlatformAndProject(String platform, String project);
    
    boolean existsByExternalReproductionTopic(String topic);
    
    boolean existsByExternalUpdatesSubscription(String subscription);
    
    // Complex business logic queries
    @Query("SELECT j FROM Job j WHERE j.platform = :platform AND " +
           "(j.customBinaryKey IS NULL OR j.customBinaryKey = '') AND " +
           "(j.externalReproductionTopic IS NULL OR j.externalReproductionTopic = '')")
    List<Job> findStandardJobsByPlatform(@Param("platform") String platform);
    
    @Query("SELECT j FROM Job j WHERE j.project = :project AND " +
           "j.externalReproductionTopic IS NOT NULL AND j.externalReproductionTopic != ''")
    List<Job> findExternalJobsByProject(@Param("project") String project);
    
    // Ordering queries
    List<Job> findAllByOrderByNameAsc();
    
    List<Job> findByPlatformOrderByNameAsc(String platform);
    
    List<Job> findByProjectOrderByNameAsc(String project);
}