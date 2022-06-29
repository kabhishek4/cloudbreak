package com.sequenceiq.cloudbreak.cloud.azure.connector.resource;

import java.util.List;

import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource;
import com.sequenceiq.cloudbreak.cloud.model.DatabaseStack;
import com.sequenceiq.cloudbreak.cloud.notification.PersistenceNotifier;
import com.sequenceiq.cloudbreak.cloud.template.compute.DatabaseServerUpgradeService;
import com.sequenceiq.cloudbreak.common.database.TargetMajorVersion;

//@Component
public class AzureDatabaseServerUpgradeService implements DatabaseServerUpgradeService {

//    @Inject
//    private ClusterApiConnectors apiConnectors;

    @Override
    public List<CloudResource> upgrade(AuthenticatedContext ac, DatabaseStack stack, PersistenceNotifier resourceNotifier, TargetMajorVersion databaseVersion) throws Exception {
        return null;
    }
}
