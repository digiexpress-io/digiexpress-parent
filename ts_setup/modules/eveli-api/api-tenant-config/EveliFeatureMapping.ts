import { TenantFeature } from "./TenantConfigContext";

function oneOf(required: TenantFeature[]): (given: TenantFeature[]) => boolean {
  return (given: TenantFeature[]) => {
    return !!given.find((givenOnFeature) => required.includes(givenOnFeature));
  };
}

function notOneOf(required: TenantFeature[]): (given: TenantFeature[]) => boolean {
  return (given: TenantFeature[]) => {
    return !given.find((givenOnFeature) => required.includes(givenOnFeature));
  };
}


export const EveliFeatureMapping = {
  'LOGIN_BUTTON': notOneOf(['wrench-only']),
  'STENCIL_ENABLED': notOneOf(['stencil-disabled']),
  'WRENCH_ENABLED': notOneOf(['wrench-disabled']),
  'TAGOMI_ENABLED': oneOf(['tagomi']),     
  'CONTRACT_ENABLED': oneOf(['contract']),
  'COCKPITS_ENABLED': oneOf(['cockpits']),
  'EVELI_TREE_ENABLED': oneOf(['eveli_tree']),
  'IN_HOUSE_ENABLED': oneOf(['in_house']),

  'PUBLICATION_UPDATE': notOneOf(['external-deployment']),                    

  'STENCIL_LOCALE_FILTER': oneOf(['stencil_locale_filter']),                  

  'DIALOB_ENABLED': notOneOf(['wrench-disabled', 'stencil-disabled']),

  'FORM_REVIEW_FLASHY': oneOf(['visual_accommodation']),                    
  'FORM_REVIEW_NORMAL': notOneOf(['visual_accommodation']),

  'QUEUES_ENABLED': notOneOf(['queues-visually-disabled']),
  'FEEDBACK_ENABLED': notOneOf(['feedback-visually-disabled']),               

  'PROFILE_ENABLED': oneOf(['user_profile']),

  'SMART_TABLES': oneOf(['smart_tables']),
  
  'SMART_TASK_AUDIT': oneOf(['smart_task_audit']),
  'BATCHES': oneOf(['batches']),
  'AI_ASSISTANT': oneOf(['ai-assistant']),

}



/*
 1. wrench-only: Eveli is reconfigured so as to include only wrench and strip the rest away
 2. stencil-disabled: Eveli is reconfigured so as to remove the stencil application
 3. wrench-disabled: Eveli is reconfigured so as to remove the wrench application
 4. external-deployment: Assets come explicitely from an environment-specific data source that implements ExternalDeploymentProvider
 5. stencil_locale_filter: Configure visibility of the locale filtering UI component in Stencil
 6. eveli_publication_only: Editing assets is disabled they come directly from a publication
 7. visual_accommodation: To make certain buttons and UI items pop - for example, Form Review button on the task detail view
 8. queues-visually-disabled: Show / hide the Queues feature from the UI
 9. feedback-visually-disabled: Show / hide the feedback feature from the UI
 10. user_profile: Show / hide the user profile feature from the UI
 11. smart_tables: All tables are persistant - they save user-defined configurations
 12: batches: Enable / disable batch processing features
*/

export type EveliFeatureType = keyof typeof EveliFeatureMapping;
