/*
 * Copyright (C) 2012-2014 DuyHai DOAN
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package info.archinnov.achilles.test.integration.tests.bugs;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import info.archinnov.achilles.junit.AchillesTestResource.Steps;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.test.integration.AchillesInternalCQLResource;
import info.archinnov.achilles.test.integration.entity.JobExecutionPopulationEntity;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static info.archinnov.achilles.test.integration.entity.JobExecutionPopulationEntity.JobExecutionPopulationKey;
import static org.fest.assertions.api.Assertions.assertThat;

public class NPEIT {

    @Rule
    public AchillesInternalCQLResource resource = new AchillesInternalCQLResource(Steps.AFTER_TEST, "job_execution_population");

    private PersistenceManager manager = resource.getPersistenceManager();

    @Test
    public void should_insert_and_typed_query() throws Exception {
        //Given
        UUID jobId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        Integer generation1 = RandomUtils.nextInt(0, Integer.MAX_VALUE);
        Integer generation2 = RandomUtils.nextInt(0, Integer.MAX_VALUE);
        Long fitness1 = RandomUtils.nextLong(0, Long.MAX_VALUE);
        Long fitness2 = RandomUtils.nextLong(0, Long.MAX_VALUE);
        int limit = 100;

        JobExecutionPopulationKey pk1 = new JobExecutionPopulationKey(jobId, executionId, generation1);
        JobExecutionPopulationKey pk2 = new JobExecutionPopulationKey(jobId, executionId, generation2);
        JobExecutionPopulationEntity entity1 = new JobExecutionPopulationEntity(pk1, fitness1.doubleValue());
        JobExecutionPopulationEntity entity2 = new JobExecutionPopulationEntity(pk2, fitness2.doubleValue());

        manager.insert(entity1);
        manager.insert(entity2);

        List<JobExecutionPopulationEntity> entities = manager.sliceQuery(JobExecutionPopulationEntity.class)
                .forSelect()
                .withPartitionComponents(jobId, executionId)
                .fromClusterings(generation1)
                .withExclusiveBounds()
                .limit(limit)
                .get();

        assertThat(entities).hasSize(1);
    }
    

}
