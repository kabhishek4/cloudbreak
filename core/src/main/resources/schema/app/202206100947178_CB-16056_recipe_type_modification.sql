-- // CB-16056 recipe type modification
-- Migration SQL that makes the change goes here.

UPDATE recipe set recipeType='PRE_CLOUDERA_MANAGER_START' where recipetype='PRE_SERVICE_DEPLOYMENT';
UPDATE recipe set recipeType='POST_CLUSTER_INSTALL' where recipetype='POST_SERVICE_DEPLOYMENT';

-- //@UNDO
-- SQL to undo the change goes here.

UPDATE recipe set recipeType='PRE_SERVICE_DEPLOYMENT' where recipeType='PRE_CLOUDERA_MANAGER_START';
UPDATE recipe set recipeType='POST_SERVICE_DEPLOYMENT' where recipeType='POST_CLUSTER_INSTALL';