import { Fs } from './fs-types'

export const mockFsDirentProperties: Record<string, Fs.Props> = {

  'content': {
    id: 'content',
    type: 'folder',
    expanded: true,
    reference: false,
    locked: false,
    description: 'Root content folder for all municipal services.',
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '07.01.2024', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } },
      { changeType: 'update', changeDate: '07.01.2025', changedBy: { userName: 'Sarah Johnson', email: 'sarah.johnson@example.com' } }
    ],
    permissions: [],
    labels: [
      { id: 'label-001', value: 'Needs improvement' }
    ],
    errors: [],
  },

  'shared': {
    id: 'shared',
    type: 'folder',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '15.06.2023', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
  },

  'printouts': {
    id: 'printouts',
    type: 'folder',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '15.06.2023', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
  },

  'languages': {
    id: 'languages',
    type: 'folder',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '15.06.2023', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
  },

  'fi.language': {
    id: 'fi.language',
    type: 'language',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '15.06.2023', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    localeCode: 'fi',
  },

  'sv.language': {
    id: 'sv.language',
    type: 'language',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '15.06.2023', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    localeCode: 'sv',
  },

  'en.language': {
    id: 'en.language',
    type: 'language',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '15.06.2023', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    localeCode: 'en',
  },

  'index.article': {
    id: 'index.article',
    type: 'article',
    expanded: false,
    reference: false,
    locked: false,
    orderNumber: 0,
    description: 'Main landing article for the municipal services portal.',
    configOptions: [],
    comments: [
      { comment: 'This article needs updated localization for the 2025 service period.', author: 'Anna Virtanen', created: '07.01.2025' },
      { comment: 'Images are missing from the Swedish version.', author: 'Mikael Berg', created: '15.02.2025' }
    ],
    changes: [
      { changeType: 'create', changeDate: '20.06.2023', changedBy: { userName: 'Anna Virtanen', email: 'anna.virtanen@example.com' } },
      { changeType: 'update', changeDate: '10.01.2025', changedBy: { userName: 'Sarah Johnson', email: 'sarah.johnson@example.com' } }
    ],
    permissions: [],
    labels: [
      { id: 'label-003', value: 'Needs translation' },
      { id: 'label-004', value: 'Under review' }
    ],
    errors: [
      { code: 'MISSING_LOCALE', severity: 'WARNING', message: 'Swedish locale content is missing.' }
    ],
  },

  'gdpr.article': {
    id: 'gdpr.article',
    type: 'article',
    expanded: false,
    reference: false,
    locked: false,
    orderNumber: 100,
    description: 'GDPR information and data protection policy.',
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '20.06.2023', changedBy: { userName: 'Anna Virtanen', email: 'anna.virtanen@example.com' } },
      { changeType: 'update', changeDate: '05.03.2024', changedBy: { userName: 'Lars Eriksson', email: 'lars.eriksson@example.com' } }
    ],
    permissions: [],
    labels: [
      { id: 'label-011', value: 'Under review' }
    ],
    errors: [],
  },

  'gdpr-child.article': {
    id: 'gdpr-child.article',
    type: 'article',
    expanded: false,
    reference: false,
    locked: false,
    orderNumber: 100,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '20.06.2023', changedBy: { userName: 'Anna Virtanen', email: 'anna.virtanen@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
  },

  'democracy.article': {
    id: 'democracy.article',
    type: 'article',
    expanded: false,
    reference: false,
    locked: false,
    orderNumber: 200,
    description: 'Democracy and civic participation services.',
    configOptions: [],
    comments: [
      { comment: 'Content needs review after the 2025 municipal elections.', author: 'Lars Eriksson', created: '12.02.2025' }
    ],
    changes: [
      { changeType: 'create', changeDate: '20.06.2023', changedBy: { userName: 'Anna Virtanen', email: 'anna.virtanen@example.com' } },
      { changeType: 'update', changeDate: '14.02.2025', changedBy: { userName: 'Lars Eriksson', email: 'lars.eriksson@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
  },

  'city-living.article': {
    id: 'city-living.article',
    type: 'article',
    expanded: false,
    reference: false,
    locked: false,
    orderNumber: 300,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '05.09.2023', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } }
    ],
    permissions: [],
    labels: [
      { id: 'label-012', value: 'Draft' }
    ],
    errors: [],
  },

  'fi-index': {
    id: 'fi-index',
    type: 'page',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: ['disabledMode'],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '20.06.2023', changedBy: { userName: 'Anna Virtanen', email: 'anna.virtanen@example.com' } },
      { changeType: 'update', changeDate: '12.01.2025', changedBy: { userName: 'Anna Virtanen', email: 'anna.virtanen@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    localeCode: 'fi',
    articleId: 'index.article',
    content: `# This is markdown content

* bullet 1
* **Bullet 2**
*
* Other

*bold*
---
At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique sunt in culpa qui officia deserunt mollitia animi, id est laborum et dolorum fuga.

Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus id quod maxime placeat facere possimus, omnis voluptas assumenda est, omnis dolor repellendus.

Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet ut et voluptates repudiandae sint et molestiae non recusandae. Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat.`,
  },

  'sv-index': {
    id: 'sv-index',
    type: 'page',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '20.06.2023', changedBy: { userName: 'Anna Virtanen', email: 'anna.virtanen@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    localeCode: 'sv',
    articleId: 'index.article',
  },

  'en-index': {
    id: 'en-index',
    type: 'page',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '15.08.2023', changedBy: { userName: 'Sarah Johnson', email: 'sarah.johnson@example.com' } },
      { changeType: 'update', changeDate: '20.01.2025', changedBy: { userName: 'Sarah Johnson', email: 'sarah.johnson@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    localeCode: 'en',
    articleId: 'index.article',
    content: `# Welcome to the Service Portal

This portal gives you access to municipal services, allowing you to submit applications, track the status of your cases, and communicate with the relevant departments.

## Getting Started

1. Sign in using your personal identity number or bank credentials
2. Browse available services from the navigation menu
3. Complete the application form and attach any required documents
4. Submit your application — a confirmation will be sent to your registered email address

## Processing Times

| Service | Estimated Time |
|---|---|
| Building permit | 10 weeks |
| Social assistance | 2 weeks |
| School placement | 4 weeks |
| Business licence | 6 weeks |

## Opening Hours

The service centre is open Monday to Friday, **08:00–16:30**.

> **Note:** For urgent matters, please contact the relevant department directly rather than using this portal.`,
  },

  'fi-gdpr': {
    id: 'fi-gdpr',
    type: 'page',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '01.07.2023', changedBy: { userName: 'Lars Eriksson', email: 'lars.eriksson@example.com' } },
      { changeType: 'update', changeDate: '10.03.2024', changedBy: { userName: 'Lars Eriksson', email: 'lars.eriksson@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    localeCode: 'fi',
    articleId: 'gdpr.article',
  },

  'sv-gdpr': {
    id: 'sv-gdpr',
    type: 'page',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '01.07.2023', changedBy: { userName: 'Lars Eriksson', email: 'lars.eriksson@example.com' } },
      { changeType: 'update', changeDate: '12.03.2024', changedBy: { userName: 'Lars Eriksson', email: 'lars.eriksson@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    localeCode: 'sv',
    articleId: 'gdpr.article',
    content: `# Privacy Notice

This notice explains how we collect, use, and protect your personal data in accordance with the General Data Protection Regulation (GDPR).

## What Data We Collect

- **Identity information**: name, personal identity number, date of birth
- **Contact details**: postal address, email address, phone number
- **Case data**: applications submitted, decisions made, correspondence

## Purpose of Processing

Your personal data is processed in order to:
- Handle your service applications and requests
- Fulfil legal obligations under municipal law
- Send notifications related to your active cases

## Your Rights

Under GDPR you have the right to:

1. **Access** — request a copy of the data we hold about you
2. **Rectification** — correct inaccurate or incomplete data
3. **Erasure** — request deletion of data where legally permitted
4. **Restriction** — limit how your data is processed in certain circumstances
5. **Portability** — receive your data in a structured, machine-readable format

## Data Retention

Personal data is retained only as long as required by law or the purpose for which it was collected. Case records are kept for a minimum of **10 years** in accordance with national archiving legislation.

## Contact

For questions regarding your personal data, contact our Data Protection Officer at **dpo@municipality.fi**.`,
  },

  'fi-gdpr-child': {
    id: 'fi-gdpr-child',
    type: 'page',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '01.07.2023', changedBy: { userName: 'Lars Eriksson', email: 'lars.eriksson@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    localeCode: 'fi',
    articleId: 'gdpr-child.article',
  },

  'sv-gdpr-child': {
    id: 'sv-gdpr-child',
    type: 'page',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '01.07.2023', changedBy: { userName: 'Lars Eriksson', email: 'lars.eriksson@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    localeCode: 'sv',
    articleId: 'gdpr-child.article',
  },

  'fi-democracy': {
    id: 'fi-democracy',
    type: 'page',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '10.09.2023', changedBy: { userName: 'Anna Virtanen', email: 'anna.virtanen@example.com' } },
      { changeType: 'update', changeDate: '18.02.2025', changedBy: { userName: 'Lars Eriksson', email: 'lars.eriksson@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    localeCode: 'fi',
    articleId: 'democracy.article',
  },

  'sv-democracy': {
    id: 'sv-democracy',
    type: 'page',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '10.09.2023', changedBy: { userName: 'Anna Virtanen', email: 'anna.virtanen@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    localeCode: 'sv',
    articleId: 'democracy.article',
  },

  'en-democracy': {
    id: 'en-democracy',
    type: 'page',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '10.09.2023', changedBy: { userName: 'Anna Virtanen', email: 'anna.virtanen@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    localeCode: 'en',
    articleId: 'democracy.article',
  },

  'fi-city-living': {
    id: 'fi-city-living',
    type: 'page',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '05.09.2023', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    localeCode: 'fi',
    articleId: 'city-living.article',
  },

  'sv-city-living': {
    id: 'sv-city-living',
    type: 'page',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '05.09.2023', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    localeCode: 'sv',
    articleId: 'city-living.article',
  },

  'general-message.service': {
    id: 'general-message.service',
    type: 'service',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: ['devMode'],
    comments: [
      { comment: 'Form linked correctly after the January Dialob update.', author: 'Sarah Johnson', created: '10.01.2025' }
    ],
    changes: [
      { changeType: 'create', changeDate: '15.03.2024', changedBy: { userName: 'Anna Virtanen', email: 'anna.virtanen@example.com' } }
    ],
    permissions: [],
    labels: [
      { id: 'label-005', value: 'Draft' }
    ],
    errors: [
      { code: 'MISSING_FLOW', severity: 'CRITICAL', message: 'No flow assigned to service.' }
    ],
    serviceName: 'General Message',
    dialobFormName: 'general-message.dialob',
    dialobFormTag: 'LATEST',
    flowName: 'taskMsgFlow.flow',
    validityStart: '01.01.2025',
    validityEnd: undefined,
    articles: ['index.article'],
    intlValues: { en: 'General message', fi: 'Yleinen viesti', sv: 'Allmänt meddelande' },
  },

  'public-inforeq.service': {
    id: 'public-inforeq.service',
    type: 'service',
    expanded: false,
    reference: false,
    locked: true,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '01.03.2024', changedBy: { userName: 'Lars Eriksson', email: 'lars.eriksson@example.com' } },
      { changeType: 'update', changeDate: '15.04.2024', changedBy: { userName: 'Sarah Johnson', email: 'sarah.johnson@example.com' } }
    ],
    permissions: [],
    labels: [
      { id: 'label-006', value: 'Active' },
      { id: 'label-007', value: 'Reviewed' }
    ],
    errors: [],
    serviceName: 'Public Information Request',
    dialobFormName: 'public-inforeq.dialob',
    dialobFormTag: 'v2',
    flowName: 'taskGenericFlow.flow',
    validityStart: '01.03.2024',
    validityEnd: undefined,
    articles: ['gdpr.article'],
    intlValues: { en: 'Public information request', fi: 'Julkinen tietopyyntö', sv: 'Offentlig informationsbegäran' },
  },

  'wilma-preschool.service': {
    id: 'wilma-preschool.service',
    type: 'service',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: ['devMode', 'assignableMode'],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '15.07.2024', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } },
      { changeType: 'update', changeDate: '03.02.2025', changedBy: { userName: 'Anna Virtanen', email: 'anna.virtanen@example.com' } }
    ],
    permissions: [
      { name: 'admin', types: ['read', 'write'] }
    ],
    labels: [
      { id: 'label-002', value: 'Priority' }
    ],
    errors: [],
    serviceName: 'Wilma Preschool Application',
    dialobFormName: 'wilma-preschool.dialob',
    dialobFormTag: 'LATEST',
    flowName: 'taskGenericFlow.flow',
    validityStart: '01.08.2024',
    validityEnd: undefined,
    articles: ['index.article', 'gdpr.article'],
    intlValues: { en: 'Preschool application', fi: 'Varhaiskasvatushakemus', sv: 'Ansökan om förskola' },
  },

  'general-message.dialob': {
    id: 'general-message.dialob',
    type: 'dialob',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '15.03.2024', changedBy: { userName: 'Anna Virtanen', email: 'anna.virtanen@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    formName: 'General Message Form',
    formTechnicalId: 'general_msg_v1',
    versionTags: ['version 1', 'Test version 1.2'],
  },

  'public-inforeq.dialob': {
    id: 'public-inforeq.dialob',
    type: 'dialob',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '01.03.2024', changedBy: { userName: 'Lars Eriksson', email: 'lars.eriksson@example.com' } },
      { changeType: 'update', changeDate: '10.04.2024', changedBy: { userName: 'Lars Eriksson', email: 'lars.eriksson@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [
      { code: 'FORM_VERSION_MISMATCH', severity: 'WARNING', message: 'Form version tag does not match deployed version.' }
    ],
    formName: 'Public Info Request Form',
    formTechnicalId: 'pub_inforeq_v2',
    versionTags: ['v1', 'v2'],
  },

  'wilma-preschool.dialob': {
    id: 'wilma-preschool.dialob',
    type: 'dialob',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '15.07.2024', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } },
      { changeType: 'update', changeDate: '20.01.2025', changedBy: { userName: 'Sarah Johnson', email: 'sarah.johnson@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    formName: 'Preschool Application Form',
    formTechnicalId: 'wilma_preschool_v1',
    versionTags: ['My_test-first_137ao7r'],
  },

  'taskMsgFlow.flow': {
    id: 'taskMsgFlow.flow',
    type: 'flow',
    name: 'taskMessagingFlow',
    expanded: false,
    reference: false,
    locked: false,
    description: 'Task messaging workflow',
    content: `id: taskMessagingFlow\ndescription: "task messaging flow"\n\ninputs:\n  taskId:\n    required: true\n    type: STRING\n  messageType:\n    required: true\n    type: STRING\n  recipientId:\n    required: true\n    type: STRING`,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '10.02.2024', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
  },

  'taskGenericFlow.flow': {
    id: 'taskGenericFlow.flow',
    type: 'flow',
    name: 'taskGenericFlow',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    content: `id: taskGenericFlow\ndescription: "generic task flow"\n\ninputs:\n  taskId:\n    required: true\n    type: STRING\n  assigneeId:\n    required: false\n    type: STRING\n  dueDate:\n    required: false\n    type: STRING`,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '10.02.2024', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } },
      { changeType: 'update', changeDate: '05.11.2024', changedBy: { userName: 'Anna Virtanen', email: 'anna.virtanen@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
  },

  'taskSplitFlow.flow': {
    id: 'taskSplitFlow.flow',
    type: 'flow',
    name: 'taskSplitFlow',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    content: `id: taskSplitFlow\ndescription: "task split flow"\n\ninputs:\n  taskId:\n    required: true\n    type: STRING\n  splitType:\n    required: true\n    type: STRING\n  targetIds:\n    required: true\n    type: STRING`,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '20.05.2024', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
  },

  'index-main.phone': {
    id: 'index-main.phone',
    type: 'phone',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '15.06.2023', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    phoneValue: '+358 9 235 11',
    intlValues: { en: 'City Hall switchboard', fi: 'Kaupungintalon vaihde', sv: 'Stadshuset växel' },
  },

  'democracy-info.phone': {
    id: 'democracy-info.phone',
    type: 'phone',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: ['devMode'],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '15.06.2023', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } },
      { changeType: 'update', changeDate: '10.01.2025', changedBy: { userName: 'Sarah Johnson', email: 'sarah.johnson@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    phoneValue: '+358 9 235 22',
    intlValues: { en: 'Democracy services', fi: 'Demokratiapalvelut', sv: 'Demokratitjänster' },
  },

  'water-services.phone': {
    id: 'water-services.phone',
    type: 'phone',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '15.06.2023', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    phoneValue: '+358 9 235 33',
    intlValues: { en: 'Water services', fi: 'Vesihuolto', sv: 'Vattentjänster' },
  },

  'sipoo-main-site.link': {
    id: 'sipoo-main-site.link',
    type: 'link',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: ['disabledMode'],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '15.06.2023', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } },
      { changeType: 'update', changeDate: '01.09.2024', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    urlValue: 'https://www.sipoo.fi',
    intlValues: { en: 'Sipoo official website', fi: 'Sipoon virallinen sivusto', sv: 'Sibbo officiella webbplats' },
  },

  'wilma-info.link': {
    id: 'wilma-info.link',
    type: 'link',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '15.06.2023', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    urlValue: 'https://wilma.sipoo.fi',
    intlValues: { en: 'Wilma student portal', fi: 'Wilma-oppilasjärjestelmä', sv: 'Wilma elevportal' },
  },

  'lupapiste.link': {
    id: 'lupapiste.link',
    type: 'link',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '20.08.2023', changedBy: { userName: 'Lars Eriksson', email: 'lars.eriksson@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    urlValue: 'https://www.lupapiste.fi',
    intlValues: { en: 'Lupapiste permit service', fi: 'Lupapiste-lupajärjestelmä', sv: 'Lupapiste tillståndstjänst' },
  },

  'sipoo-main-logo.png': {
    id: 'sipoo-main-logo.png',
    type: 'image',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '10.06.2023', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
  },

  'sipoo-color-logo.png': {
    id: 'sipoo-color-logo.png',
    type: 'image',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '10.06.2023', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } },
      { changeType: 'update', changeDate: '15.04.2024', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
  },

  'municipal-seal.svg': {
    id: 'municipal-seal.svg',
    type: 'image',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '10.06.2023', changedBy: { userName: 'Mikael Berg', email: 'mikael.berg@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
  },

  'new-construction-permit.printout': {
    id: 'new-construction-permit.printout',
    type: 'printout',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [
      { comment: 'Swedish template needs review before next release.', author: 'Lars Eriksson', created: '20.03.2025' }
    ],
    changes: [
      { changeType: 'create', changeDate: '10.10.2023', changedBy: { userName: 'Lars Eriksson', email: 'lars.eriksson@example.com' } },
      { changeType: 'update', changeDate: '05.03.2025', changedBy: { userName: 'Lars Eriksson', email: 'lars.eriksson@example.com' } }
    ],
    permissions: [],
    labels: [
      { id: 'label-008', value: 'Needs translation' },
      { id: 'label-009', value: 'Pending approval' }
    ],
    errors: [],
    printoutServiceName: 'NewConstructionPermitService',
    orchestratorName: 'taskSplitFlow.flow',
    intlValues: { fi: 'Uudisrakennuslupa', sv: 'Nybyggnadstillstånd' },
  },

  'municipal-services-guide.printout': {
    id: 'municipal-services-guide.printout',
    type: 'printout',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '10.10.2023', changedBy: { userName: 'Lars Eriksson', email: 'lars.eriksson@example.com' } },
      { changeType: 'update', changeDate: '20.01.2025', changedBy: { userName: 'Anna Virtanen', email: 'anna.virtanen@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    printoutServiceName: 'MunicipalServicesGuideService',
    orchestratorName: 'taskGenericFlow.flow',
    intlValues: { fi: 'Kunnallispalveluopas', sv: 'Kommunal serviceguide', en: 'Municipal services guide' },
  },

  'waste-management-info.printout': {
    id: 'waste-management-info.printout',
    type: 'printout',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '10.10.2023', changedBy: { userName: 'Lars Eriksson', email: 'lars.eriksson@example.com' } }
    ],
    permissions: [],
    labels: [
      { id: 'label-010', value: 'Outdated' }
    ],
    errors: [],
    printoutServiceName: 'WasteManagementService',
    orchestratorName: 'taskGenericFlow.flow',
    intlValues: { fi: 'Jätehuolto-ohje' },
  },

  'fi-construction-permit': {
    id: 'fi-construction-permit',
    type: 'template',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '10.10.2023', changedBy: { userName: 'Lars Eriksson', email: 'lars.eriksson@example.com' } },
      { changeType: 'update', changeDate: '08.03.2025', changedBy: { userName: 'Lars Eriksson', email: 'lars.eriksson@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    serviceId: 'new-construction-permit.printout',
    localeId: 'fi',
    content: undefined,
  },

  'sv-construction-permit': {
    id: 'sv-construction-permit',
    type: 'template',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '10.10.2023', changedBy: { userName: 'Lars Eriksson', email: 'lars.eriksson@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    serviceId: 'new-construction-permit.printout',
    localeId: 'sv',
    content: undefined,
  },

  'en-construction-permit': {
    id: 'en-construction-permit',
    type: 'template',
    expanded: false,
    reference: false,
    locked: false,
    description: undefined,
    configOptions: [],
    comments: [],
    changes: [
      { changeType: 'create', changeDate: '10.10.2023', changedBy: { userName: 'Lars Eriksson', email: 'lars.eriksson@example.com' } }
    ],
    permissions: [],
    labels: [],
    errors: [],
    serviceId: 'municipal-services-guide.printout',
    localeId: 'en',
    content: undefined,
  },

};
