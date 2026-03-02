import { FsNode } from "@dxs-ts/fs-api";



export interface FsPropertiesProps {
  node: FsNode | undefined;
}


export const propertiesMock = {
  serviceLocaleLabels: ['en', 'sv', 'fi'],
  serviceName: 'General Message',
  comments: 'Still needs spellcheck in Swedish language translations',
  dialobFormName: 'feedback_form',
  dialobFormTag: 'v1.0',
  flowName: 'General_Message_Flow',
  validityStart: '11.01.2025',
  validityEnd: '12.03.2025',
  validityPeriod: '30 days',
  configOptionsEnabled: ['Assignable', 'DevMode'],
  selectedArticles: ['000_index', '230_send_feedback', '400_contact_us'],
  pages: ['en', 'fi', 'sv'],
  labels: ['protected', 'gdpr']
};