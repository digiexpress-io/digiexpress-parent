import { Hook as RootHook } from './fetch/__root'
import { Hook as orgGroupMembershipGET } from './fetch/$org.groupMembership.get.ts'
import { Hook as orgGroupsListGET } from './fetch/$org.groupsList.get.ts'
import { Hook as orgUserInfoGET } from './fetch/$org.userInfo.get.ts'
import { Hook as configGET } from './fetch/config.get.ts'
import { Hook as dialobGET } from './fetch/dialob.get.ts'
import { Hook as workerRestApiAssetsDeploymentsDeploymentIdGET } from './fetch/worker.rest.api.assets.deployments.$deploymentId.get.ts'
import { Hook as workerRestApiAssetsDeploymentsDeploymentIdPUT } from './fetch/worker.rest.api.assets.deployments.$deploymentId.put.ts'
import { Hook as workerRestApiAssetsDeploymentsPOST } from './fetch/worker.rest.api.assets.deployments.post.ts'
import { Hook as workerRestApiAssetsDialobFillSessionIdGET } from './fetch/worker.rest.api.assets.dialob.fill.$sessionId.get.ts'
import { Hook as workerRestApiAssetsDialobFillSessionIdPOST } from './fetch/worker.rest.api.assets.dialob.fill.$sessionId.post.ts'
import { Hook as workerRestApiAssetsDialobGET } from './fetch/worker.rest.api.assets.dialob.get.ts'
import { Hook as workerRestApiAssetsDialobProxyFormsFormIdDELETE } from './fetch/worker.rest.api.assets.dialob.proxy.forms.$formId.delete.ts'
import { Hook as workerRestApiAssetsDialobProxyFormsFormIdGET } from './fetch/worker.rest.api.assets.dialob.proxy.forms.$formId.get.ts'
import { Hook as workerRestApiAssetsDialobProxyFormsPOST } from './fetch/worker.rest.api.assets.dialob.proxy.forms.post.ts'
import { Hook as workerRestApiAssetsDialobTagsGET } from './fetch/worker.rest.api.assets.dialob.tags.get.ts'
import { Hook as workerRestApiAssetsFsDebugsPOST } from './fetch/worker.rest.api.assets.fs.debugs.post.ts'
import { Hook as workerRestApiAssetsFsDirentsIdBodiesBodyTypeGET } from './fetch/worker.rest.api.assets.fs.dirents.$id.bodies.$bodyType.get.ts'
import { Hook as workerRestApiAssetsFsDirentsIdBodiesBodyTypeTransientChangesPOST } from './fetch/worker.rest.api.assets.fs.dirents.$id.bodies.$bodyType.transient-changes.post.ts'
import { Hook as workerRestApiAssetsFsDirentsPOST } from './fetch/worker.rest.api.assets.fs.dirents.post.ts'
import { Hook as workerRestApiAssetsFsDirentsPUT } from './fetch/worker.rest.api.assets.fs.dirents.put.ts'
import { Hook as workerRestApiAssetsFsGET } from './fetch/worker.rest.api.assets.fs.get.ts'
import { Hook as workerRestApiAssetsMigrationPOST } from './fetch/worker.rest.api.assets.migration.post.ts'
import { Hook as workerRestApiAssetsPublicationsGET } from './fetch/worker.rest.api.assets.publications.get.ts'
import { Hook as workerRestApiAssetsPublicationsPOST } from './fetch/worker.rest.api.assets.publications.post.ts'
import { Hook as workerRestApiAssetsStencilAssetTypeDELETE } from './fetch/worker.rest.api.assets.stencil.$assetType.delete.ts'
import { Hook as workerRestApiAssetsStencilAssetTypePOST } from './fetch/worker.rest.api.assets.stencil.$assetType.post.ts'
import { Hook as workerRestApiAssetsStencilAssetTypePUT } from './fetch/worker.rest.api.assets.stencil.$assetType.put.ts'
import { Hook as workerRestApiAssetsStencilCommitlogsGET } from './fetch/worker.rest.api.assets.stencil.commitlogs.get.ts'
import { Hook as workerRestApiAssetsStencilGET } from './fetch/worker.rest.api.assets.stencil.get.ts'
import { Hook as workerRestApiAssetsStencilReleasesReleaseIdGET } from './fetch/worker.rest.api.assets.stencil.releases.$releaseId.get.ts'
import { Hook as workerRestApiAssetsTagomiGET } from './fetch/worker.rest.api.assets.tagomi.get.ts'
import { Hook as workerRestApiAssetsWorkflowsWorkflowIdPUT } from './fetch/worker.rest.api.assets.workflows.$workflowId.put.ts'
import { Hook as workerRestApiAssetsWorkflowsGET } from './fetch/worker.rest.api.assets.workflows.get.ts'
import { Hook as workerRestApiAssetsWrenchCommandsIdGET } from './fetch/worker.rest.api.assets.wrench.commands.$id.get.ts'
import { Hook as workerRestApiAssetsWrenchCommandsPOST } from './fetch/worker.rest.api.assets.wrench.commands.post.ts'
import { Hook as workerRestApiAssetsWrenchCommitlogsGET } from './fetch/worker.rest.api.assets.wrench.commitlogs.get.ts'
import { Hook as workerRestApiAssetsWrenchCopyasPOST } from './fetch/worker.rest.api.assets.wrench.copyas.post.ts'
import { Hook as workerRestApiAssetsWrenchDataModelsGET } from './fetch/worker.rest.api.assets.wrench.dataModels.get.ts'
import { Hook as workerRestApiAssetsWrenchDebugsPOST } from './fetch/worker.rest.api.assets.wrench.debugs.post.ts'
import { Hook as workerRestApiAssetsWrenchDiffGET } from './fetch/worker.rest.api.assets.wrench.diff.get.ts'
import { Hook as workerRestApiAssetsWrenchFlowNamesGET } from './fetch/worker.rest.api.assets.wrench.flow-names.get.ts'
import { Hook as workerRestApiAssetsWrenchImportTagPOST } from './fetch/worker.rest.api.assets.wrench.importTag.post.ts'
import { Hook as workerRestApiAssetsWrenchResourcesIdDELETE } from './fetch/worker.rest.api.assets.wrench.resources.$id.delete.ts'
import { Hook as workerRestApiAssetsWrenchResourcesPOST } from './fetch/worker.rest.api.assets.wrench.resources.post.ts'
import { Hook as workerRestApiAssetsWrenchResourcesPUT } from './fetch/worker.rest.api.assets.wrench.resources.put.ts'
import { Hook as workerRestApiAssetsWrenchSummaryTagIdGET } from './fetch/worker.rest.api.assets.wrench.summary.$tagId.get.ts'
import { Hook as workerRestApiBatchesBatchNameInstancesPOST } from './fetch/worker.rest.api.batches.$batchName.instances.post.ts'
import { Hook as workerRestApiBatchesGET } from './fetch/worker.rest.api.batches.get.ts'
import { Hook as workerRestApiBatchesStepsStepIdGET } from './fetch/worker.rest.api.batches.steps.$stepId.get.ts'
import { Hook as workerRestApiCockpitsCockpitIdGET } from './fetch/worker.rest.api.cockpits.$cockpitId.get.ts'
import { Hook as workerRestApiCockpitsCockpitIdTenantsPOST } from './fetch/worker.rest.api.cockpits.$cockpitId.tenants.post.ts'
import { Hook as workerRestApiCockpitsActivityCurrentStatePOST } from './fetch/worker.rest.api.cockpits.activity.current-state.post.ts'
import { Hook as workerRestApiCockpitsGET } from './fetch/worker.rest.api.cockpits.get.ts'
import { Hook as workerRestApiCockpitsPOST } from './fetch/worker.rest.api.cockpits.post.ts'
import { Hook as workerRestApiContractsGET } from './fetch/worker.rest.api.contracts.get.ts'
import { Hook as workerRestApiFeedbackFeedbackIdDELETE } from './fetch/worker.rest.api.feedback.$feedbackId.delete.ts'
import { Hook as workerRestApiFeedbackFeedbackIdEnabledGET } from './fetch/worker.rest.api.feedback.$feedbackId.enabled.get.ts'
import { Hook as workerRestApiFeedbackFeedbackIdGET } from './fetch/worker.rest.api.feedback.$feedbackId.get.ts'
import { Hook as workerRestApiFeedbackFeedbackIdPOST } from './fetch/worker.rest.api.feedback.$feedbackId.post.ts'
import { Hook as workerRestApiFeedbackFeedbackIdPUT } from './fetch/worker.rest.api.feedback.$feedbackId.put.ts'
import { Hook as workerRestApiFeedbackFeedbackIdSentimentAndSubcategoryGET } from './fetch/worker.rest.api.feedback.$feedbackId.sentiment-and-subcategory.get.ts'
import { Hook as workerRestApiFeedbackFeedbackIdSimilarGET } from './fetch/worker.rest.api.feedback.$feedbackId.similar.get.ts'
import { Hook as workerRestApiFeedbackFeedbackIdTemplatesGET } from './fetch/worker.rest.api.feedback.$feedbackId.templates.get.ts'
import { Hook as workerRestApiFeedbackGET } from './fetch/worker.rest.api.feedback.get.ts'
import { Hook as workerRestApiHealthGET } from './fetch/worker.rest.api.health.get.ts'
import { Hook as workerRestApiIamLivenessGET } from './fetch/worker.rest.api.iam.liveness.ts'
import { Hook as workerRestApiLedgersGET } from './fetch/worker.rest.api.ledgers.get.ts'
import { Hook as workerRestApiPdfPOST } from './fetch/worker.rest.api.pdf.post.ts'
import { Hook as workerRestApiProcessesGET } from './fetch/worker.rest.api.processes.get.ts'
import { Hook as workerRestApiQueuesConfigsGET } from './fetch/worker.rest.api.queues.configs.get.ts'
import { Hook as workerRestApiQueuesDeliveriesGET } from './fetch/worker.rest.api.queues.deliveries.get.ts'
import { Hook as workerRestApiQueuesMessagesGET } from './fetch/worker.rest.api.queues.messages.get.ts'
import { Hook as workerRestApiTasksTaskIdAuditsGET } from './fetch/worker.rest.api.tasks.$taskId.audits.get.ts'
import { Hook as workerRestApiTasksTaskIdCommentsGET } from './fetch/worker.rest.api.tasks.$taskId.comments.get.ts'
import { Hook as workerRestApiTasksTaskIdCommentsPOST } from './fetch/worker.rest.api.tasks.$taskId.comments.post.ts'
import { Hook as workerRestApiTasksTaskIdDELETE } from './fetch/worker.rest.api.tasks.$taskId.delete.ts'
import { Hook as workerRestApiTasksTaskIdFilesFilenameDELETE } from './fetch/worker.rest.api.tasks.$taskId.files.$filename.delete.ts'
import { Hook as workerRestApiTasksTaskIdFilesFilenameGET } from './fetch/worker.rest.api.tasks.$taskId.files.$filename.get.ts'
import { Hook as workerRestApiTasksTaskIdFilesGET } from './fetch/worker.rest.api.tasks.$taskId.files.get.ts'
import { Hook as workerRestApiTasksTaskIdFilesPOST } from './fetch/worker.rest.api.tasks.$taskId.files.post.ts'
import { Hook as workerRestApiTasksTaskIdFormAssignmentsDELETE } from './fetch/worker.rest.api.tasks.$taskId.form-assignments.delete.ts'
import { Hook as workerRestApiTasksTaskIdFormAssignmentsGET } from './fetch/worker.rest.api.tasks.$taskId.form-assignments.get.ts'
import { Hook as workerRestApiTasksTaskIdFormAssignmentsPOST } from './fetch/worker.rest.api.tasks.$taskId.form-assignments.post.ts'
import { Hook as workerRestApiTasksTaskIdGET } from './fetch/worker.rest.api.tasks.$taskId.get.ts'
import { Hook as workerRestApiTasksTaskIdPUT } from './fetch/worker.rest.api.tasks.$taskId.put.ts'
import { Hook as workerRestApiTasksTaskIdReviewActionsGET } from './fetch/worker.rest.api.tasks.$taskId.review-actions.ts'
import { Hook as workerRestApiTasksTaskIdReviewsGET } from './fetch/worker.rest.api.tasks.$taskId.reviews.ts'
import { Hook as workerRestApiTasksTaskIdTransfersPUT } from './fetch/worker.rest.api.tasks.$taskId.transfers.put.ts'
import { Hook as workerRestApiTasksGET } from './fetch/worker.rest.api.tasks.get.ts'
import { Hook as workerRestApiTasksPOST } from './fetch/worker.rest.api.tasks.post.ts'
import { Hook as workerRestApiTasksUnreadGET } from './fetch/worker.rest.api.tasks.unread.get.ts'
import { Hook as workerRestApiTenantConfigsGET } from './fetch/worker.rest.api.tenant-configs.get.ts'
import { Hook as workerRestApiUserprofilesProfileIdGET } from './fetch/worker.rest.api.userprofiles.$profileId.get.ts'
import { Hook as workerRestApiVersionGET } from './fetch/worker.rest.api.version.get.ts'



const orgGroupMembershipGETRoute = orgGroupMembershipGET.update({
  path: '$org/groupMembership',
  method: 'GET',
})

const orgGroupsListGETRoute = orgGroupsListGET.update({
  path: '$org/groupsList',
  method: 'GET',
})

const orgUserInfoGETRoute = orgUserInfoGET.update({
  path: '$org/userInfo',
  method: 'GET',
})

const configGETRoute = configGET.update({
  path: 'config',
  method: 'GET',
})

const dialobGETRoute = dialobGET.update({
  path: 'dialob',
  method: 'GET',
})

const workerRestApiAssetsDeploymentsDeploymentIdGETRoute = workerRestApiAssetsDeploymentsDeploymentIdGET.update({
  path: 'worker/rest/api/assets/deployments/$deploymentId',
  method: 'GET',
})

const workerRestApiAssetsDeploymentsDeploymentIdPUTRoute = workerRestApiAssetsDeploymentsDeploymentIdPUT.update({
  path: 'worker/rest/api/assets/deployments/$deploymentId',
  method: 'PUT',
})

const workerRestApiAssetsDeploymentsPOSTRoute = workerRestApiAssetsDeploymentsPOST.update({
  path: 'worker/rest/api/assets/deployments',
  method: 'POST',
})

const workerRestApiAssetsDialobFillSessionIdGETRoute = workerRestApiAssetsDialobFillSessionIdGET.update({
  path: 'worker/rest/api/assets/dialob/fill/$sessionId',
  method: 'GET',
})

const workerRestApiAssetsDialobFillSessionIdPOSTRoute = workerRestApiAssetsDialobFillSessionIdPOST.update({
  path: 'worker/rest/api/assets/dialob/fill/$sessionId',
  method: 'POST',
})

const workerRestApiAssetsDialobGETRoute = workerRestApiAssetsDialobGET.update({
  path: 'worker/rest/api/assets/dialob',
  method: 'GET',
})

const workerRestApiAssetsDialobProxyFormsFormIdDELETERoute = workerRestApiAssetsDialobProxyFormsFormIdDELETE.update({
  path: 'worker/rest/api/assets/dialob/proxy/forms/$formId',
  method: 'DELETE',
})

const workerRestApiAssetsDialobProxyFormsFormIdGETRoute = workerRestApiAssetsDialobProxyFormsFormIdGET.update({
  path: 'worker/rest/api/assets/dialob/proxy/forms/$formId',
  method: 'GET',
})

const workerRestApiAssetsDialobProxyFormsPOSTRoute = workerRestApiAssetsDialobProxyFormsPOST.update({
  path: 'worker/rest/api/assets/dialob/proxy/forms',
  method: 'POST',
})

const workerRestApiAssetsDialobTagsGETRoute = workerRestApiAssetsDialobTagsGET.update({
  path: 'worker/rest/api/assets/dialob/tags',
  method: 'GET',
})

const workerRestApiAssetsFsDebugsPOSTRoute = workerRestApiAssetsFsDebugsPOST.update({
  path: 'worker/rest/api/assets/fs/debugs',
  method: 'POST',
})

const workerRestApiAssetsFsDirentsIdBodiesBodyTypeGETRoute = workerRestApiAssetsFsDirentsIdBodiesBodyTypeGET.update({
  path: 'worker/rest/api/assets/fs/dirents/$id/bodies/$bodyType',
  method: 'GET',
})

const workerRestApiAssetsFsDirentsIdBodiesBodyTypeTransientChangesPOSTRoute = workerRestApiAssetsFsDirentsIdBodiesBodyTypeTransientChangesPOST.update({
  path: 'worker/rest/api/assets/fs/dirents/$id/bodies/$bodyType/transient-changes',
  method: 'POST',
})

const workerRestApiAssetsFsDirentsPOSTRoute = workerRestApiAssetsFsDirentsPOST.update({
  path: 'worker/rest/api/assets/fs/dirents',
  method: 'POST',
})

const workerRestApiAssetsFsDirentsPUTRoute = workerRestApiAssetsFsDirentsPUT.update({
  path: 'worker/rest/api/assets/fs/dirents',
  method: 'PUT',
})

const workerRestApiAssetsFsGETRoute = workerRestApiAssetsFsGET.update({
  path: 'worker/rest/api/assets/fs',
  method: 'GET',
})

const workerRestApiAssetsMigrationPOSTRoute = workerRestApiAssetsMigrationPOST.update({
  path: 'worker/rest/api/assets/migration',
  method: 'POST',
})

const workerRestApiAssetsPublicationsGETRoute = workerRestApiAssetsPublicationsGET.update({
  path: 'worker/rest/api/assets/publications',
  method: 'GET',
})

const workerRestApiAssetsPublicationsPOSTRoute = workerRestApiAssetsPublicationsPOST.update({
  path: 'worker/rest/api/assets/publications',
  method: 'POST',
})

const workerRestApiAssetsStencilAssetTypeDELETERoute = workerRestApiAssetsStencilAssetTypeDELETE.update({
  path: 'worker/rest/api/assets/stencil/$assetType',
  method: 'DELETE',
})

const workerRestApiAssetsStencilAssetTypePOSTRoute = workerRestApiAssetsStencilAssetTypePOST.update({
  path: 'worker/rest/api/assets/stencil/$assetType',
  method: 'POST',
})

const workerRestApiAssetsStencilAssetTypePUTRoute = workerRestApiAssetsStencilAssetTypePUT.update({
  path: 'worker/rest/api/assets/stencil/$assetType',
  method: 'PUT',
})

const workerRestApiAssetsStencilCommitlogsGETRoute = workerRestApiAssetsStencilCommitlogsGET.update({
  path: 'worker/rest/api/assets/stencil/commitlogs',
  method: 'GET',
})

const workerRestApiAssetsStencilGETRoute = workerRestApiAssetsStencilGET.update({
  path: 'worker/rest/api/assets/stencil',
  method: 'GET',
})

const workerRestApiAssetsStencilReleasesReleaseIdGETRoute = workerRestApiAssetsStencilReleasesReleaseIdGET.update({
  path: 'worker/rest/api/assets/stencil/releases/$releaseId',
  method: 'GET',
})

const workerRestApiAssetsTagomiGETRoute = workerRestApiAssetsTagomiGET.update({
  path: 'worker/rest/api/assets/tagomi',
  method: 'GET',
})

const workerRestApiAssetsWorkflowsWorkflowIdPUTRoute = workerRestApiAssetsWorkflowsWorkflowIdPUT.update({
  path: 'worker/rest/api/assets/workflows/$workflowId',
  method: 'PUT',
})

const workerRestApiAssetsWorkflowsGETRoute = workerRestApiAssetsWorkflowsGET.update({
  path: 'worker/rest/api/assets/workflows',
  method: 'GET',
})

const workerRestApiAssetsWrenchCommandsIdGETRoute = workerRestApiAssetsWrenchCommandsIdGET.update({
  path: 'worker/rest/api/assets/wrench/commands/$id',
  method: 'GET',
})

const workerRestApiAssetsWrenchCommandsPOSTRoute = workerRestApiAssetsWrenchCommandsPOST.update({
  path: 'worker/rest/api/assets/wrench/commands',
  method: 'POST',
})

const workerRestApiAssetsWrenchCommitlogsGETRoute = workerRestApiAssetsWrenchCommitlogsGET.update({
  path: 'worker/rest/api/assets/wrench/commitlogs',
  method: 'GET',
})

const workerRestApiAssetsWrenchCopyasPOSTRoute = workerRestApiAssetsWrenchCopyasPOST.update({
  path: 'worker/rest/api/assets/wrench/copyas',
  method: 'POST',
})

const workerRestApiAssetsWrenchDataModelsGETRoute = workerRestApiAssetsWrenchDataModelsGET.update({
  path: 'worker/rest/api/assets/wrench/dataModels',
  method: 'GET',
})

const workerRestApiAssetsWrenchDebugsPOSTRoute = workerRestApiAssetsWrenchDebugsPOST.update({
  path: 'worker/rest/api/assets/wrench/debugs',
  method: 'POST',
})

const workerRestApiAssetsWrenchDiffGETRoute = workerRestApiAssetsWrenchDiffGET.update({
  path: 'worker/rest/api/assets/wrench/diff',
  method: 'GET',
})

const workerRestApiAssetsWrenchFlowNamesGETRoute = workerRestApiAssetsWrenchFlowNamesGET.update({
  path: 'worker/rest/api/assets/wrench/flow-names',
  method: 'GET',
})

const workerRestApiAssetsWrenchImportTagPOSTRoute = workerRestApiAssetsWrenchImportTagPOST.update({
  path: 'worker/rest/api/assets/wrench/importTag',
  method: 'POST',
})

const workerRestApiAssetsWrenchResourcesIdDELETERoute = workerRestApiAssetsWrenchResourcesIdDELETE.update({
  path: 'worker/rest/api/assets/wrench/resources/$id',
  method: 'DELETE',
})

const workerRestApiAssetsWrenchResourcesPOSTRoute = workerRestApiAssetsWrenchResourcesPOST.update({
  path: 'worker/rest/api/assets/wrench/resources',
  method: 'POST',
})

const workerRestApiAssetsWrenchResourcesPUTRoute = workerRestApiAssetsWrenchResourcesPUT.update({
  path: 'worker/rest/api/assets/wrench/resources',
  method: 'PUT',
})

const workerRestApiAssetsWrenchSummaryTagIdGETRoute = workerRestApiAssetsWrenchSummaryTagIdGET.update({
  path: 'worker/rest/api/assets/wrench/summary/$tagId',
  method: 'GET',
})

const workerRestApiBatchesBatchNameInstancesPOSTRoute = workerRestApiBatchesBatchNameInstancesPOST.update({
  path: 'worker/rest/api/batches/$batchName/instances',
  method: 'POST',
})

const workerRestApiBatchesGETRoute = workerRestApiBatchesGET.update({
  path: 'worker/rest/api/batches',
  method: 'GET',
})

const workerRestApiBatchesStepsStepIdGETRoute = workerRestApiBatchesStepsStepIdGET.update({
  path: 'worker/rest/api/batches/steps/$stepId',
  method: 'GET',
})

const workerRestApiCockpitsCockpitIdGETRoute = workerRestApiCockpitsCockpitIdGET.update({
  path: 'worker/rest/api/cockpits/$cockpitId',
  method: 'GET',
})

const workerRestApiCockpitsCockpitIdTenantsPOSTRoute = workerRestApiCockpitsCockpitIdTenantsPOST.update({
  path: 'worker/rest/api/cockpits/$cockpitId/tenants',
  method: 'POST',
})

const workerRestApiCockpitsActivityCurrentStatePOSTRoute = workerRestApiCockpitsActivityCurrentStatePOST.update({
  path: 'worker/rest/api/cockpits/activity/current-state',
  method: 'POST',
})

const workerRestApiCockpitsGETRoute = workerRestApiCockpitsGET.update({
  path: 'worker/rest/api/cockpits',
  method: 'GET',
})

const workerRestApiCockpitsPOSTRoute = workerRestApiCockpitsPOST.update({
  path: 'worker/rest/api/cockpits',
  method: 'POST',
})

const workerRestApiContractsGETRoute = workerRestApiContractsGET.update({
  path: 'worker/rest/api/contracts',
  method: 'GET',
})

const workerRestApiFeedbackFeedbackIdDELETERoute = workerRestApiFeedbackFeedbackIdDELETE.update({
  path: 'worker/rest/api/feedback/$feedbackId',
  method: 'DELETE',
})

const workerRestApiFeedbackFeedbackIdEnabledGETRoute = workerRestApiFeedbackFeedbackIdEnabledGET.update({
  path: 'worker/rest/api/feedback/$feedbackId/enabled',
  method: 'GET',
})

const workerRestApiFeedbackFeedbackIdGETRoute = workerRestApiFeedbackFeedbackIdGET.update({
  path: 'worker/rest/api/feedback/$feedbackId',
  method: 'GET',
})

const workerRestApiFeedbackFeedbackIdPOSTRoute = workerRestApiFeedbackFeedbackIdPOST.update({
  path: 'worker/rest/api/feedback/$feedbackId',
  method: 'POST',
})

const workerRestApiFeedbackFeedbackIdPUTRoute = workerRestApiFeedbackFeedbackIdPUT.update({
  path: 'worker/rest/api/feedback/$feedbackId',
  method: 'PUT',
})

const workerRestApiFeedbackFeedbackIdSentimentAndSubcategoryGETRoute = workerRestApiFeedbackFeedbackIdSentimentAndSubcategoryGET.update({
  path: 'worker/rest/api/feedback/$feedbackId/sentiment-and-subcategory',
  method: 'GET',
})

const workerRestApiFeedbackFeedbackIdSimilarGETRoute = workerRestApiFeedbackFeedbackIdSimilarGET.update({
  path: 'worker/rest/api/feedback/$feedbackId/similar',
  method: 'GET',
})

const workerRestApiFeedbackFeedbackIdTemplatesGETRoute = workerRestApiFeedbackFeedbackIdTemplatesGET.update({
  path: 'worker/rest/api/feedback/$feedbackId/templates',
  method: 'GET',
})

const workerRestApiFeedbackGETRoute = workerRestApiFeedbackGET.update({
  path: 'worker/rest/api/feedback',
  method: 'GET',
})

const workerRestApiHealthGETRoute = workerRestApiHealthGET.update({
  path: 'worker/rest/api/health',
  method: 'GET',
})

const workerRestApiIamLivenessGETRoute = workerRestApiIamLivenessGET.update({
  path: 'worker/rest/api/iam/liveness',
  method: 'GET',
})

const workerRestApiLedgersGETRoute = workerRestApiLedgersGET.update({
  path: 'worker/rest/api/ledgers',
  method: 'GET',
})

const workerRestApiPdfPOSTRoute = workerRestApiPdfPOST.update({
  path: 'worker/rest/api/pdf',
  method: 'POST',
})

const workerRestApiProcessesGETRoute = workerRestApiProcessesGET.update({
  path: 'worker/rest/api/processes',
  method: 'GET',
})

const workerRestApiQueuesConfigsGETRoute = workerRestApiQueuesConfigsGET.update({
  path: 'worker/rest/api/queues/configs',
  method: 'GET',
})

const workerRestApiQueuesDeliveriesGETRoute = workerRestApiQueuesDeliveriesGET.update({
  path: 'worker/rest/api/queues/deliveries',
  method: 'GET',
})

const workerRestApiQueuesMessagesGETRoute = workerRestApiQueuesMessagesGET.update({
  path: 'worker/rest/api/queues/messages',
  method: 'GET',
})

const workerRestApiTasksTaskIdAuditsGETRoute = workerRestApiTasksTaskIdAuditsGET.update({
  path: 'worker/rest/api/tasks/$taskId/audits',
  method: 'GET',
})

const workerRestApiTasksTaskIdCommentsGETRoute = workerRestApiTasksTaskIdCommentsGET.update({
  path: 'worker/rest/api/tasks/$taskId/comments',
  method: 'GET',
})

const workerRestApiTasksTaskIdCommentsPOSTRoute = workerRestApiTasksTaskIdCommentsPOST.update({
  path: 'worker/rest/api/tasks/$taskId/comments',
  method: 'POST',
})

const workerRestApiTasksTaskIdDELETERoute = workerRestApiTasksTaskIdDELETE.update({
  path: 'worker/rest/api/tasks/$taskId',
  method: 'DELETE',
})

const workerRestApiTasksTaskIdFilesFilenameDELETERoute = workerRestApiTasksTaskIdFilesFilenameDELETE.update({
  path: 'worker/rest/api/tasks/$taskId/files/$filename',
  method: 'DELETE',
})

const workerRestApiTasksTaskIdFilesFilenameGETRoute = workerRestApiTasksTaskIdFilesFilenameGET.update({
  path: 'worker/rest/api/tasks/$taskId/files/$filename',
  method: 'GET',
})

const workerRestApiTasksTaskIdFilesGETRoute = workerRestApiTasksTaskIdFilesGET.update({
  path: 'worker/rest/api/tasks/$taskId/files',
  method: 'GET',
})

const workerRestApiTasksTaskIdFilesPOSTRoute = workerRestApiTasksTaskIdFilesPOST.update({
  path: 'worker/rest/api/tasks/$taskId/files',
  method: 'POST',
})

const workerRestApiTasksTaskIdFormAssignmentsDELETERoute = workerRestApiTasksTaskIdFormAssignmentsDELETE.update({
  path: 'worker/rest/api/tasks/$taskId/form-assignments',
  method: 'DELETE',
})

const workerRestApiTasksTaskIdFormAssignmentsGETRoute = workerRestApiTasksTaskIdFormAssignmentsGET.update({
  path: 'worker/rest/api/tasks/$taskId/form-assignments',
  method: 'GET',
})

const workerRestApiTasksTaskIdFormAssignmentsPOSTRoute = workerRestApiTasksTaskIdFormAssignmentsPOST.update({
  path: 'worker/rest/api/tasks/$taskId/form-assignments',
  method: 'POST',
})

const workerRestApiTasksTaskIdGETRoute = workerRestApiTasksTaskIdGET.update({
  path: 'worker/rest/api/tasks/$taskId',
  method: 'GET',
})

const workerRestApiTasksTaskIdPUTRoute = workerRestApiTasksTaskIdPUT.update({
  path: 'worker/rest/api/tasks/$taskId',
  method: 'PUT',
})

const workerRestApiTasksTaskIdReviewActionsGETRoute = workerRestApiTasksTaskIdReviewActionsGET.update({
  path: 'worker/rest/api/tasks/$taskId/review-actions',
  method: 'GET',
})

const workerRestApiTasksTaskIdReviewsGETRoute = workerRestApiTasksTaskIdReviewsGET.update({
  path: 'worker/rest/api/tasks/$taskId/reviews',
  method: 'GET',
})

const workerRestApiTasksTaskIdTransfersPUTRoute = workerRestApiTasksTaskIdTransfersPUT.update({
  path: 'worker/rest/api/tasks/$taskId/transfers',
  method: 'PUT',
})

const workerRestApiTasksGETRoute = workerRestApiTasksGET.update({
  path: 'worker/rest/api/tasks',
  method: 'GET',
})

const workerRestApiTasksPOSTRoute = workerRestApiTasksPOST.update({
  path: 'worker/rest/api/tasks',
  method: 'POST',
})

const workerRestApiTasksUnreadGETRoute = workerRestApiTasksUnreadGET.update({
  path: 'worker/rest/api/tasks/unread',
  method: 'GET',
})

const workerRestApiTenantConfigsGETRoute = workerRestApiTenantConfigsGET.update({
  path: 'worker/rest/api/tenant-configs',
  method: 'GET',
})

const workerRestApiUserprofilesProfileIdGETRoute = workerRestApiUserprofilesProfileIdGET.update({
  path: 'worker/rest/api/userprofiles/$profileId',
  method: 'GET',
})

const workerRestApiVersionGETRoute = workerRestApiVersionGET.update({
  path: 'worker/rest/api/version',
  method: 'GET',
})



declare module '@dxs-ts/envir-fetch' {
  interface HookByPath {

    '$org/groupMembership.GET': {
      id: '$org/groupMembership.GET',
      path: '$org/groupMembership',
      method: 'GET',
      params: {org: string},
      hook: typeof orgGroupMembershipGETRoute
    }

    '$org/groupsList.GET': {
      id: '$org/groupsList.GET',
      path: '$org/groupsList',
      method: 'GET',
      params: {org: string},
      hook: typeof orgGroupsListGETRoute
    }

    '$org/userInfo.GET': {
      id: '$org/userInfo.GET',
      path: '$org/userInfo',
      method: 'GET',
      params: {org: string},
      hook: typeof orgUserInfoGETRoute
    }

    'config.GET': {
      id: 'config.GET',
      path: 'config',
      method: 'GET',
      params: {},
      hook: typeof configGETRoute
    }

    'dialob.GET': {
      id: 'dialob.GET',
      path: 'dialob',
      method: 'GET',
      params: {},
      hook: typeof dialobGETRoute
    }

    'worker/rest/api/assets/deployments/$deploymentId.GET': {
      id: 'worker/rest/api/assets/deployments/$deploymentId.GET',
      path: 'worker/rest/api/assets/deployments/$deploymentId',
      method: 'GET',
      params: {deploymentId: string},
      hook: typeof workerRestApiAssetsDeploymentsDeploymentIdGETRoute
    }

    'worker/rest/api/assets/deployments/$deploymentId.PUT': {
      id: 'worker/rest/api/assets/deployments/$deploymentId.PUT',
      path: 'worker/rest/api/assets/deployments/$deploymentId',
      method: 'PUT',
      params: {deploymentId: string},
      hook: typeof workerRestApiAssetsDeploymentsDeploymentIdPUTRoute
    }

    'worker/rest/api/assets/deployments.POST': {
      id: 'worker/rest/api/assets/deployments.POST',
      path: 'worker/rest/api/assets/deployments',
      method: 'POST',
      params: {},
      hook: typeof workerRestApiAssetsDeploymentsPOSTRoute
    }

    'worker/rest/api/assets/dialob/fill/$sessionId.GET': {
      id: 'worker/rest/api/assets/dialob/fill/$sessionId.GET',
      path: 'worker/rest/api/assets/dialob/fill/$sessionId',
      method: 'GET',
      params: {sessionId: string},
      hook: typeof workerRestApiAssetsDialobFillSessionIdGETRoute
    }

    'worker/rest/api/assets/dialob/fill/$sessionId.POST': {
      id: 'worker/rest/api/assets/dialob/fill/$sessionId.POST',
      path: 'worker/rest/api/assets/dialob/fill/$sessionId',
      method: 'POST',
      params: {sessionId: string},
      hook: typeof workerRestApiAssetsDialobFillSessionIdPOSTRoute
    }

    'worker/rest/api/assets/dialob.GET': {
      id: 'worker/rest/api/assets/dialob.GET',
      path: 'worker/rest/api/assets/dialob',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiAssetsDialobGETRoute
    }

    'worker/rest/api/assets/dialob/proxy/forms/$formId.DELETE': {
      id: 'worker/rest/api/assets/dialob/proxy/forms/$formId.DELETE',
      path: 'worker/rest/api/assets/dialob/proxy/forms/$formId',
      method: 'DELETE',
      params: {formId: string},
      hook: typeof workerRestApiAssetsDialobProxyFormsFormIdDELETERoute
    }

    'worker/rest/api/assets/dialob/proxy/forms/$formId.GET': {
      id: 'worker/rest/api/assets/dialob/proxy/forms/$formId.GET',
      path: 'worker/rest/api/assets/dialob/proxy/forms/$formId',
      method: 'GET',
      params: {formId: string},
      hook: typeof workerRestApiAssetsDialobProxyFormsFormIdGETRoute
    }

    'worker/rest/api/assets/dialob/proxy/forms.POST': {
      id: 'worker/rest/api/assets/dialob/proxy/forms.POST',
      path: 'worker/rest/api/assets/dialob/proxy/forms',
      method: 'POST',
      params: {},
      hook: typeof workerRestApiAssetsDialobProxyFormsPOSTRoute
    }

    'worker/rest/api/assets/dialob/tags.GET': {
      id: 'worker/rest/api/assets/dialob/tags.GET',
      path: 'worker/rest/api/assets/dialob/tags',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiAssetsDialobTagsGETRoute
    }

    'worker/rest/api/assets/fs/debugs.POST': {
      id: 'worker/rest/api/assets/fs/debugs.POST',
      path: 'worker/rest/api/assets/fs/debugs',
      method: 'POST',
      params: {},
      hook: typeof workerRestApiAssetsFsDebugsPOSTRoute
    }

    'worker/rest/api/assets/fs/dirents/$id/bodies/$bodyType.GET': {
      id: 'worker/rest/api/assets/fs/dirents/$id/bodies/$bodyType.GET',
      path: 'worker/rest/api/assets/fs/dirents/$id/bodies/$bodyType',
      method: 'GET',
      params: {id: string, bodyType: string},
      hook: typeof workerRestApiAssetsFsDirentsIdBodiesBodyTypeGETRoute
    }

    'worker/rest/api/assets/fs/dirents/$id/bodies/$bodyType/transient-changes.POST': {
      id: 'worker/rest/api/assets/fs/dirents/$id/bodies/$bodyType/transient-changes.POST',
      path: 'worker/rest/api/assets/fs/dirents/$id/bodies/$bodyType/transient-changes',
      method: 'POST',
      params: {id: string, bodyType: string},
      hook: typeof workerRestApiAssetsFsDirentsIdBodiesBodyTypeTransientChangesPOSTRoute
    }

    'worker/rest/api/assets/fs/dirents.POST': {
      id: 'worker/rest/api/assets/fs/dirents.POST',
      path: 'worker/rest/api/assets/fs/dirents',
      method: 'POST',
      params: {},
      hook: typeof workerRestApiAssetsFsDirentsPOSTRoute
    }

    'worker/rest/api/assets/fs/dirents.PUT': {
      id: 'worker/rest/api/assets/fs/dirents.PUT',
      path: 'worker/rest/api/assets/fs/dirents',
      method: 'PUT',
      params: {},
      hook: typeof workerRestApiAssetsFsDirentsPUTRoute
    }

    'worker/rest/api/assets/fs.GET': {
      id: 'worker/rest/api/assets/fs.GET',
      path: 'worker/rest/api/assets/fs',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiAssetsFsGETRoute
    }

    'worker/rest/api/assets/migration.POST': {
      id: 'worker/rest/api/assets/migration.POST',
      path: 'worker/rest/api/assets/migration',
      method: 'POST',
      params: {},
      hook: typeof workerRestApiAssetsMigrationPOSTRoute
    }

    'worker/rest/api/assets/publications.GET': {
      id: 'worker/rest/api/assets/publications.GET',
      path: 'worker/rest/api/assets/publications',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiAssetsPublicationsGETRoute
    }

    'worker/rest/api/assets/publications.POST': {
      id: 'worker/rest/api/assets/publications.POST',
      path: 'worker/rest/api/assets/publications',
      method: 'POST',
      params: {},
      hook: typeof workerRestApiAssetsPublicationsPOSTRoute
    }

    'worker/rest/api/assets/stencil/$assetType.DELETE': {
      id: 'worker/rest/api/assets/stencil/$assetType.DELETE',
      path: 'worker/rest/api/assets/stencil/$assetType',
      method: 'DELETE',
      params: {assetType: string},
      hook: typeof workerRestApiAssetsStencilAssetTypeDELETERoute
    }

    'worker/rest/api/assets/stencil/$assetType.POST': {
      id: 'worker/rest/api/assets/stencil/$assetType.POST',
      path: 'worker/rest/api/assets/stencil/$assetType',
      method: 'POST',
      params: {assetType: string},
      hook: typeof workerRestApiAssetsStencilAssetTypePOSTRoute
    }

    'worker/rest/api/assets/stencil/$assetType.PUT': {
      id: 'worker/rest/api/assets/stencil/$assetType.PUT',
      path: 'worker/rest/api/assets/stencil/$assetType',
      method: 'PUT',
      params: {assetType: string},
      hook: typeof workerRestApiAssetsStencilAssetTypePUTRoute
    }

    'worker/rest/api/assets/stencil/commitlogs.GET': {
      id: 'worker/rest/api/assets/stencil/commitlogs.GET',
      path: 'worker/rest/api/assets/stencil/commitlogs',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiAssetsStencilCommitlogsGETRoute
    }

    'worker/rest/api/assets/stencil.GET': {
      id: 'worker/rest/api/assets/stencil.GET',
      path: 'worker/rest/api/assets/stencil',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiAssetsStencilGETRoute
    }

    'worker/rest/api/assets/stencil/releases/$releaseId.GET': {
      id: 'worker/rest/api/assets/stencil/releases/$releaseId.GET',
      path: 'worker/rest/api/assets/stencil/releases/$releaseId',
      method: 'GET',
      params: {releaseId: string},
      hook: typeof workerRestApiAssetsStencilReleasesReleaseIdGETRoute
    }

    'worker/rest/api/assets/tagomi.GET': {
      id: 'worker/rest/api/assets/tagomi.GET',
      path: 'worker/rest/api/assets/tagomi',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiAssetsTagomiGETRoute
    }

    'worker/rest/api/assets/workflows/$workflowId.PUT': {
      id: 'worker/rest/api/assets/workflows/$workflowId.PUT',
      path: 'worker/rest/api/assets/workflows/$workflowId',
      method: 'PUT',
      params: {workflowId: string},
      hook: typeof workerRestApiAssetsWorkflowsWorkflowIdPUTRoute
    }

    'worker/rest/api/assets/workflows.GET': {
      id: 'worker/rest/api/assets/workflows.GET',
      path: 'worker/rest/api/assets/workflows',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiAssetsWorkflowsGETRoute
    }

    'worker/rest/api/assets/wrench/commands/$id.GET': {
      id: 'worker/rest/api/assets/wrench/commands/$id.GET',
      path: 'worker/rest/api/assets/wrench/commands/$id',
      method: 'GET',
      params: {id: string},
      hook: typeof workerRestApiAssetsWrenchCommandsIdGETRoute
    }

    'worker/rest/api/assets/wrench/commands.POST': {
      id: 'worker/rest/api/assets/wrench/commands.POST',
      path: 'worker/rest/api/assets/wrench/commands',
      method: 'POST',
      params: {},
      hook: typeof workerRestApiAssetsWrenchCommandsPOSTRoute
    }

    'worker/rest/api/assets/wrench/commitlogs.GET': {
      id: 'worker/rest/api/assets/wrench/commitlogs.GET',
      path: 'worker/rest/api/assets/wrench/commitlogs',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiAssetsWrenchCommitlogsGETRoute
    }

    'worker/rest/api/assets/wrench/copyas.POST': {
      id: 'worker/rest/api/assets/wrench/copyas.POST',
      path: 'worker/rest/api/assets/wrench/copyas',
      method: 'POST',
      params: {},
      hook: typeof workerRestApiAssetsWrenchCopyasPOSTRoute
    }

    'worker/rest/api/assets/wrench/dataModels.GET': {
      id: 'worker/rest/api/assets/wrench/dataModels.GET',
      path: 'worker/rest/api/assets/wrench/dataModels',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiAssetsWrenchDataModelsGETRoute
    }

    'worker/rest/api/assets/wrench/debugs.POST': {
      id: 'worker/rest/api/assets/wrench/debugs.POST',
      path: 'worker/rest/api/assets/wrench/debugs',
      method: 'POST',
      params: {},
      hook: typeof workerRestApiAssetsWrenchDebugsPOSTRoute
    }

    'worker/rest/api/assets/wrench/diff.GET': {
      id: 'worker/rest/api/assets/wrench/diff.GET',
      path: 'worker/rest/api/assets/wrench/diff',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiAssetsWrenchDiffGETRoute
    }

    'worker/rest/api/assets/wrench/flow-names.GET': {
      id: 'worker/rest/api/assets/wrench/flow-names.GET',
      path: 'worker/rest/api/assets/wrench/flow-names',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiAssetsWrenchFlowNamesGETRoute
    }

    'worker/rest/api/assets/wrench/importTag.POST': {
      id: 'worker/rest/api/assets/wrench/importTag.POST',
      path: 'worker/rest/api/assets/wrench/importTag',
      method: 'POST',
      params: {},
      hook: typeof workerRestApiAssetsWrenchImportTagPOSTRoute
    }

    'worker/rest/api/assets/wrench/resources/$id.DELETE': {
      id: 'worker/rest/api/assets/wrench/resources/$id.DELETE',
      path: 'worker/rest/api/assets/wrench/resources/$id',
      method: 'DELETE',
      params: {id: string},
      hook: typeof workerRestApiAssetsWrenchResourcesIdDELETERoute
    }

    'worker/rest/api/assets/wrench/resources.POST': {
      id: 'worker/rest/api/assets/wrench/resources.POST',
      path: 'worker/rest/api/assets/wrench/resources',
      method: 'POST',
      params: {},
      hook: typeof workerRestApiAssetsWrenchResourcesPOSTRoute
    }

    'worker/rest/api/assets/wrench/resources.PUT': {
      id: 'worker/rest/api/assets/wrench/resources.PUT',
      path: 'worker/rest/api/assets/wrench/resources',
      method: 'PUT',
      params: {},
      hook: typeof workerRestApiAssetsWrenchResourcesPUTRoute
    }

    'worker/rest/api/assets/wrench/summary/$tagId.GET': {
      id: 'worker/rest/api/assets/wrench/summary/$tagId.GET',
      path: 'worker/rest/api/assets/wrench/summary/$tagId',
      method: 'GET',
      params: {tagId: string},
      hook: typeof workerRestApiAssetsWrenchSummaryTagIdGETRoute
    }

    'worker/rest/api/batches/$batchName/instances.POST': {
      id: 'worker/rest/api/batches/$batchName/instances.POST',
      path: 'worker/rest/api/batches/$batchName/instances',
      method: 'POST',
      params: {batchName: string},
      hook: typeof workerRestApiBatchesBatchNameInstancesPOSTRoute
    }

    'worker/rest/api/batches.GET': {
      id: 'worker/rest/api/batches.GET',
      path: 'worker/rest/api/batches',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiBatchesGETRoute
    }

    'worker/rest/api/batches/steps/$stepId.GET': {
      id: 'worker/rest/api/batches/steps/$stepId.GET',
      path: 'worker/rest/api/batches/steps/$stepId',
      method: 'GET',
      params: {stepId: string},
      hook: typeof workerRestApiBatchesStepsStepIdGETRoute
    }

    'worker/rest/api/cockpits/$cockpitId.GET': {
      id: 'worker/rest/api/cockpits/$cockpitId.GET',
      path: 'worker/rest/api/cockpits/$cockpitId',
      method: 'GET',
      params: {cockpitId: string},
      hook: typeof workerRestApiCockpitsCockpitIdGETRoute
    }

    'worker/rest/api/cockpits/$cockpitId/tenants.POST': {
      id: 'worker/rest/api/cockpits/$cockpitId/tenants.POST',
      path: 'worker/rest/api/cockpits/$cockpitId/tenants',
      method: 'POST',
      params: {cockpitId: string},
      hook: typeof workerRestApiCockpitsCockpitIdTenantsPOSTRoute
    }

    'worker/rest/api/cockpits/activity/current-state.POST': {
      id: 'worker/rest/api/cockpits/activity/current-state.POST',
      path: 'worker/rest/api/cockpits/activity/current-state',
      method: 'POST',
      params: {},
      hook: typeof workerRestApiCockpitsActivityCurrentStatePOSTRoute
    }

    'worker/rest/api/cockpits.GET': {
      id: 'worker/rest/api/cockpits.GET',
      path: 'worker/rest/api/cockpits',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiCockpitsGETRoute
    }

    'worker/rest/api/cockpits.POST': {
      id: 'worker/rest/api/cockpits.POST',
      path: 'worker/rest/api/cockpits',
      method: 'POST',
      params: {},
      hook: typeof workerRestApiCockpitsPOSTRoute
    }

    'worker/rest/api/contracts.GET': {
      id: 'worker/rest/api/contracts.GET',
      path: 'worker/rest/api/contracts',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiContractsGETRoute
    }

    'worker/rest/api/feedback/$feedbackId.DELETE': {
      id: 'worker/rest/api/feedback/$feedbackId.DELETE',
      path: 'worker/rest/api/feedback/$feedbackId',
      method: 'DELETE',
      params: {feedbackId: string},
      hook: typeof workerRestApiFeedbackFeedbackIdDELETERoute
    }

    'worker/rest/api/feedback/$feedbackId/enabled.GET': {
      id: 'worker/rest/api/feedback/$feedbackId/enabled.GET',
      path: 'worker/rest/api/feedback/$feedbackId/enabled',
      method: 'GET',
      params: {feedbackId: string},
      hook: typeof workerRestApiFeedbackFeedbackIdEnabledGETRoute
    }

    'worker/rest/api/feedback/$feedbackId.GET': {
      id: 'worker/rest/api/feedback/$feedbackId.GET',
      path: 'worker/rest/api/feedback/$feedbackId',
      method: 'GET',
      params: {feedbackId: string},
      hook: typeof workerRestApiFeedbackFeedbackIdGETRoute
    }

    'worker/rest/api/feedback/$feedbackId.POST': {
      id: 'worker/rest/api/feedback/$feedbackId.POST',
      path: 'worker/rest/api/feedback/$feedbackId',
      method: 'POST',
      params: {feedbackId: string},
      hook: typeof workerRestApiFeedbackFeedbackIdPOSTRoute
    }

    'worker/rest/api/feedback/$feedbackId.PUT': {
      id: 'worker/rest/api/feedback/$feedbackId.PUT',
      path: 'worker/rest/api/feedback/$feedbackId',
      method: 'PUT',
      params: {feedbackId: string},
      hook: typeof workerRestApiFeedbackFeedbackIdPUTRoute
    }

    'worker/rest/api/feedback/$feedbackId/sentiment-and-subcategory.GET': {
      id: 'worker/rest/api/feedback/$feedbackId/sentiment-and-subcategory.GET',
      path: 'worker/rest/api/feedback/$feedbackId/sentiment-and-subcategory',
      method: 'GET',
      params: {feedbackId: string},
      hook: typeof workerRestApiFeedbackFeedbackIdSentimentAndSubcategoryGETRoute
    }

    'worker/rest/api/feedback/$feedbackId/similar.GET': {
      id: 'worker/rest/api/feedback/$feedbackId/similar.GET',
      path: 'worker/rest/api/feedback/$feedbackId/similar',
      method: 'GET',
      params: {feedbackId: string},
      hook: typeof workerRestApiFeedbackFeedbackIdSimilarGETRoute
    }

    'worker/rest/api/feedback/$feedbackId/templates.GET': {
      id: 'worker/rest/api/feedback/$feedbackId/templates.GET',
      path: 'worker/rest/api/feedback/$feedbackId/templates',
      method: 'GET',
      params: {feedbackId: string},
      hook: typeof workerRestApiFeedbackFeedbackIdTemplatesGETRoute
    }

    'worker/rest/api/feedback.GET': {
      id: 'worker/rest/api/feedback.GET',
      path: 'worker/rest/api/feedback',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiFeedbackGETRoute
    }

    'worker/rest/api/health.GET': {
      id: 'worker/rest/api/health.GET',
      path: 'worker/rest/api/health',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiHealthGETRoute
    }

    'worker/rest/api/iam/liveness.GET': {
      id: 'worker/rest/api/iam/liveness.GET',
      path: 'worker/rest/api/iam/liveness',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiIamLivenessGETRoute
    }

    'worker/rest/api/ledgers.GET': {
      id: 'worker/rest/api/ledgers.GET',
      path: 'worker/rest/api/ledgers',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiLedgersGETRoute
    }

    'worker/rest/api/pdf.POST': {
      id: 'worker/rest/api/pdf.POST',
      path: 'worker/rest/api/pdf',
      method: 'POST',
      params: {},
      hook: typeof workerRestApiPdfPOSTRoute
    }

    'worker/rest/api/processes.GET': {
      id: 'worker/rest/api/processes.GET',
      path: 'worker/rest/api/processes',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiProcessesGETRoute
    }

    'worker/rest/api/queues/configs.GET': {
      id: 'worker/rest/api/queues/configs.GET',
      path: 'worker/rest/api/queues/configs',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiQueuesConfigsGETRoute
    }

    'worker/rest/api/queues/deliveries.GET': {
      id: 'worker/rest/api/queues/deliveries.GET',
      path: 'worker/rest/api/queues/deliveries',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiQueuesDeliveriesGETRoute
    }

    'worker/rest/api/queues/messages.GET': {
      id: 'worker/rest/api/queues/messages.GET',
      path: 'worker/rest/api/queues/messages',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiQueuesMessagesGETRoute
    }

    'worker/rest/api/tasks/$taskId/audits.GET': {
      id: 'worker/rest/api/tasks/$taskId/audits.GET',
      path: 'worker/rest/api/tasks/$taskId/audits',
      method: 'GET',
      params: {taskId: string},
      hook: typeof workerRestApiTasksTaskIdAuditsGETRoute
    }

    'worker/rest/api/tasks/$taskId/comments.GET': {
      id: 'worker/rest/api/tasks/$taskId/comments.GET',
      path: 'worker/rest/api/tasks/$taskId/comments',
      method: 'GET',
      params: {taskId: string},
      hook: typeof workerRestApiTasksTaskIdCommentsGETRoute
    }

    'worker/rest/api/tasks/$taskId/comments.POST': {
      id: 'worker/rest/api/tasks/$taskId/comments.POST',
      path: 'worker/rest/api/tasks/$taskId/comments',
      method: 'POST',
      params: {taskId: string},
      hook: typeof workerRestApiTasksTaskIdCommentsPOSTRoute
    }

    'worker/rest/api/tasks/$taskId.DELETE': {
      id: 'worker/rest/api/tasks/$taskId.DELETE',
      path: 'worker/rest/api/tasks/$taskId',
      method: 'DELETE',
      params: {taskId: string},
      hook: typeof workerRestApiTasksTaskIdDELETERoute
    }

    'worker/rest/api/tasks/$taskId/files/$filename.DELETE': {
      id: 'worker/rest/api/tasks/$taskId/files/$filename.DELETE',
      path: 'worker/rest/api/tasks/$taskId/files/$filename',
      method: 'DELETE',
      params: {taskId: string, filename: string},
      hook: typeof workerRestApiTasksTaskIdFilesFilenameDELETERoute
    }

    'worker/rest/api/tasks/$taskId/files/$filename.GET': {
      id: 'worker/rest/api/tasks/$taskId/files/$filename.GET',
      path: 'worker/rest/api/tasks/$taskId/files/$filename',
      method: 'GET',
      params: {taskId: string, filename: string},
      hook: typeof workerRestApiTasksTaskIdFilesFilenameGETRoute
    }

    'worker/rest/api/tasks/$taskId/files.GET': {
      id: 'worker/rest/api/tasks/$taskId/files.GET',
      path: 'worker/rest/api/tasks/$taskId/files',
      method: 'GET',
      params: {taskId: string},
      hook: typeof workerRestApiTasksTaskIdFilesGETRoute
    }

    'worker/rest/api/tasks/$taskId/files.POST': {
      id: 'worker/rest/api/tasks/$taskId/files.POST',
      path: 'worker/rest/api/tasks/$taskId/files',
      method: 'POST',
      params: {taskId: string},
      hook: typeof workerRestApiTasksTaskIdFilesPOSTRoute
    }

    'worker/rest/api/tasks/$taskId/form-assignments.DELETE': {
      id: 'worker/rest/api/tasks/$taskId/form-assignments.DELETE',
      path: 'worker/rest/api/tasks/$taskId/form-assignments',
      method: 'DELETE',
      params: {taskId: string},
      hook: typeof workerRestApiTasksTaskIdFormAssignmentsDELETERoute
    }

    'worker/rest/api/tasks/$taskId/form-assignments.GET': {
      id: 'worker/rest/api/tasks/$taskId/form-assignments.GET',
      path: 'worker/rest/api/tasks/$taskId/form-assignments',
      method: 'GET',
      params: {taskId: string},
      hook: typeof workerRestApiTasksTaskIdFormAssignmentsGETRoute
    }

    'worker/rest/api/tasks/$taskId/form-assignments.POST': {
      id: 'worker/rest/api/tasks/$taskId/form-assignments.POST',
      path: 'worker/rest/api/tasks/$taskId/form-assignments',
      method: 'POST',
      params: {taskId: string},
      hook: typeof workerRestApiTasksTaskIdFormAssignmentsPOSTRoute
    }

    'worker/rest/api/tasks/$taskId.GET': {
      id: 'worker/rest/api/tasks/$taskId.GET',
      path: 'worker/rest/api/tasks/$taskId',
      method: 'GET',
      params: {taskId: string},
      hook: typeof workerRestApiTasksTaskIdGETRoute
    }

    'worker/rest/api/tasks/$taskId.PUT': {
      id: 'worker/rest/api/tasks/$taskId.PUT',
      path: 'worker/rest/api/tasks/$taskId',
      method: 'PUT',
      params: {taskId: string},
      hook: typeof workerRestApiTasksTaskIdPUTRoute
    }

    'worker/rest/api/tasks/$taskId/review-actions.GET': {
      id: 'worker/rest/api/tasks/$taskId/review-actions.GET',
      path: 'worker/rest/api/tasks/$taskId/review-actions',
      method: 'GET',
      params: {taskId: string},
      hook: typeof workerRestApiTasksTaskIdReviewActionsGETRoute
    }

    'worker/rest/api/tasks/$taskId/reviews.GET': {
      id: 'worker/rest/api/tasks/$taskId/reviews.GET',
      path: 'worker/rest/api/tasks/$taskId/reviews',
      method: 'GET',
      params: {taskId: string},
      hook: typeof workerRestApiTasksTaskIdReviewsGETRoute
    }

    'worker/rest/api/tasks/$taskId/transfers.PUT': {
      id: 'worker/rest/api/tasks/$taskId/transfers.PUT',
      path: 'worker/rest/api/tasks/$taskId/transfers',
      method: 'PUT',
      params: {taskId: string},
      hook: typeof workerRestApiTasksTaskIdTransfersPUTRoute
    }

    'worker/rest/api/tasks.GET': {
      id: 'worker/rest/api/tasks.GET',
      path: 'worker/rest/api/tasks',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiTasksGETRoute
    }

    'worker/rest/api/tasks.POST': {
      id: 'worker/rest/api/tasks.POST',
      path: 'worker/rest/api/tasks',
      method: 'POST',
      params: {},
      hook: typeof workerRestApiTasksPOSTRoute
    }

    'worker/rest/api/tasks/unread.GET': {
      id: 'worker/rest/api/tasks/unread.GET',
      path: 'worker/rest/api/tasks/unread',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiTasksUnreadGETRoute
    }

    'worker/rest/api/tenant-configs.GET': {
      id: 'worker/rest/api/tenant-configs.GET',
      path: 'worker/rest/api/tenant-configs',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiTenantConfigsGETRoute
    }

    'worker/rest/api/userprofiles/$profileId.GET': {
      id: 'worker/rest/api/userprofiles/$profileId.GET',
      path: 'worker/rest/api/userprofiles/$profileId',
      method: 'GET',
      params: {profileId: string},
      hook: typeof workerRestApiUserprofilesProfileIdGETRoute
    }

    'worker/rest/api/version.GET': {
      id: 'worker/rest/api/version.GET',
      path: 'worker/rest/api/version',
      method: 'GET',
      params: {},
      hook: typeof workerRestApiVersionGETRoute
    }
  }
}
export const tree = RootHook.update({'$org/groupMembership.GET': orgGroupMembershipGETRoute, '$org/groupsList.GET': orgGroupsListGETRoute, '$org/userInfo.GET': orgUserInfoGETRoute, 'config.GET': configGETRoute, 'dialob.GET': dialobGETRoute, 'worker/rest/api/assets/deployments/$deploymentId.GET': workerRestApiAssetsDeploymentsDeploymentIdGETRoute, 'worker/rest/api/assets/deployments/$deploymentId.PUT': workerRestApiAssetsDeploymentsDeploymentIdPUTRoute, 'worker/rest/api/assets/deployments.POST': workerRestApiAssetsDeploymentsPOSTRoute, 'worker/rest/api/assets/dialob/fill/$sessionId.GET': workerRestApiAssetsDialobFillSessionIdGETRoute, 'worker/rest/api/assets/dialob/fill/$sessionId.POST': workerRestApiAssetsDialobFillSessionIdPOSTRoute, 'worker/rest/api/assets/dialob.GET': workerRestApiAssetsDialobGETRoute, 'worker/rest/api/assets/dialob/proxy/forms/$formId.DELETE': workerRestApiAssetsDialobProxyFormsFormIdDELETERoute, 'worker/rest/api/assets/dialob/proxy/forms/$formId.GET': workerRestApiAssetsDialobProxyFormsFormIdGETRoute, 'worker/rest/api/assets/dialob/proxy/forms.POST': workerRestApiAssetsDialobProxyFormsPOSTRoute, 'worker/rest/api/assets/dialob/tags.GET': workerRestApiAssetsDialobTagsGETRoute, 'worker/rest/api/assets/fs/debugs.POST': workerRestApiAssetsFsDebugsPOSTRoute, 'worker/rest/api/assets/fs/dirents/$id/bodies/$bodyType.GET': workerRestApiAssetsFsDirentsIdBodiesBodyTypeGETRoute, 'worker/rest/api/assets/fs/dirents/$id/bodies/$bodyType/transient-changes.POST': workerRestApiAssetsFsDirentsIdBodiesBodyTypeTransientChangesPOSTRoute, 'worker/rest/api/assets/fs/dirents.POST': workerRestApiAssetsFsDirentsPOSTRoute, 'worker/rest/api/assets/fs/dirents.PUT': workerRestApiAssetsFsDirentsPUTRoute, 'worker/rest/api/assets/fs.GET': workerRestApiAssetsFsGETRoute, 'worker/rest/api/assets/migration.POST': workerRestApiAssetsMigrationPOSTRoute, 'worker/rest/api/assets/publications.GET': workerRestApiAssetsPublicationsGETRoute, 'worker/rest/api/assets/publications.POST': workerRestApiAssetsPublicationsPOSTRoute, 'worker/rest/api/assets/stencil/$assetType.DELETE': workerRestApiAssetsStencilAssetTypeDELETERoute, 'worker/rest/api/assets/stencil/$assetType.POST': workerRestApiAssetsStencilAssetTypePOSTRoute, 'worker/rest/api/assets/stencil/$assetType.PUT': workerRestApiAssetsStencilAssetTypePUTRoute, 'worker/rest/api/assets/stencil/commitlogs.GET': workerRestApiAssetsStencilCommitlogsGETRoute, 'worker/rest/api/assets/stencil.GET': workerRestApiAssetsStencilGETRoute, 'worker/rest/api/assets/stencil/releases/$releaseId.GET': workerRestApiAssetsStencilReleasesReleaseIdGETRoute, 'worker/rest/api/assets/tagomi.GET': workerRestApiAssetsTagomiGETRoute, 'worker/rest/api/assets/workflows/$workflowId.PUT': workerRestApiAssetsWorkflowsWorkflowIdPUTRoute, 'worker/rest/api/assets/workflows.GET': workerRestApiAssetsWorkflowsGETRoute, 'worker/rest/api/assets/wrench/commands/$id.GET': workerRestApiAssetsWrenchCommandsIdGETRoute, 'worker/rest/api/assets/wrench/commands.POST': workerRestApiAssetsWrenchCommandsPOSTRoute, 'worker/rest/api/assets/wrench/commitlogs.GET': workerRestApiAssetsWrenchCommitlogsGETRoute, 'worker/rest/api/assets/wrench/copyas.POST': workerRestApiAssetsWrenchCopyasPOSTRoute, 'worker/rest/api/assets/wrench/dataModels.GET': workerRestApiAssetsWrenchDataModelsGETRoute, 'worker/rest/api/assets/wrench/debugs.POST': workerRestApiAssetsWrenchDebugsPOSTRoute, 'worker/rest/api/assets/wrench/diff.GET': workerRestApiAssetsWrenchDiffGETRoute, 'worker/rest/api/assets/wrench/flow-names.GET': workerRestApiAssetsWrenchFlowNamesGETRoute, 'worker/rest/api/assets/wrench/importTag.POST': workerRestApiAssetsWrenchImportTagPOSTRoute, 'worker/rest/api/assets/wrench/resources/$id.DELETE': workerRestApiAssetsWrenchResourcesIdDELETERoute, 'worker/rest/api/assets/wrench/resources.POST': workerRestApiAssetsWrenchResourcesPOSTRoute, 'worker/rest/api/assets/wrench/resources.PUT': workerRestApiAssetsWrenchResourcesPUTRoute, 'worker/rest/api/assets/wrench/summary/$tagId.GET': workerRestApiAssetsWrenchSummaryTagIdGETRoute, 'worker/rest/api/batches/$batchName/instances.POST': workerRestApiBatchesBatchNameInstancesPOSTRoute, 'worker/rest/api/batches.GET': workerRestApiBatchesGETRoute, 'worker/rest/api/batches/steps/$stepId.GET': workerRestApiBatchesStepsStepIdGETRoute, 'worker/rest/api/cockpits/$cockpitId.GET': workerRestApiCockpitsCockpitIdGETRoute, 'worker/rest/api/cockpits/$cockpitId/tenants.POST': workerRestApiCockpitsCockpitIdTenantsPOSTRoute, 'worker/rest/api/cockpits/activity/current-state.POST': workerRestApiCockpitsActivityCurrentStatePOSTRoute, 'worker/rest/api/cockpits.GET': workerRestApiCockpitsGETRoute, 'worker/rest/api/cockpits.POST': workerRestApiCockpitsPOSTRoute, 'worker/rest/api/contracts.GET': workerRestApiContractsGETRoute, 'worker/rest/api/feedback/$feedbackId.DELETE': workerRestApiFeedbackFeedbackIdDELETERoute, 'worker/rest/api/feedback/$feedbackId/enabled.GET': workerRestApiFeedbackFeedbackIdEnabledGETRoute, 'worker/rest/api/feedback/$feedbackId.GET': workerRestApiFeedbackFeedbackIdGETRoute, 'worker/rest/api/feedback/$feedbackId.POST': workerRestApiFeedbackFeedbackIdPOSTRoute, 'worker/rest/api/feedback/$feedbackId.PUT': workerRestApiFeedbackFeedbackIdPUTRoute, 'worker/rest/api/feedback/$feedbackId/sentiment-and-subcategory.GET': workerRestApiFeedbackFeedbackIdSentimentAndSubcategoryGETRoute, 'worker/rest/api/feedback/$feedbackId/similar.GET': workerRestApiFeedbackFeedbackIdSimilarGETRoute, 'worker/rest/api/feedback/$feedbackId/templates.GET': workerRestApiFeedbackFeedbackIdTemplatesGETRoute, 'worker/rest/api/feedback.GET': workerRestApiFeedbackGETRoute, 'worker/rest/api/health.GET': workerRestApiHealthGETRoute, 'worker/rest/api/iam/liveness.GET': workerRestApiIamLivenessGETRoute, 'worker/rest/api/ledgers.GET': workerRestApiLedgersGETRoute, 'worker/rest/api/pdf.POST': workerRestApiPdfPOSTRoute, 'worker/rest/api/processes.GET': workerRestApiProcessesGETRoute, 'worker/rest/api/queues/configs.GET': workerRestApiQueuesConfigsGETRoute, 'worker/rest/api/queues/deliveries.GET': workerRestApiQueuesDeliveriesGETRoute, 'worker/rest/api/queues/messages.GET': workerRestApiQueuesMessagesGETRoute, 'worker/rest/api/tasks/$taskId/audits.GET': workerRestApiTasksTaskIdAuditsGETRoute, 'worker/rest/api/tasks/$taskId/comments.GET': workerRestApiTasksTaskIdCommentsGETRoute, 'worker/rest/api/tasks/$taskId/comments.POST': workerRestApiTasksTaskIdCommentsPOSTRoute, 'worker/rest/api/tasks/$taskId.DELETE': workerRestApiTasksTaskIdDELETERoute, 'worker/rest/api/tasks/$taskId/files/$filename.DELETE': workerRestApiTasksTaskIdFilesFilenameDELETERoute, 'worker/rest/api/tasks/$taskId/files/$filename.GET': workerRestApiTasksTaskIdFilesFilenameGETRoute, 'worker/rest/api/tasks/$taskId/files.GET': workerRestApiTasksTaskIdFilesGETRoute, 'worker/rest/api/tasks/$taskId/files.POST': workerRestApiTasksTaskIdFilesPOSTRoute, 'worker/rest/api/tasks/$taskId/form-assignments.DELETE': workerRestApiTasksTaskIdFormAssignmentsDELETERoute, 'worker/rest/api/tasks/$taskId/form-assignments.GET': workerRestApiTasksTaskIdFormAssignmentsGETRoute, 'worker/rest/api/tasks/$taskId/form-assignments.POST': workerRestApiTasksTaskIdFormAssignmentsPOSTRoute, 'worker/rest/api/tasks/$taskId.GET': workerRestApiTasksTaskIdGETRoute, 'worker/rest/api/tasks/$taskId.PUT': workerRestApiTasksTaskIdPUTRoute, 'worker/rest/api/tasks/$taskId/review-actions.GET': workerRestApiTasksTaskIdReviewActionsGETRoute, 'worker/rest/api/tasks/$taskId/reviews.GET': workerRestApiTasksTaskIdReviewsGETRoute, 'worker/rest/api/tasks/$taskId/transfers.PUT': workerRestApiTasksTaskIdTransfersPUTRoute, 'worker/rest/api/tasks.GET': workerRestApiTasksGETRoute, 'worker/rest/api/tasks.POST': workerRestApiTasksPOSTRoute, 'worker/rest/api/tasks/unread.GET': workerRestApiTasksUnreadGETRoute, 'worker/rest/api/tenant-configs.GET': workerRestApiTenantConfigsGETRoute, 'worker/rest/api/userprofiles/$profileId.GET': workerRestApiUserprofilesProfileIdGETRoute, 'worker/rest/api/version.GET': workerRestApiVersionGETRoute})