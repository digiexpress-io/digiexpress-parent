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
  'LOGIN_BUTTON': notOneOf(['wrench-only']),                                  // Eveli is reconfigured so as to include only wrench
  'STENCIL_ENABLED': notOneOf(['stencil-disabled']),                          // Stencil is not available in Eveli 
  'WRENCH_ENABLED': notOneOf(['wrench-disabled']),                            // Wrench is not available in Eveli
  'PUBLICATION_UPDATE': notOneOf(['external-deployment']),                    // 

  'STENCIL_LOCALE_FILTER': oneOf(['stencil_locale_filter']),                  // Configure visibility of the locale filtering UI component in Stencil

  'DIALOB_ENABLED': notOneOf(['wrench-disabled', 'stencil-disabled']),        

  'WRENCH_RELEASES': notOneOf(['eveli_publication_only']),                    //
  'STENCIL_RELEASES': notOneOf(['eveli_publication_only']),

  'FORM_REVIEW_FLASHY': oneOf(['visual_accommodation']),                       //
  'FORM_REVIEW_NORMAL': notOneOf(['visual_accommodation']),

  'QUEUES_ENABLED': notOneOf(['queues-visually-disabled']),                   // Queues feature is hidden from Eveli UI
  'FEEDBACK_ENABLED': notOneOf(['feedback-visually-disabled']),               // Feedback feature is hidden from Eveli UI

  'PROFILE_ENABLED': oneOf(['user_profile']),                                 // Configure visibility of User profile features in Eveli UI

  'SMART_TABLES': oneOf(['smart_tables']),                                    // Configure the usage of (new) improved table component vs original (older) table component

  'BATCHES': oneOf(['batches']),                                              // Configure the availability of the Batches feature in Eveli
}


export type EveliFeatureType = keyof typeof EveliFeatureMapping;
