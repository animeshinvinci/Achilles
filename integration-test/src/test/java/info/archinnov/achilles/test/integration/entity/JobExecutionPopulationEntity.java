package info.archinnov.achilles.test.integration.entity;

import info.archinnov.achilles.annotations.*;
import info.archinnov.achilles.type.ConsistencyLevel;
import info.archinnov.achilles.type.InsertStrategy;

import java.util.Map;
import java.util.UUID;

@Entity(table="job_execution_population")
@Consistency(read=ConsistencyLevel.ONE,write= ConsistencyLevel.QUORUM)
@Strategy(insert = InsertStrategy.NOT_NULL_FIELDS)
public class JobExecutionPopulationEntity {

    @EmbeddedId
    private JobExecutionPopulationKey id;

    public static class JobExecutionPopulationKey
    {
        @PartitionKey
        @Order(1)
        @Column(name="job_id")
        private UUID jobId = null;

        @PartitionKey
        @Order(2)
        @Column(name="execution_id")
        private UUID executionId = null;

        @Order(3)
        @Column(name="generation_number")
        private Integer generationNumber = null;

        public JobExecutionPopulationKey(){
        }
        public JobExecutionPopulationKey(UUID jobId, UUID executionId, Integer generationNumber) {
            super();
            this.jobId = jobId;
            this.executionId = executionId;
            this.generationNumber = generationNumber;
        }

        public UUID getJobId() {
            return jobId;
        }
        public void setJobId(UUID jobId) {
            this.jobId = jobId;
        }
        public UUID getExecutionId() {
            return executionId;
        }
        public void setExecutionId(UUID executionId) {
            this.executionId = executionId;
        }
        public Integer getGenerationNumber() {
            return generationNumber;
        }
        public void setGenerationNumber(Integer generationNumber) {
            this.generationNumber = generationNumber;
        }
    }

    @Column(name="best_candidate_fitness")
    private Double bestCandidateFitness = null;
    @Column(name="best_candidate_params")
    private Map<String,String> bestCandidateParams = null;
    @Column(name="mean_fitness")
    private Double meanFitness = null;
    @Column(name="elapsed_time")
    private Long elapsedTime = null;
    @Column(name="fitness_standard_deviation")
    private Double fitnessStandardDeviation = null;

    public JobExecutionPopulationEntity(){
    }

    public JobExecutionPopulationEntity(JobExecutionPopulationKey id, Double bestCandidateFitness) {
        this.id = id;
        this.bestCandidateFitness = bestCandidateFitness;
    }

    public JobExecutionPopulationKey getId() {
        return id;
    }

    public void setId(JobExecutionPopulationKey id) {
        this.id = id;
    }

    public Double getBestCandidateFitness() {
        return bestCandidateFitness;
    }

    public void setBestCandidateFitness(Double bestCandidateFitness) {
        this.bestCandidateFitness = bestCandidateFitness;
    }

    public Map<String, String> getBestCandidateParams() {
        return bestCandidateParams;
    }

    public void setBestCandidateParams(Map<String, String> bestCandidateParams) {
        this.bestCandidateParams = bestCandidateParams;
    }

    public Double getMeanFitness() {
        return meanFitness;
    }

    public void setMeanFitness(Double meanFitness) {
        this.meanFitness = meanFitness;
    }

    public Long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(Long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public Double getFitnessStandardDeviation() {
        return fitnessStandardDeviation;
    }

    public void setFitnessStandardDeviation(Double fitnessStandardDeviation) {
        this.fitnessStandardDeviation = fitnessStandardDeviation;
    }
}