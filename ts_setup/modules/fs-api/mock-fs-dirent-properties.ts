import { FsDirentProps } from './fs-types'

export const mockFsDirentProperties: Record<string, FsDirentProps> = {
  'content': {
      id: 'content',
      expanded: true,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '07.01.2024',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '08.07.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '07.01.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        }
      ],
      permissions: [],
      labels: [
        {
          id: 'label-021',
          value: 'Needs improvement'
        },
        {
          id: 'label-022',
          value: 'Needs approval'
        },
        {
          id: 'label-023',
          value: 'Protection order'
        }
      ],
      errors: []
    },
  '000_index': {
      id: '000_index',
      expanded: true,
      reference: false,
      locked: true,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '14.08.2023',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '29.04.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '14.01.2025',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Mikael Berg',
          types: [
            'write',
            'view'
          ]
        },
        {
          name: 'Michael Chen',
          types: [
            'write',
            'view'
          ]
        }
      ],
      labels: [
        {
          id: 'label-068',
          value: 'Protection order'
        }
      ],
      errors: []
    },
  'main.article': {
      id: 'main.article',
      expanded: true,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '21.05.2023',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '21.03.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '21.01.2025',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Sarah Johnson',
          types: [
            'read'
          ]
        },
        {
          name: 'part-time staff',
          types: [
            'write'
          ]
        },
        {
          name: 'everyone',
          types: [
            'write',
            'read',
            'view'
          ]
        },
        {
          name: 'John Smith',
          types: [
            'read',
            'view',
            'write'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'ref.article': {
      id: 'ref.article',
      expanded: false,
      reference: true,
      locked: false,
      description: 'Reference article for common content',
      configOptions: [
        {
          anonymousMode: true,
          disabledMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '28.11.2023',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '28.06.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '28.01.2025',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'fi-ref': {
      id: 'fi-ref',
      expanded: false,
      reference: false,
      locked: false,
      description: 'Yleinen viitesisältö',
      configOptions: [
        {
          devMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '03.01.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '19.07.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '03.02.2025',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'everyone',
          types: [
            'read'
          ]
        },
        {
          name: 'Sarah Johnson',
          types: [
            'read'
          ]
        }
      ],
      labels: [],
      errors: [
        {
          code: 'FS-1',
          severity: 'CRITICAL',
          message: 'Missing translation'
        },
        {
          code: 'FS-5',
          severity: 'WARNING',
          message: 'Asset in disabled mode and will not appear on the client-facing side'
        },
        {
          code: 'FS-5',
          severity: 'WARNING',
          message: 'Asset in disabled mode and will not appear on the client-facing side'
        }
      ]
    },
  'en-ref': {
      id: 'en-ref',
      expanded: false,
      reference: false,
      locked: false,
      description: 'Common reference content',
      configOptions: [
        {
          disabledMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '10.10.2023',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '10.06.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '10.02.2025',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: [
        {
          code: 'FS-1',
          severity: 'CRITICAL',
          message: 'Missing translation'
        }
      ]
    },
  'fi-main': {
      id: 'fi-main',
      expanded: false,
      reference: false,
      locked: false,
      description: 'Tervetuloa Sipoon Oma asiointiin!',
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '17.06.2023',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '17.04.2024',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '17.02.2025',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Diana Hasselback',
          types: [
            'view',
            'read',
            'write'
          ]
        },
        {
          name: 'office-staff',
          types: [
            'view',
            'read'
          ]
        },
        {
          name: 'Michael Chen',
          types: [
            'write'
          ]
        },
        {
          name: 'part-time staff',
          types: [
            'read',
            'write',
            'view'
          ]
        }
      ],
      labels: [
        {
          id: 'label-075',
          value: 'Needs approval'
        },
        {
          id: 'label-076',
          value: 'Assignable-only'
        },
        {
          id: 'label-077',
          value: 'Development'
        },
        {
          id: 'label-078',
          value: 'Needs improvement'
        }
      ],
      errors: [
        {
          code: 'FS-2',
          severity: 'WARNING',
          message: 'No level 1 heading found'
        },
        {
          code: 'FS-1',
          severity: 'CRITICAL',
          message: 'Missing translation'
        }
      ]
    },
  'sv-main': {
      id: 'sv-main',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '24.08.2023',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '25.05.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '24.02.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Diana Hasselback',
          types: [
            'write'
          ]
        },
        {
          name: 'office-staff',
          types: [
            'read',
            'write',
            'view'
          ]
        },
        {
          name: 'Mikael Berg',
          types: [
            'view',
            'read'
          ]
        },
        {
          name: 'Michael Chen',
          types: [
            'view',
            'read'
          ]
        }
      ],
      labels: [
        {
          id: 'label-071',
          value: 'Protection order'
        },
        {
          id: 'label-072',
          value: 'Temporary'
        },
        {
          id: 'label-073',
          value: 'Deprecated'
        },
        {
          id: 'label-074',
          value: 'Needs approval'
        }
      ],
      errors: [
        {
          code: 'FS-1',
          severity: 'CRITICAL',
          message: 'Missing translation'
        },
        {
          code: 'FS-2',
          severity: 'WARNING',
          message: 'No level 1 heading found'
        }
      ]
    },
  'en-main': {
      id: 'en-main',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [
        {
          anonymousMode: true,
          assignableMode: true,
          devMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '03.02.2024',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '18.08.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '03.03.2025',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Mikael Berg',
          types: [
            'view'
          ]
        },
        {
          name: 'Sarah Johnson',
          types: [
            'view'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'info-gdpr.article': {
      id: 'info-gdpr.article',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [
        {
          devMode: true,
          disabledMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '10.07.2023',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '09.05.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '10.03.2025',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Mikael Berg',
          types: [
            'read'
          ]
        },
        {
          name: 'Sarah Johnson',
          types: [
            'read',
            'write',
            'view'
          ]
        },
        {
          name: 'Tom Walsh',
          types: [
            'write',
            'read',
            'view'
          ]
        }
      ],
      labels: [],
      errors: [
        {
          code: 'FS-5',
          severity: 'WARNING',
          message: 'Asset in disabled mode and will not appear on the client-facing side'
        }
      ]
    },
  'general-message.service': {
      id: 'general-message.service',
      expanded: true,
      reference: false,
      locked: true,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '17.03.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '15.09.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '17.03.2025',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'fi-general': {
      id: 'fi-general',
      expanded: false,
      reference: false,
      locked: false,
      description: 'Lähetä viesti',
      configOptions: [
        {
          devMode: true,
          assignableMode: true,
          disabledMode: true,
          anonymousMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '24.07.2023',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '23.05.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '24.03.2025',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'John Smith',
          types: [
            'write',
            'read',
            'view'
          ]
        },
        {
          name: 'Sarah Johnson',
          types: [
            'read'
          ]
        },
        {
          name: 'part-time staff',
          types: [
            'read',
            'view'
          ]
        }
      ],
      labels: [],
      errors: [
        {
          code: 'FS-3',
          severity: 'CRITICAL',
          message: 'No Dialob form defined'
        },
        {
          code: 'FS-4',
          severity: 'CRITICAL',
          message: 'Service has no flow defined and cannot process customer requests'
        }
      ]
    },
  'sv-general': {
      id: 'sv-general',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '31.08.2023',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '15.06.2024',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '31.03.2025',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'part-time staff',
          types: [
            'read',
            'write'
          ]
        },
        {
          name: 'Mikael Berg',
          types: [
            'write'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'general-message.dialob': {
      id: 'general-message.dialob',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [
        {
          comment: 'Needs to be reviewed before the next release.',
          author: 'juhani.virtanen',
          created: '2024-09-13'
        },
        {
          comment: 'Updated by the content team, pending sign-off.',
          author: 'pekka.leinonen',
          created: '2024-10-08'
        },
        {
          comment: 'Translation keys added, waiting on localization.',
          author: 'anna.makinen',
          created: '2024-08-20'
        },
        {
          comment: 'Do not modify — locked for compliance review.',
          author: 'maria.korhonen',
          created: '2024-04-24'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '07.10.2023',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '07.07.2024',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '07.04.2025',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        }
      ],
      permissions: [],
      labels: [
        {
          id: 'label-069',
          value: 'Finnish-only'
        },
        {
          id: 'label-070',
          value: 'Deprecated'
        }
      ],
      errors: [
        {
          code: 'FS-1',
          severity: 'CRITICAL',
          message: 'Missing translation'
        },
        {
          code: 'FS-5',
          severity: 'WARNING',
          message: 'Asset in disabled mode and will not appear on the client-facing side'
        }
      ]
    },
  'taskMsgFlow.flow': {
      id: 'taskMsgFlow.flow',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [
        {
          assignableMode: true,
          anonymousMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '14.02.2024',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '13.09.2024',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '14.04.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: [
        {
          code: 'FS-1',
          severity: 'CRITICAL',
          message: 'Missing translation'
        },
        {
          code: 'FS-5',
          severity: 'WARNING',
          message: 'Asset in disabled mode and will not appear on the client-facing side'
        }
      ]
    },
  '100_residence': {
      id: '100_residence',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '21.11.2023',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '05.08.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '21.04.2025',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'ref-article-ref1': {
      id: 'ref-article-ref1',
      expanded: false,
      reference: true,
      locked: false,
      configOptions: [
        {
          devMode: true,
          disabledMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '28.10.2023',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '28.07.2024',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '28.04.2025',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Diana Hasselback',
          types: [
            'write',
            'view',
            'read'
          ]
        },
        {
          name: 'Sarah Johnson',
          types: [
            'view',
            'read'
          ]
        },
        {
          name: 'John Smith',
          types: [
            'view',
            'write',
            'read'
          ]
        },
        {
          name: 'part-time staff',
          types: [
            'view',
            'write',
            'read'
          ]
        }
      ],
      labels: [
        {
          id: 'label-066',
          value: 'Assignable-only'
        },
        {
          id: 'label-067',
          value: 'Development'
        }
      ],
      errors: []
    },
  'general-message-ref1': {
      id: 'general-message-ref1',
      expanded: false,
      reference: true,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '05.12.2023',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '19.08.2024',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '05.05.2025',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Michael Chen',
          types: [
            'write'
          ]
        },
        {
          name: 'Tom Walsh',
          types: [
            'write',
            'read'
          ]
        },
        {
          name: 'Anna Virtanen',
          types: [
            'write',
            'view',
            'read'
          ]
        },
        {
          name: 'everyone',
          types: [
            'read',
            'write',
            'view'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'public-inforeq.service': {
      id: 'public-inforeq.service',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [
        {
          devMode: true,
          assignableMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '12.05.2024',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '10.11.2024',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '12.05.2025',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'everyone',
          types: [
            'view',
            'write'
          ]
        },
        {
          name: 'Sarah Johnson',
          types: [
            'write'
          ]
        },
        {
          name: 'Anna Virtanen',
          types: [
            'read',
            'write'
          ]
        }
      ],
      labels: [],
      errors: [
        {
          code: 'FS-4',
          severity: 'CRITICAL',
          message: 'Service has no flow defined and cannot process customer requests'
        },
        {
          code: 'FS-3',
          severity: 'CRITICAL',
          message: 'No Dialob form defined'
        }
      ]
    },
  'public-inforeq.dialob': {
      id: 'public-inforeq.dialob',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [
        {
          devMode: true
        }
      ],
      comments: [
        {
          comment: 'Marked for removal after Q2 migration.',
          author: 'maria.korhonen',
          created: '2024-11-09'
        },
        {
          comment: 'Reviewed and approved on 2025-11-15.',
          author: 'juhani.virtanen',
          created: '2024-07-01'
        },
        {
          comment: 'Needs to be reviewed before the next release.',
          author: 'satu.nieminen',
          created: '2024-03-06'
        },
        {
          comment: 'Needs to be reviewed before the next release.',
          author: 'anna.makinen',
          created: '2025-03-19'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '19.09.2023',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '19.07.2024',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '19.05.2025',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        }
      ],
      permissions: [],
      labels: [
        {
          id: 'label-061',
          value: 'Deprecated'
        },
        {
          id: 'label-062',
          value: 'Needs approval'
        },
        {
          id: 'label-063',
          value: 'Temporary'
        },
        {
          id: 'label-064',
          value: 'Finnish-only'
        },
        {
          id: 'label-065',
          value: 'Protection order'
        }
      ],
      errors: []
    },
  'taskGenericFlow-ref1': {
      id: 'taskGenericFlow-ref1',
      expanded: false,
      reference: true,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '26.04.2024',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '09.11.2024',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '26.05.2025',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: [
        {
          code: 'FS-5',
          severity: 'WARNING',
          message: 'Asset in disabled mode and will not appear on the client-facing side'
        }
      ]
    },
  '200_democracy': {
      id: '200_democracy',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '02.04.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '01.11.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '02.06.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Tom Walsh',
          types: [
            'write'
          ]
        },
        {
          name: 'John Smith',
          types: [
            'read',
            'write',
            'view'
          ]
        }
      ],
      labels: [
        {
          id: 'label-052',
          value: 'Temporary'
        },
        {
          id: 'label-053',
          value: 'Needs improvement'
        },
        {
          id: 'label-054',
          value: 'Assignable-only'
        }
      ],
      errors: []
    },
  'ref-article-ref2': {
      id: 'ref-article-ref2',
      expanded: false,
      reference: true,
      locked: false,
      configOptions: [
        {
          devMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '09.05.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '23.11.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '09.06.2025',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Sarah Johnson',
          types: [
            'read',
            'write',
            'view'
          ]
        },
        {
          name: 'Anna Virtanen',
          types: [
            'write',
            'read',
            'view'
          ]
        },
        {
          name: 'Tom Walsh',
          types: [
            'view',
            'read',
            'write'
          ]
        },
        {
          name: 'Mikael Berg',
          types: [
            'write',
            'read',
            'view'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'trustee-info-form.service': {
      id: 'trustee-info-form.service',
      expanded: true,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [
        {
          comment: 'Flagged for translation — Finnish version missing.',
          author: 'satu.nieminen',
          created: '2024-05-24'
        },
        {
          comment: 'Translation keys added, waiting on localization.',
          author: 'pekka.leinonen',
          created: '2024-03-17'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '16.03.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '30.10.2024',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '16.06.2025',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Anna Virtanen',
          types: [
            'read'
          ]
        },
        {
          name: 'Mikael Berg',
          types: [
            'write',
            'read'
          ]
        },
        {
          name: 'everyone',
          types: [
            'write',
            'view'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'fi-trustee': {
      id: 'fi-trustee',
      expanded: false,
      reference: false,
      locked: false,
      description: 'Luottamushenkilön tietolomake',
      configOptions: [
        {
          assignableMode: true,
          anonymousMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '23.03.2024',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '06.11.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '23.06.2025',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Tom Walsh',
          types: [
            'view'
          ]
        },
        {
          name: 'office-staff',
          types: [
            'read'
          ]
        },
        {
          name: 'Anna Virtanen',
          types: [
            'read',
            'view'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'trustee-info.dialob': {
      id: 'trustee-info.dialob',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '29.02.2024',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '29.10.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '30.06.2025',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: [
        {
          code: 'FS-5',
          severity: 'WARNING',
          message: 'Asset in disabled mode and will not appear on the client-facing side'
        }
      ]
    },
  'taskGenericFlow-ref2': {
      id: 'taskGenericFlow-ref2',
      expanded: false,
      reference: true,
      locked: false,
      configOptions: [
        {
          anonymousMode: true,
          assignableMode: true,
          devMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '07.02.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '22.10.2024',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '07.07.2025',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'trustee-travel-pay.service': {
      id: 'trustee-travel-pay.service',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [
        {
          devMode: true,
          assignableMode: true,
          disabledMode: true,
          anonymousMode: true
        }
      ],
      comments: [
        {
          comment: 'Original version archived, this is the updated copy.',
          author: 'satu.nieminen',
          created: '2025-06-22'
        },
        {
          comment: 'Needs to be reviewed before the next release.',
          author: 'anna.makinen',
          created: '2025-05-20'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '14.02.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '29.10.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '14.07.2025',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Tom Walsh',
          types: [
            'read',
            'write'
          ]
        },
        {
          name: 'everyone',
          types: [
            'view',
            'write'
          ]
        },
        {
          name: 'Sarah Johnson',
          types: [
            'write'
          ]
        },
        {
          name: 'office-staff',
          types: [
            'view',
            'write',
            'read'
          ]
        }
      ],
      labels: [
        {
          id: 'label-059',
          value: 'Needs approval'
        },
        {
          id: 'label-060',
          value: 'Needs improvement'
        }
      ],
      errors: []
    },
  'trustee-travel-pay.dialob': {
      id: 'trustee-travel-pay.dialob',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '21.03.2024',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '19.11.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '21.07.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Michael Chen',
          types: [
            'write',
            'read'
          ]
        },
        {
          name: 'office-staff',
          types: [
            'view',
            'read'
          ]
        },
        {
          name: 'John Smith',
          types: [
            'view',
            'write',
            'read'
          ]
        },
        {
          name: 'part-time staff',
          types: [
            'write',
            'view',
            'read'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'general-message-ref2': {
      id: 'general-message-ref2',
      expanded: false,
      reference: true,
      locked: false,
      configOptions: [],
      comments: [
        {
          comment: 'Flagged for translation — Finnish version missing.',
          author: 'maria.korhonen',
          created: '2024-08-12'
        },
        {
          comment: 'Do not modify — locked for compliance review.',
          author: 'anna.makinen',
          created: '2024-10-13'
        },
        {
          comment: 'Approved by team lead, ready to publish.',
          author: 'maria.korhonen',
          created: '2024-03-01'
        },
        {
          comment: 'Temporary placeholder, replace before go-live.',
          author: 'anna.makinen',
          created: '2025-05-04'
        },
        {
          comment: 'Translation keys added, waiting on localization.',
          author: 'pekka.leinonen',
          created: '2024-01-12'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '28.06.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '11.01.2025',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '28.07.2025',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        }
      ],
      permissions: [],
      labels: [
        {
          id: 'label-055',
          value: 'Assignable-only'
        },
        {
          id: 'label-056',
          value: 'Protection order'
        },
        {
          id: 'label-057',
          value: 'Finnish-only'
        },
        {
          id: 'label-058',
          value: 'Deprecated'
        }
      ],
      errors: []
    },
  '350_education': {
      id: '350_education',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [
        {
          assignableMode: true,
          devMode: true,
          disabledMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '04.01.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '19.10.2024',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '04.08.2025',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'ref-article-ref3': {
      id: 'ref-article-ref3',
      expanded: false,
      reference: true,
      locked: false,
      configOptions: [
        {
          anonymousMode: true,
          assignableMode: true,
          devMode: true
        }
      ],
      comments: [
        {
          comment: 'Reviewed and approved on 2025-11-15.',
          author: 'juhani.virtanen',
          created: '2025-12-26'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '11.05.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '25.12.2024',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '11.08.2025',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Tom Walsh',
          types: [
            'read'
          ]
        },
        {
          name: 'Michael Chen',
          types: [
            'read',
            'write'
          ]
        }
      ],
      labels: [
        {
          id: 'label-048',
          value: 'Needs approval'
        },
        {
          id: 'label-049',
          value: 'Development'
        },
        {
          id: 'label-050',
          value: 'Protection order'
        },
        {
          id: 'label-051',
          value: 'Assignable-only'
        }
      ],
      errors: []
    },
  'wilma-preschool.service': {
      id: 'wilma-preschool.service',
      expanded: true,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [
        {
          comment: 'Flagged for translation — Finnish version missing.',
          author: 'maria.korhonen',
          created: '2025-11-17'
        },
        {
          comment: 'Marked for removal after Q2 migration.',
          author: 'maria.korhonen',
          created: '2025-11-26'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '18.12.2023',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '17.10.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '18.08.2025',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        }
      ],
      permissions: [],
      labels: [
        {
          id: 'label-047',
          value: 'Needs improvement'
        }
      ],
      errors: []
    },
  'fi-wilma': {
      id: 'fi-wilma',
      expanded: false,
      reference: false,
      locked: false,
      description: 'Wilmatunnukset esiopetuslapsen huoltajalle',
      configOptions: [
        {
          assignableMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '25.02.2024',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '24.11.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '25.08.2025',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'wilma-preschool.dialob': {
      id: 'wilma-preschool.dialob',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [
        {
          comment: 'Reviewed and approved on 2025-11-15.',
          author: 'satu.nieminen',
          created: '2024-06-25'
        },
        {
          comment: 'Do not modify — locked for compliance review.',
          author: 'maria.korhonen',
          created: '2025-04-10'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '01.04.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '16.12.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '01.09.2025',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'part-time staff',
          types: [
            'write',
            'read',
            'view'
          ]
        },
        {
          name: 'Sarah Johnson',
          types: [
            'read',
            'view',
            'write'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'taskGenericFlow-ref3': {
      id: 'taskGenericFlow-ref3',
      expanded: false,
      reference: true,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '08.09.2024',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '09.03.2025',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '08.09.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'part-time staff',
          types: [
            'write'
          ]
        },
        {
          name: 'Tom Walsh',
          types: [
            'read',
            'view',
            'write'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'protection-order-school.service': {
      id: 'protection-order-school.service',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [
        {
          devMode: true,
          anonymousMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '15.05.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '14.01.2025',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '15.09.2025',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Tom Walsh',
          types: [
            'view'
          ]
        },
        {
          name: 'Michael Chen',
          types: [
            'read'
          ]
        },
        {
          name: 'Anna Virtanen',
          types: [
            'view',
            'read'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'protection-order-school.dialob': {
      id: 'protection-order-school.dialob',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [
        {
          assignableMode: true,
          devMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '22.04.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '06.01.2025',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '22.09.2025',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        }
      ],
      permissions: [],
      labels: [
        {
          id: 'label-042',
          value: 'Temporary'
        },
        {
          id: 'label-043',
          value: 'Needs improvement'
        },
        {
          id: 'label-044',
          value: 'Finnish-only'
        },
        {
          id: 'label-045',
          value: 'Development'
        },
        {
          id: 'label-046',
          value: 'Protection order'
        }
      ],
      errors: [
        {
          code: 'FS-1',
          severity: 'CRITICAL',
          message: 'Missing translation'
        },
        {
          code: 'FS-5',
          severity: 'WARNING',
          message: 'Asset in disabled mode and will not appear on the client-facing side'
        }
      ]
    },
  'general-message-ref3': {
      id: 'general-message-ref3',
      expanded: false,
      reference: true,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '29.02.2024',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '14.12.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '29.09.2025',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Michael Chen',
          types: [
            'view',
            'read',
            'write'
          ]
        },
        {
          name: 'office-staff',
          types: [
            'write'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  '400_traffic-and-roads': {
      id: '400_traffic-and-roads',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [
        {
          comment: 'Updated by the content team, pending sign-off.',
          author: 'satu.nieminen',
          created: '2024-12-05'
        },
        {
          comment: 'Flagged for translation — Finnish version missing.',
          author: 'juhani.virtanen',
          created: '2024-01-25'
        },
        {
          comment: 'Original version archived, this is the updated copy.',
          author: 'satu.nieminen',
          created: '2025-03-12'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '06.02.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '06.12.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '06.10.2025',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'John Smith',
          types: [
            'write',
            'view',
            'read'
          ]
        },
        {
          name: 'everyone',
          types: [
            'view'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'ref-article-ref4': {
      id: 'ref-article-ref4',
      expanded: false,
      reference: true,
      locked: false,
      configOptions: [
        {
          anonymousMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '13.08.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '14.03.2025',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '13.10.2025',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'dig-permit.service': {
      id: 'dig-permit.service',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '20.02.2024',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '20.12.2024',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '20.10.2025',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'dig-permit.dialob': {
      id: 'dig-permit.dialob',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '27.04.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '26.01.2025',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '27.10.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'taskSplitFlow.flow': {
      id: 'taskSplitFlow.flow',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [
        {
          devMode: true,
          disabledMode: true,
          anonymousMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '03.06.2024',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '17.02.2025',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '03.11.2025',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'rent-street-area.service': {
      id: 'rent-street-area.service',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '10.03.2024',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '09.01.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '10.11.2025',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'rent-street-area.dialob': {
      id: 'rent-street-area.dialob',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '17.11.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '18.05.2025',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '17.11.2025',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'part-time staff',
          types: [
            'write'
          ]
        },
        {
          name: 'Anna Virtanen',
          types: [
            'read',
            'view'
          ]
        },
        {
          name: 'everyone',
          types: [
            'read',
            'write'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'private-road-sign.service': {
      id: 'private-road-sign.service',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [
        {
          anonymousMode: true,
          devMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '24.09.2024',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '25.04.2025',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '24.11.2025',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Mikael Berg',
          types: [
            'view',
            'read'
          ]
        },
        {
          name: 'John Smith',
          types: [
            'read',
            'view',
            'write'
          ]
        },
        {
          name: 'Tom Walsh',
          types: [
            'write',
            'view',
            'read'
          ]
        },
        {
          name: 'office-staff',
          types: [
            'view'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'private-road-sign.dialob': {
      id: 'private-road-sign.dialob',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '01.11.2024',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '17.05.2025',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '01.12.2025',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Michael Chen',
          types: [
            'write',
            'read',
            'view'
          ]
        },
        {
          name: 'everyone',
          types: [
            'read',
            'view'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'general-message-ref4': {
      id: 'general-message-ref4',
      expanded: false,
      reference: true,
      locked: false,
      configOptions: [
        {
          anonymousMode: true,
          assignableMode: true,
          devMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '08.05.2024',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '21.02.2025',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '08.12.2025',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Sarah Johnson',
          types: [
            'write'
          ]
        },
        {
          name: 'Anna Virtanen',
          types: [
            'view'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  '425_invoicing': {
      id: '425_invoicing',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '15.10.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '16.05.2025',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '15.12.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'main-invoicing.article': {
      id: 'main-invoicing.article',
      expanded: false,
      reference: false,
      locked: false,
      description: 'Laskutus',
      configOptions: [
        {
          devMode: true,
          assignableMode: true,
          disabledMode: true,
          anonymousMode: true
        }
      ],
      comments: [
        {
          comment: 'Referenced by the main service flow, handle with care.',
          author: 'maria.korhonen',
          created: '2024-05-09'
        },
        {
          comment: 'Updated by the content team, pending sign-off.',
          author: 'anna.makinen',
          created: '2024-09-06'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '22.11.2024',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '07.06.2025',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '22.12.2025',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'invoicing-erapaiva.service': {
      id: 'invoicing-erapaiva.service',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [
        {
          disabledMode: true
        }
      ],
      comments: [
        {
          comment: 'Original version archived, this is the updated copy.',
          author: 'anna.makinen',
          created: '2024-10-11'
        },
        {
          comment: 'Approved by team lead, ready to publish.',
          author: 'pekka.leinonen',
          created: '2024-03-19'
        },
        {
          comment: 'Translation keys added, waiting on localization.',
          author: 'maria.korhonen',
          created: '2024-01-11'
        },
        {
          comment: 'Marked for removal after Q2 migration.',
          author: 'satu.nieminen',
          created: '2025-03-27'
        },
        {
          comment: 'Needs to be reviewed before the next release.',
          author: 'pekka.leinonen',
          created: '2025-04-26'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '29.08.2024',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '29.04.2025',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '29.12.2025',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'everyone',
          types: [
            'read',
            'view',
            'write'
          ]
        },
        {
          name: 'Diana Hasselback',
          types: [
            'view'
          ]
        },
        {
          name: 'Mikael Berg',
          types: [
            'write'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'invoicing-erapaiva.dialob': {
      id: 'invoicing-erapaiva.dialob',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '05.12.2024',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '21.06.2025',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '05.01.2026',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Sarah Johnson',
          types: [
            'read',
            'view',
            'write'
          ]
        },
        {
          name: 'office-staff',
          types: [
            'view'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'taskGenericFlow-ref4': {
      id: 'taskGenericFlow-ref4',
      expanded: false,
      reference: true,
      locked: false,
      configOptions: [
        {
          assignableMode: true,
          devMode: true
        }
      ],
      comments: [
        {
          comment: 'Referenced by the main service flow, handle with care.',
          author: 'juhani.virtanen',
          created: '2025-11-06'
        },
        {
          comment: 'Temporary placeholder, replace before go-live.',
          author: 'maria.korhonen',
          created: '2024-08-17'
        },
        {
          comment: 'Updated by the content team, pending sign-off.',
          author: 'maria.korhonen',
          created: '2025-08-03'
        },
        {
          comment: 'Original version archived, this is the updated copy.',
          author: 'anna.makinen',
          created: '2024-10-22'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '12.11.2024',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '13.06.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '12.01.2026',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'part-time staff',
          types: [
            'read',
            'write',
            'view'
          ]
        },
        {
          name: 'Diana Hasselback',
          types: [
            'view',
            'read',
            'write'
          ]
        },
        {
          name: 'Michael Chen',
          types: [
            'write',
            'view'
          ]
        },
        {
          name: 'everyone',
          types: [
            'read',
            'view'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'invoice-copy-request.service': {
      id: 'invoice-copy-request.service',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '19.09.2024',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '20.05.2025',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '19.01.2026',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'John Smith',
          types: [
            'write'
          ]
        },
        {
          name: 'Tom Walsh',
          types: [
            'read'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'invoice-copy-request.dialob': {
      id: 'invoice-copy-request.dialob',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [
        {
          anonymousMode: true,
          assignableMode: true,
          devMode: true
        }
      ],
      comments: [
        {
          comment: 'Flagged for translation — Finnish version missing.',
          author: 'juhani.virtanen',
          created: '2025-12-23'
        },
        {
          comment: 'Translation keys added, waiting on localization.',
          author: 'pekka.leinonen',
          created: '2024-06-09'
        },
        {
          comment: 'Approved by team lead, ready to publish.',
          author: 'juhani.virtanen',
          created: '2025-10-28'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '26.11.2024',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '27.06.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '26.01.2026',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Anna Virtanen',
          types: [
            'view'
          ]
        },
        {
          name: 'everyone',
          types: [
            'write',
            'read',
            'view'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'bank-account-info.service': {
      id: 'bank-account-info.service',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '02.10.2024',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '03.06.2025',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '02.02.2026',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        }
      ],
      permissions: [],
      labels: [
        {
          id: 'label-037',
          value: 'Temporary'
        },
        {
          id: 'label-038',
          value: 'Protection order'
        },
        {
          id: 'label-039',
          value: 'Assignable-only'
        },
        {
          id: 'label-040',
          value: 'Needs improvement'
        },
        {
          id: 'label-041',
          value: 'Finnish-only'
        }
      ],
      errors: []
    },
  'bank-account-info.dialob': {
      id: 'bank-account-info.dialob',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '09.11.2024',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '25.06.2025',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '09.02.2026',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  '517_sipoo-institute': {
      id: '517_sipoo-institute',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '16.10.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '17.06.2025',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '16.02.2026',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'main-institute.article': {
      id: 'main-institute.article',
      expanded: false,
      reference: false,
      locked: false,
      description: 'Sipoon opisto',
      configOptions: [
        {
          anonymousMode: true,
          disabledMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '23.06.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '24.04.2025',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '23.02.2026',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'teacher-info.service': {
      id: 'teacher-info.service',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '02.10.2024',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '17.06.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '02.03.2026',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'teacher-info.dialob': {
      id: 'teacher-info.dialob',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '09.02.2025',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '24.08.2025',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '09.03.2026',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Mikael Berg',
          types: [
            'view'
          ]
        },
        {
          name: 'Michael Chen',
          types: [
            'write'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'taskGenericFlow-ref5': {
      id: 'taskGenericFlow-ref5',
      expanded: false,
      reference: true,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '16.11.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '16.07.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '16.03.2026',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'teacher-travel-pay.service': {
      id: 'teacher-travel-pay.service',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '08.10.2023',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '24.05.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '08.01.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Michael Chen',
          types: [
            'read'
          ]
        },
        {
          name: 'part-time staff',
          types: [
            'read'
          ]
        },
        {
          name: 'Sarah Johnson',
          types: [
            'read'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'teacher-travel-pay.dialob': {
      id: 'teacher-travel-pay.dialob',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [
        {
          comment: 'Translation keys added, waiting on localization.',
          author: 'pekka.leinonen',
          created: '2024-05-15'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '15.12.2023',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '30.06.2024',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '15.01.2025',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'study-voucher.service': {
      id: 'study-voucher.service',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '22.12.2023',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '07.07.2024',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '22.01.2025',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'study-voucher.dialob': {
      id: 'study-voucher.dialob',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '29.11.2023',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '29.06.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '29.01.2025',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'John Smith',
          types: [
            'read',
            'write'
          ]
        },
        {
          name: 'Tom Walsh',
          types: [
            'write',
            'view'
          ]
        },
        {
          name: 'Diana Hasselback',
          types: [
            'view',
            'write',
            'read'
          ]
        },
        {
          name: 'Mikael Berg',
          types: [
            'write',
            'read',
            'view'
          ]
        }
      ],
      labels: [
        {
          id: 'label-036',
          value: 'Finnish-only'
        }
      ],
      errors: []
    },
  'general-message-ref5': {
      id: 'general-message-ref5',
      expanded: false,
      reference: true,
      locked: false,
      configOptions: [
        {
          anonymousMode: true,
          devMode: true,
          disabledMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '05.07.2023',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '20.04.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '05.02.2025',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'John Smith',
          types: [
            'write',
            'read'
          ]
        },
        {
          name: 'Michael Chen',
          types: [
            'write',
            'read'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  '520_sipoo-water': {
      id: '520_sipoo-water',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [
        {
          comment: 'Temporary placeholder, replace before go-live.',
          author: 'pekka.leinonen',
          created: '2024-05-14'
        },
        {
          comment: 'Translation keys added, waiting on localization.',
          author: 'anna.makinen',
          created: '2024-05-13'
        },
        {
          comment: 'Reviewed and approved on 2025-11-15.',
          author: 'satu.nieminen',
          created: '2025-04-13'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '12.02.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '13.08.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '12.02.2025',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Anna Virtanen',
          types: [
            'read',
            'write'
          ]
        },
        {
          name: 'office-staff',
          types: [
            'write',
            'read',
            'view'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'main-water.article': {
      id: 'main-water.article',
      expanded: false,
      reference: false,
      locked: false,
      description: 'Sipoon Vesi',
      configOptions: [
        {
          disabledMode: true
        }
      ],
      comments: [
        {
          comment: 'Temporary placeholder, replace before go-live.',
          author: 'pekka.leinonen',
          created: '2025-04-24'
        },
        {
          comment: 'Updated by the content team, pending sign-off.',
          author: 'maria.korhonen',
          created: '2025-02-19'
        },
        {
          comment: 'Needs to be reviewed before the next release.',
          author: 'maria.korhonen',
          created: '2025-05-12'
        },
        {
          comment: 'Original version archived, this is the updated copy.',
          author: 'satu.nieminen',
          created: '2025-04-25'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '19.08.2023',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '20.05.2024',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '19.02.2025',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'John Smith',
          types: [
            'read',
            'view'
          ]
        },
        {
          name: 'Anna Virtanen',
          types: [
            'write',
            'view'
          ]
        },
        {
          name: 'Diana Hasselback',
          types: [
            'write',
            'read'
          ]
        },
        {
          name: 'Tom Walsh',
          types: [
            'view',
            'read',
            'write'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'water-supply-maintenance.service': {
      id: 'water-supply-maintenance.service',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [
        {
          comment: 'Needs to be reviewed before the next release.',
          author: 'pekka.leinonen',
          created: '2024-09-18'
        },
        {
          comment: 'Needs to be reviewed before the next release.',
          author: 'juhani.virtanen',
          created: '2025-11-24'
        },
        {
          comment: 'Temporary placeholder, replace before go-live.',
          author: 'juhani.virtanen',
          created: '2025-04-09'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '26.11.2023',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '12.07.2024',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '26.02.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'water-supply-maintenance.dialob': {
      id: 'water-supply-maintenance.dialob',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '05.09.2023',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '04.06.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '05.03.2025',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        }
      ],
      permissions: [],
      labels: [
        {
          id: 'label-034',
          value: 'Needs approval'
        },
        {
          id: 'label-035',
          value: 'Needs improvement'
        }
      ],
      errors: []
    },
  'taskGenericFlow-ref6': {
      id: 'taskGenericFlow-ref6',
      expanded: false,
      reference: true,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '12.10.2023',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '26.06.2024',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '12.03.2025',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'office-staff',
          types: [
            'read',
            'view',
            'write'
          ]
        },
        {
          name: 'everyone',
          types: [
            'read',
            'view',
            'write'
          ]
        },
        {
          name: 'Mikael Berg',
          types: [
            'write'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'water-invoice-erapaiva.service': {
      id: 'water-invoice-erapaiva.service',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '19.08.2023',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '03.06.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '19.03.2025',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'everyone',
          types: [
            'read',
            'view'
          ]
        },
        {
          name: 'John Smith',
          types: [
            'view',
            'read',
            'write'
          ]
        },
        {
          name: 'part-time staff',
          types: [
            'write',
            'read'
          ]
        },
        {
          name: 'office-staff',
          types: [
            'view'
          ]
        }
      ],
      labels: [
        {
          id: 'label-032',
          value: 'Needs improvement'
        },
        {
          id: 'label-033',
          value: 'Finnish-only'
        }
      ],
      errors: []
    },
  'water-invoice-erapaiva.dialob': {
      id: 'water-invoice-erapaiva.dialob',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '26.12.2023',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '10.08.2024',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '26.03.2025',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'water-connection-statement.service': {
      id: 'water-connection-statement.service',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '02.04.2024',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '01.10.2024',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '02.04.2025',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'water-connection-statement.dialob': {
      id: 'water-connection-statement.dialob',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '09.01.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '24.08.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '09.04.2025',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Sarah Johnson',
          types: [
            'read',
            'view',
            'write'
          ]
        },
        {
          name: 'Tom Walsh',
          types: [
            'write',
            'view',
            'read'
          ]
        },
        {
          name: 'part-time staff',
          types: [
            'view',
            'read'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'general-message-ref6': {
      id: 'general-message-ref6',
      expanded: false,
      reference: true,
      locked: false,
      configOptions: [],
      comments: [
        {
          comment: 'Do not modify — locked for compliance review.',
          author: 'juhani.virtanen',
          created: '2024-11-12'
        },
        {
          comment: 'Marked for removal after Q2 migration.',
          author: 'anna.makinen',
          created: '2024-11-09'
        },
        {
          comment: 'Check with the product owner before making changes.',
          author: 'pekka.leinonen',
          created: '2024-07-28'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '16.04.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '15.10.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '16.04.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        }
      ],
      permissions: [],
      labels: [
        {
          id: 'label-029',
          value: 'Assignable-only'
        },
        {
          id: 'label-030',
          value: 'Needs improvement'
        },
        {
          id: 'label-031',
          value: 'Finnish-only'
        }
      ],
      errors: []
    },
  '650_leisure-time-and-youth': {
      id: '650_leisure-time-and-youth',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '23.10.2023',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '23.07.2024',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '23.04.2025',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'main-leisure.article': {
      id: 'main-leisure.article',
      expanded: false,
      reference: false,
      locked: false,
      description: 'Vapaa-aika ja nuoret',
      configOptions: [
        {
          anonymousMode: true,
          devMode: true,
          disabledMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '30.04.2024',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '29.10.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '30.04.2025',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'part-time staff',
          types: [
            'write',
            'read'
          ]
        },
        {
          name: 'Anna Virtanen',
          types: [
            'write'
          ]
        },
        {
          name: 'Sarah Johnson',
          types: [
            'read',
            'view',
            'write'
          ]
        }
      ],
      labels: [
        {
          id: 'label-027',
          value: 'Protection order'
        },
        {
          id: 'label-028',
          value: 'Temporary'
        }
      ],
      errors: []
    },
  'sports-grant-settlement.service': {
      id: 'sports-grant-settlement.service',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [
        {
          comment: 'Approved by team lead, ready to publish.',
          author: 'pekka.leinonen',
          created: '2025-02-15'
        },
        {
          comment: 'Approved by team lead, ready to publish.',
          author: 'pekka.leinonen',
          created: '2024-09-10'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '07.09.2023',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '07.07.2024',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '07.05.2025',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Michael Chen',
          types: [
            'read',
            'view'
          ]
        },
        {
          name: 'part-time staff',
          types: [
            'view',
            'write'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'sports-grant-settlement.dialob': {
      id: 'sports-grant-settlement.dialob',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [
        {
          comment: 'Temporary placeholder, replace before go-live.',
          author: 'anna.makinen',
          created: '2024-03-02'
        },
        {
          comment: 'Check with the product owner before making changes.',
          author: 'anna.makinen',
          created: '2025-08-08'
        },
        {
          comment: 'Original version archived, this is the updated copy.',
          author: 'anna.makinen',
          created: '2024-11-24'
        },
        {
          comment: 'Updated by the content team, pending sign-off.',
          author: 'anna.makinen',
          created: '2025-04-25'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '14.10.2023',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '29.07.2024',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '14.05.2025',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'John Smith',
          types: [
            'write'
          ]
        },
        {
          name: 'Tom Walsh',
          types: [
            'read',
            'view'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'taskGenericFlow-ref7': {
      id: 'taskGenericFlow-ref7',
      expanded: false,
      reference: true,
      locked: false,
      configOptions: [
        {
          assignableMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '21.03.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '20.10.2024',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '21.05.2025',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'sports-fee-return.service': {
      id: 'sports-fee-return.service',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [
        {
          anonymousMode: true,
          devMode: true,
          disabledMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '28.10.2023',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '12.08.2024',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '28.05.2025',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Michael Chen',
          types: [
            'read'
          ]
        },
        {
          name: 'Anna Virtanen',
          types: [
            'view'
          ]
        }
      ],
      labels: [
        {
          id: 'label-025',
          value: 'Needs approval'
        },
        {
          id: 'label-026',
          value: 'Development'
        }
      ],
      errors: []
    },
  'sports-fee-return.dialob': {
      id: 'sports-fee-return.dialob',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '04.11.2023',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '19.08.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '04.06.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'children-sport-grant.service': {
      id: 'children-sport-grant.service',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '11.05.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '25.11.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '11.06.2025',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        }
      ],
      permissions: [],
      labels: [
        {
          id: 'label-024',
          value: 'Development'
        }
      ],
      errors: []
    },
  'children-sport-grant.dialob': {
      id: 'children-sport-grant.dialob',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '18.12.2023',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '17.09.2024',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '18.06.2025',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Michael Chen',
          types: [
            'read'
          ]
        },
        {
          name: 'Diana Hasselback',
          types: [
            'write',
            'view'
          ]
        },
        {
          name: 'part-time staff',
          types: [
            'read',
            'write',
            'view'
          ]
        },
        {
          name: 'Sarah Johnson',
          types: [
            'view',
            'write'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'shared': {
      id: 'shared',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [
        {
          assignableMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '25.11.2023',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '09.09.2024',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '25.06.2025',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'shared-taskGenericFlow.flow': {
      id: 'shared-taskGenericFlow.flow',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [
        {
          assignableMode: true
        }
      ],
      comments: [
        {
          comment: 'Original version archived, this is the updated copy.',
          author: 'juhani.virtanen',
          created: '2024-09-10'
        },
        {
          comment: 'Reviewed and approved on 2025-11-15.',
          author: 'maria.korhonen',
          created: '2024-06-07'
        },
        {
          comment: 'Translation keys added, waiting on localization.',
          author: 'pekka.leinonen',
          created: '2025-07-13'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '02.06.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '16.12.2024',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '02.07.2025',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'part-time staff',
          types: [
            'view',
            'write',
            'read'
          ]
        },
        {
          name: 'Michael Chen',
          types: [
            'view'
          ]
        },
        {
          name: 'everyone',
          types: [
            'view',
            'read'
          ]
        },
        {
          name: 'Tom Walsh',
          types: [
            'view',
            'write',
            'read'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'shared-taskMsgFlow.flow': {
      id: 'shared-taskMsgFlow.flow',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [
        {
          devMode: true,
          disabledMode: true
        }
      ],
      comments: [
        {
          comment: 'Flagged for translation — Finnish version missing.',
          author: 'pekka.leinonen',
          created: '2024-09-11'
        },
        {
          comment: 'Marked for removal after Q2 migration.',
          author: 'pekka.leinonen',
          created: '2025-02-28'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '09.02.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '24.10.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '09.07.2025',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'shared-taskSplitFlow.flow': {
      id: 'shared-taskSplitFlow.flow',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [
        {
          comment: 'Updated by the content team, pending sign-off.',
          author: 'maria.korhonen',
          created: '2024-05-06'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '16.04.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '30.11.2024',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '16.07.2025',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Sarah Johnson',
          types: [
            'read'
          ]
        },
        {
          name: 'Tom Walsh',
          types: [
            'write',
            'view',
            'read'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'links': {
      id: 'links',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '23.11.2023',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '22.09.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '23.07.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Anna Virtanen',
          types: [
            'read',
            'view'
          ]
        },
        {
          name: 'Diana Hasselback',
          types: [
            'view',
            'read'
          ]
        },
        {
          name: 'Michael Chen',
          types: [
            'write'
          ]
        }
      ],
      labels: [
        {
          id: 'label-017',
          value: 'Protection order'
        },
        {
          id: 'label-018',
          value: 'Temporary'
        },
        {
          id: 'label-019',
          value: 'Development'
        },
        {
          id: 'label-020',
          value: 'Needs approval'
        }
      ],
      errors: []
    },
  'sipoo-main-site.link': {
      id: 'sipoo-main-site.link',
      expanded: false,
      reference: false,
      locked: false,
      description: 'https://www.sipoo.fi',
      configOptions: [
        {
          devMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '30.05.2024',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '29.12.2024',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '30.07.2025',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'wilma-info.link': {
      id: 'wilma-info.link',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [
        {
          disabledMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '06.05.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '20.12.2024',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '06.08.2025',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Diana Hasselback',
          types: [
            'write',
            'view',
            'read'
          ]
        },
        {
          name: 'part-time staff',
          types: [
            'read',
            'view',
            'write'
          ]
        },
        {
          name: 'Anna Virtanen',
          types: [
            'read',
            'write',
            'view'
          ]
        },
        {
          name: 'John Smith',
          types: [
            'view',
            'write'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'lupapiste.link': {
      id: 'lupapiste.link',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [
        {
          disabledMode: true
        }
      ],
      comments: [
        {
          comment: 'Check with the product owner before making changes.',
          author: 'anna.makinen',
          created: '2025-01-05'
        },
        {
          comment: 'Check with the product owner before making changes.',
          author: 'satu.nieminen',
          created: '2024-10-14'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '13.12.2023',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '12.10.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '13.08.2025',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'printouts': {
      id: 'printouts',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '20.05.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '03.01.2025',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '20.08.2025',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'sipoo-main-logo.png': {
      id: 'sipoo-main-logo.png',
      expanded: false,
      reference: false,
      locked: false,
      description: 'logo: black and white',
      configOptions: [
        {
          devMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '27.05.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '10.01.2025',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '27.08.2025',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        }
      ],
      permissions: [],
      labels: [
        {
          id: 'label-014',
          value: 'Needs approval'
        },
        {
          id: 'label-015',
          value: 'Deprecated'
        },
        {
          id: 'label-016',
          value: 'Development'
        }
      ],
      errors: []
    },
  'sipoo-color-logo.png': {
      id: 'sipoo-color-logo.png',
      expanded: false,
      reference: false,
      locked: false,
      description: 'logo: full color version',
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '03.02.2024',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '18.11.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '03.09.2025',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Diana Hasselback',
          types: [
            'read'
          ]
        },
        {
          name: 'Anna Virtanen',
          types: [
            'read',
            'write',
            'view'
          ]
        },
        {
          name: 'part-time staff',
          types: [
            'view',
            'write',
            'read'
          ]
        },
        {
          name: 'Michael Chen',
          types: [
            'write'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'municipal-seal.svg': {
      id: 'municipal-seal.svg',
      expanded: false,
      reference: false,
      locked: false,
      description: 'official municipal seal',
      configOptions: [
        {
          devMode: true
        }
      ],
      comments: [
        {
          comment: 'Translation keys added, waiting on localization.',
          author: 'satu.nieminen',
          created: '2024-01-21'
        },
        {
          comment: 'Referenced by the main service flow, handle with care.',
          author: 'juhani.virtanen',
          created: '2025-09-13'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '10.03.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '09.12.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '10.09.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Anna Virtanen',
          types: [
            'view',
            'write'
          ]
        },
        {
          name: 'everyone',
          types: [
            'write'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'new-construction-permit.printout': {
      id: 'new-construction-permit.printout',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [
        {
          comment: 'Flagged for translation — Finnish version missing.',
          author: 'satu.nieminen',
          created: '2024-01-04'
        },
        {
          comment: 'Flagged for translation — Finnish version missing.',
          author: 'maria.korhonen',
          created: '2025-11-27'
        },
        {
          comment: 'Updated by the content team, pending sign-off.',
          author: 'pekka.leinonen',
          created: '2025-11-03'
        },
        {
          comment: 'Approved by team lead, ready to publish.',
          author: 'juhani.virtanen',
          created: '2024-07-11'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '17.03.2024',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '16.12.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '17.09.2025',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Mikael Berg',
          types: [
            'read'
          ]
        },
        {
          name: 'part-time staff',
          types: [
            'read'
          ]
        }
      ],
      labels: [
        {
          id: 'label-010',
          value: 'Protection order'
        },
        {
          id: 'label-011',
          value: 'Development'
        },
        {
          id: 'label-012',
          value: 'Temporary'
        }
      ],
      errors: []
    },
  'fi-construction-permit': {
      id: 'fi-construction-permit',
      expanded: false,
      reference: false,
      locked: false,
      description: 'Finnish construction permit template',
      configOptions: [
        {
          devMode: true,
          anonymousMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '24.03.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '23.12.2024',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '24.09.2025',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'sv-construction-permit': {
      id: 'sv-construction-permit',
      expanded: false,
      reference: false,
      locked: false,
      description: 'Swedish construction permit template',
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '01.06.2024',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '30.01.2025',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '01.10.2025',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Diana Hasselback',
          types: [
            'view'
          ]
        },
        {
          name: 'office-staff',
          types: [
            'read'
          ]
        },
        {
          name: 'Anna Virtanen',
          types: [
            'read',
            'write'
          ]
        }
      ],
      labels: [
        {
          id: 'label-013',
          value: 'Needs improvement'
        }
      ],
      errors: []
    },
  'municipal-services-guide.printout': {
      id: 'municipal-services-guide.printout',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [
        {
          anonymousMode: true,
          assignableMode: true,
          disabledMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '08.02.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '08.12.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '08.10.2025',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Tom Walsh',
          types: [
            'write',
            'view',
            'read'
          ]
        },
        {
          name: 'everyone',
          types: [
            'view'
          ]
        }
      ],
      labels: [
        {
          id: 'label-006',
          value: 'Needs improvement'
        },
        {
          id: 'label-007',
          value: 'Finnish-only'
        },
        {
          id: 'label-008',
          value: 'Protection order'
        },
        {
          id: 'label-009',
          value: 'Assignable-only'
        }
      ],
      errors: []
    },
  'fi-services-guide': {
      id: 'fi-services-guide',
      expanded: false,
      reference: false,
      locked: false,
      description: 'Kunnan palveluopas',
      configOptions: [
        {
          assignableMode: true,
          disabledMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '15.02.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '15.12.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '15.10.2025',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'office-staff',
          types: [
            'view',
            'write',
            'read'
          ]
        },
        {
          name: 'Michael Chen',
          types: [
            'view',
            'read',
            'write'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'sv-services-guide': {
      id: 'sv-services-guide',
      expanded: false,
      reference: false,
      locked: false,
      description: 'Kommunal servicehandbok',
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '22.03.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '05.01.2025',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '22.10.2025',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Michael Chen',
          types: [
            'view'
          ]
        },
        {
          name: 'Sarah Johnson',
          types: [
            'write',
            'read'
          ]
        },
        {
          name: 'Tom Walsh',
          types: [
            'read',
            'write',
            'view'
          ]
        },
        {
          name: 'Diana Hasselback',
          types: [
            'read',
            'view',
            'write'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'en-services-guide': {
      id: 'en-services-guide',
      expanded: false,
      reference: false,
      locked: false,
      description: 'Municipal services guide',
      configOptions: [
        {
          assignableMode: true
        }
      ],
      comments: [
        {
          comment: 'Approved by team lead, ready to publish.',
          author: 'maria.korhonen',
          created: '2024-07-07'
        },
        {
          comment: 'Temporary placeholder, replace before go-live.',
          author: 'anna.makinen',
          created: '2024-08-17'
        },
        {
          comment: 'Reviewed and approved on 2025-11-15.',
          author: 'juhani.virtanen',
          created: '2025-01-12'
        },
        {
          comment: 'Marked for removal after Q2 migration.',
          author: 'juhani.virtanen',
          created: '2024-12-26'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '29.06.2024',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '27.02.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '29.10.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Sarah Johnson',
          types: [
            'read',
            'view',
            'write'
          ]
        },
        {
          name: 'Anna Virtanen',
          types: [
            'write',
            'read',
            'view'
          ]
        },
        {
          name: 'Mikael Berg',
          types: [
            'write',
            'read',
            'view'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'waste-management-info.printout': {
      id: 'waste-management-info.printout',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '05.04.2024',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '19.01.2025',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '05.11.2025',
          changedBy: {
            userName: 'Tom Walsh',
            email: 'tom.walsh@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'Anna Virtanen',
          types: [
            'read',
            'view',
            'write'
          ]
        },
        {
          name: 'Michael Chen',
          types: [
            'write',
            'view'
          ]
        },
        {
          name: 'Sarah Johnson',
          types: [
            'write',
            'view'
          ]
        },
        {
          name: 'Mikael Berg',
          types: [
            'write'
          ]
        }
      ],
      labels: [],
      errors: []
    },
  'fi-waste-info': {
      id: 'fi-waste-info',
      expanded: false,
      reference: false,
      locked: false,
      description: 'Jätehuolto-ohje',
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '12.06.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '26.02.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '12.11.2025',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'tax-information-leaflet.printout': {
      id: 'tax-information-leaflet.printout',
      expanded: false,
      reference: false,
      locked: false,
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '19.08.2024',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '04.04.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '19.11.2025',
          changedBy: {
            userName: 'Anna Virtanen',
            email: 'anna.virtanen@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'fi-tax-info': {
      id: 'fi-tax-info',
      expanded: false,
      reference: false,
      locked: false,
      description: 'Verotietoesite',
      configOptions: [],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '26.03.2024',
          changedBy: {
            userName: 'Michael Chen',
            email: 'michael.chen@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '25.01.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '26.11.2025',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'sv-tax-info': {
      id: 'sv-tax-info',
      expanded: false,
      reference: false,
      locked: false,
      description: 'Skatteinformationsbroschyr',
      configOptions: [
        {
          anonymousMode: true,
          devMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '03.04.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '01.02.2025',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '03.12.2025',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        }
      ],
      permissions: [],
      labels: [],
      errors: []
    },
  'en-tax-info': {
      id: 'en-tax-info',
      expanded: false,
      reference: false,
      locked: false,
      description: 'Tax information leaflet',
      configOptions: [
        {
          disabledMode: true
        }
      ],
      comments: [],
      changes: [
        {
          changeType: 'create',
          changeDate: '10.10.2024',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '11.05.2025',
          changedBy: {
            userName: 'John Smith',
            email: 'john.smith@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '10.12.2025',
          changedBy: {
            userName: 'Diana Hasselback',
            email: 'diana.hasselback@example.com'
          }
        }
      ],
      permissions: [],
      labels: [
        {
          id: 'label-005',
          value: 'Finnish-only'
        }
      ],
      errors: []
    },
  'ee-tax-info': {
      id: 'ee-tax-info',
      expanded: false,
      reference: false,
      locked: false,
      description: 'Maksuteabe leht',
      configOptions: [],
      comments: [
        {
          comment: 'Original version archived, this is the updated copy.',
          author: 'pekka.leinonen',
          created: '2024-10-11'
        },
        {
          comment: 'Check with the product owner before making changes.',
          author: 'maria.korhonen',
          created: '2024-08-11'
        }
      ],
      changes: [
        {
          changeType: 'create',
          changeDate: '17.11.2024',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '02.06.2025',
          changedBy: {
            userName: 'Mikael Berg',
            email: 'mikael.berg@example.com'
          }
        },
        {
          changeType: 'update',
          changeDate: '17.12.2025',
          changedBy: {
            userName: 'Sarah Johnson',
            email: 'sarah.johnson@example.com'
          }
        }
      ],
      permissions: [
        {
          name: 'office-staff',
          types: [
            'view',
            'read'
          ]
        },
        {
          name: 'Mikael Berg',
          types: [
            'view',
            'read',
            'write'
          ]
        },
        {
          name: 'Sarah Johnson',
          types: [
            'view',
            'read',
            'write'
          ]
        }
      ],
      labels: [
        {
          id: 'label-001',
          value: 'Temporary'
        },
        {
          id: 'label-002',
          value: 'Assignable-only'
        },
        {
          id: 'label-003',
          value: 'Needs approval'
        },
        {
          id: 'label-004',
          value: 'Finnish-only'
        }
      ],
      errors: []
    }
};
