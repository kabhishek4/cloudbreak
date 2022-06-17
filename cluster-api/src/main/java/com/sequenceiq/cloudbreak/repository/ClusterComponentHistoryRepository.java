package com.sequenceiq.cloudbreak.repository;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import java.util.Set;

import com.sequenceiq.cloudbreak.api.endpoint.v4.common.Status;
import com.sequenceiq.cloudbreak.domain.stack.cluster.ClusterComponentHistory;
import com.sequenceiq.cloudbreak.workspace.repository.EntityType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

@Transactional(TxType.REQUIRED)
@EntityType(entityClass = ClusterComponentHistory.class)
public interface ClusterComponentHistoryRepository extends CrudRepository<ClusterComponentHistory, Long> {

    @Query("SELECT cv FROM ClusterComponentHistory cv WHERE cv.cluster is NULL OR cv.cluster.status = 'DELETE_COMPLETED'")
    Set<ClusterComponentHistory> findDetachedEntries();

    void deleteByClusterIsNull();

    void deleteByClusterStatus(Status status);

}