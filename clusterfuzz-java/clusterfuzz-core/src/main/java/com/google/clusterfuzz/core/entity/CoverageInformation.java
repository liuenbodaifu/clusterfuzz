package com.google.clusterfuzz.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Coverage information for fuzzing campaigns.
 * Tracks function coverage, edge coverage, corpus size, and quarantine information.
 */
@Entity
@Table(name = "coverage_information", indexes = {
    @Index(name = "idx_coverage_date", columnList = "date"),
    @Index(name = "idx_coverage_fuzzer", columnList = "fuzzer"),
    @Index(name = "idx_coverage_date_fuzzer", columnList = "date, fuzzer"),
    @Index(name = "idx_coverage_functions_covered", columnList = "functionsCovered"),
    @Index(name = "idx_coverage_edges_covered", columnList = "edgesCovered")
})
@EntityListeners(AuditingEntityListener.class)
public class CoverageInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Date when coverage was measured.
     */
    @CreatedDate
    @Column(name = "date", nullable = false)
    private LocalDate date;

    /**
     * Fuzzer name that generated this coverage.
     */
    @NotNull
    @Column(name = "fuzzer", nullable = false, length = 255)
    private String fuzzer;

    // Function coverage information
    /**
     * Number of functions covered by fuzzing.
     */
    @Min(0)
    @Column(name = "functions_covered")
    private Integer functionsCovered;

    /**
     * Total number of functions in the target.
     */
    @Min(0)
    @Column(name = "functions_total")
    private Integer functionsTotal;

    // Edge coverage information
    /**
     * Number of edges covered by fuzzing.
     */
    @Min(0)
    @Column(name = "edges_covered")
    private Integer edgesCovered;

    /**
     * Total number of edges in the target.
     */
    @Min(0)
    @Column(name = "edges_total")
    private Integer edgesTotal;

    // Corpus size information
    /**
     * Number of units in the corpus.
     */
    @Min(0)
    @Column(name = "corpus_size_units")
    private Integer corpusSizeUnits;

    /**
     * Size of corpus in bytes.
     */
    @Min(0)
    @Column(name = "corpus_size_bytes")
    private Integer corpusSizeBytes;

    /**
     * Location of the corpus.
     */
    @Column(name = "corpus_location", length = 500)
    private String corpusLocation;

    /**
     * Location of corpus backup.
     */
    @Column(name = "corpus_backup_location", length = 500)
    private String corpusBackupLocation;

    // Quarantine size information
    /**
     * Number of units in quarantine.
     */
    @Min(0)
    @Column(name = "quarantine_size_units")
    private Integer quarantineSizeUnits;

    /**
     * Size of quarantine in bytes.
     */
    @Min(0)
    @Column(name = "quarantine_size_bytes")
    private Integer quarantineSizeBytes;

    /**
     * Location of quarantine files.
     */
    @Column(name = "quarantine_location", length = 500)
    private String quarantineLocation;

    /**
     * URL to HTML coverage report.
     */
    @Column(name = "html_report_url", length = 1000)
    private String htmlReportUrl;

    /**
     * Timestamp when this record was created.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Constructors
    public CoverageInformation() {}

    public CoverageInformation(String fuzzer) {
        this.fuzzer = fuzzer;
        this.date = LocalDate.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getFuzzer() {
        return fuzzer;
    }

    public void setFuzzer(String fuzzer) {
        this.fuzzer = fuzzer;
    }

    public Integer getFunctionsCovered() {
        return functionsCovered;
    }

    public void setFunctionsCovered(Integer functionsCovered) {
        this.functionsCovered = functionsCovered;
    }

    public Integer getFunctionsTotal() {
        return functionsTotal;
    }

    public void setFunctionsTotal(Integer functionsTotal) {
        this.functionsTotal = functionsTotal;
    }

    public Integer getEdgesCovered() {
        return edgesCovered;
    }

    public void setEdgesCovered(Integer edgesCovered) {
        this.edgesCovered = edgesCovered;
    }

    public Integer getEdgesTotal() {
        return edgesTotal;
    }

    public void setEdgesTotal(Integer edgesTotal) {
        this.edgesTotal = edgesTotal;
    }

    public Integer getCorpusSizeUnits() {
        return corpusSizeUnits;
    }

    public void setCorpusSizeUnits(Integer corpusSizeUnits) {
        this.corpusSizeUnits = corpusSizeUnits;
    }

    public Integer getCorpusSizeBytes() {
        return corpusSizeBytes;
    }

    public void setCorpusSizeBytes(Integer corpusSizeBytes) {
        this.corpusSizeBytes = corpusSizeBytes;
    }

    public String getCorpusLocation() {
        return corpusLocation;
    }

    public void setCorpusLocation(String corpusLocation) {
        this.corpusLocation = corpusLocation;
    }

    public String getCorpusBackupLocation() {
        return corpusBackupLocation;
    }

    public void setCorpusBackupLocation(String corpusBackupLocation) {
        this.corpusBackupLocation = corpusBackupLocation;
    }

    public Integer getQuarantineSizeUnits() {
        return quarantineSizeUnits;
    }

    public void setQuarantineSizeUnits(Integer quarantineSizeUnits) {
        this.quarantineSizeUnits = quarantineSizeUnits;
    }

    public Integer getQuarantineSizeBytes() {
        return quarantineSizeBytes;
    }

    public void setQuarantineSizeBytes(Integer quarantineSizeBytes) {
        this.quarantineSizeBytes = quarantineSizeBytes;
    }

    public String getQuarantineLocation() {
        return quarantineLocation;
    }

    public void setQuarantineLocation(String quarantineLocation) {
        this.quarantineLocation = quarantineLocation;
    }

    public String getHtmlReportUrl() {
        return htmlReportUrl;
    }

    public void setHtmlReportUrl(String htmlReportUrl) {
        this.htmlReportUrl = htmlReportUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Utility methods
    public double getFunctionCoveragePercentage() {
        if (functionsTotal == null || functionsTotal == 0) {
            return 0.0;
        }
        return (functionsCovered != null ? functionsCovered : 0) * 100.0 / functionsTotal;
    }

    public double getEdgeCoveragePercentage() {
        if (edgesTotal == null || edgesTotal == 0) {
            return 0.0;
        }
        return (edgesCovered != null ? edgesCovered : 0) * 100.0 / edgesTotal;
    }

    public boolean hasCorpus() {
        return corpusSizeUnits != null && corpusSizeUnits > 0;
    }

    public boolean hasQuarantine() {
        return quarantineSizeUnits != null && quarantineSizeUnits > 0;
    }

    public boolean hasHtmlReport() {
        return htmlReportUrl != null && !htmlReportUrl.trim().isEmpty();
    }

    public String getCorpusSizeFormatted() {
        if (corpusSizeBytes == null) return "0 bytes";
        return formatBytes(corpusSizeBytes);
    }

    public String getQuarantineSizeFormatted() {
        if (quarantineSizeBytes == null) return "0 bytes";
        return formatBytes(quarantineSizeBytes);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " bytes";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    @Override
    public String toString() {
        return "CoverageInformation{" +
                "id=" + id +
                ", date=" + date +
                ", fuzzer='" + fuzzer + '\'' +
                ", functionsCovered=" + functionsCovered +
                ", functionsTotal=" + functionsTotal +
                ", edgesCovered=" + edgesCovered +
                ", edgesTotal=" + edgesTotal +
                ", corpusSizeUnits=" + corpusSizeUnits +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CoverageInformation)) return false;
        CoverageInformation that = (CoverageInformation) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}