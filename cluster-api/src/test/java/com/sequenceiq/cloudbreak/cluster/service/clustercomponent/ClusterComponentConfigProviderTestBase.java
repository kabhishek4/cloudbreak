package com.sequenceiq.cloudbreak.cluster.service.clustercomponent;

import static com.sequenceiq.cloudbreak.api.endpoint.v4.common.Status.DELETE_COMPLETED;
import static com.sequenceiq.cloudbreak.cluster.common.TestUtil.getEmptyJson;
import static java.util.Collections.emptySet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import com.sequenceiq.cloudbreak.cluster.service.ClusterComponentConfigProvider;
import com.sequenceiq.cloudbreak.common.json.Json;
import com.sequenceiq.cloudbreak.domain.stack.cluster.ClusterComponentHistory;
import com.sequenceiq.cloudbreak.repository.ClusterComponentHistoryRepository;
import com.sequenceiq.cloudbreak.repository.ClusterComponentRepository;
import com.sequenceiq.cloudbreak.repository.ClusterComponentViewRepository;
import org.hibernate.envers.AuditReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ClusterComponentConfigProviderTestBase {

    protected static final Json MOCK_JSON = getEmptyJson();

    @Mock
    private ClusterComponentRepository mockComponentRepository;

    @Mock
    private ClusterComponentViewRepository mockComponentViewRepository;

    @Mock
    private ClusterComponentHistoryRepository mockClusterComponentHistoryRepository;

    @Mock
    private AuditReader mockAuditReader;

    private ClusterComponentConfigProvider underTest;

    @BeforeEach
    void setUp() {
        underTest = new ClusterComponentConfigProvider(mockComponentRepository, mockComponentViewRepository, mockClusterComponentHistoryRepository,
                mockAuditReader);
    }

    @Test
    void testCleanUpDetachedEntriesWhenThereAreOrphanedEntriesThenDeletionShouldHappen() {
        when(mockClusterComponentHistoryRepository.findDetachedEntries()).thenReturn(Set.of(new ClusterComponentHistory()));

        underTest.cleanUpDetachedEntries();

        verify(mockClusterComponentHistoryRepository, times(1)).deleteByClusterStatus(any());
        verify(mockClusterComponentHistoryRepository, times(1)).deleteByClusterStatus(DELETE_COMPLETED);
        verify(mockClusterComponentHistoryRepository, times(1)).deleteByClusterIsNull();
    }

    @Test
    void testCleanUpDetachedEntriesWhenThereAreNoOrphanedEntriesThenDeletionShouldNotHappen() {
        when(mockClusterComponentHistoryRepository.findDetachedEntries()).thenReturn(emptySet());

        underTest.cleanUpDetachedEntries();

        verify(mockClusterComponentHistoryRepository, never()).deleteByClusterStatus(any());
        verify(mockClusterComponentHistoryRepository, never()).deleteByClusterIsNull();
    }

    public ClusterComponentRepository getMockComponentRepository() {
        return mockComponentRepository;
    }

    public ClusterComponentViewRepository getMockComponentViewRepository() {
        return mockComponentViewRepository;
    }

    public ClusterComponentHistoryRepository getMockClusterComponentHistoryRepository() {
        return mockClusterComponentHistoryRepository;
    }

    public AuditReader getMockAuditReader() {
        return mockAuditReader;
    }

    public ClusterComponentConfigProvider getUnderTest() {
        return underTest;
    }

}
