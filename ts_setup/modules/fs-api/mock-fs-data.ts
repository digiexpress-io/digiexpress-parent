import { FsNode } from './fs-types'

export const mockFsData: FsNode[] = [
  {
    id: 'content',
    name: 'content',
    type: 'folder',
    comments: undefined,
    expanded: true,
    reference: false,
    locked: false,
    error: false,
    children: [
      {
        id: '000_index',
        name: '000_index',
        type: 'folder',
        comments: undefined,
        expanded: true,
        reference: false,
        locked: true,
        error: false,
        configOptions: [{
          devMode: true,
        }],
        labels: [
          {
            id: 'index-label-1',
            value: 'main-content',
            nodeId: '000_index'
          },
          {
            id: 'index-label-2',
            value: 'featured',
            nodeId: '000_index'
          }
        ],
        children: [
          {
            id: 'main.article',
            name: 'main.article',
            type: 'article',
            error: true,
            comments: [
              {
                comment: 'Found a typo in the content - should be "municipality" not "municipalty"',
                author: 'Sarah Johnson',
                created: '1.2.2025 14:30'
              },
              {
                comment: 'The Finnish translation needs review by a native speaker',
                author: 'Michael Chen',
                created: '28.1.2025 10:15'
              },
              {
                comment: 'Consider adding a confirmation dialog before deletion',
                author: 'Tom Walsh',
                created: '18.1.2025 11:35'
              }
            ],
            expanded: true,
            reference: false,
            locked: false,
            configOptions: [{
              disabledMode: true,
            }],
            labels: [
              {
                id: 'main-article-label-1',
                value: 'homepage',
                nodeId: 'main.article'
              },
              {
                id: 'main-article-label-2',
                value: 'welcome',
                nodeId: 'main.article'
              },
              {
                id: 'main-article-label-3',
                value: 'multilingual',
                nodeId: 'main.article'
              }
            ],
            children: [
              {
                id: 'ref.article',
                name: 'ref.article',
                type: 'article',
                comments: undefined,
                expanded: false,
                reference: true,
                locked: false,
                error: false,
                description: 'Reference article for common content',
                labels: [
                  {
                    id: 'ref-article-label-1',
                    value: 'reference',
                    nodeId: 'ref.article'
                  },
                  {
                    id: 'ref-article-label-2',
                    value: 'reusable',
                    nodeId: 'ref.article'
                  },
                  {
                    id: 'ref-article-label-3',
                    value: 'common-content',
                    nodeId: 'ref.article'
                  }
                ],
                children: [
                  {
                    id: 'fi-ref',
                    name: 'fi',
                    type: 'language',
                    comments: undefined,
                    expanded: false,
                    reference: false,
                    locked: false,
                    error: false,
                    description: 'Yleinen viitesisältö',
                    labels: [
                      {
                        id: 'fi-ref-label-1',
                        value: 'finnish',
                        nodeId: 'fi-ref'
                      },
                      {
                        id: 'fi-ref-label-2',
                        value: 'reference-content',
                        nodeId: 'fi-ref'
                      }
                    ]
                  },
                  {
                    id: 'en-ref',
                    name: 'en',
                    type: 'language',
                    comments: undefined,
                    expanded: false,
                    reference: false,
                    locked: false,
                    error: false,
                    description: 'Common reference content',
                    labels: [
                      {
                        id: 'en-ref-label-1',
                        value: 'english',
                        nodeId: 'en-ref'
                      },
                      {
                        id: 'en-ref-label-2',
                        value: 'reference-content',
                        nodeId: 'en-ref'
                      }
                    ]
                  }
                ]
              },
              {
                id: 'fi-main',
                name: 'fi',
                type: 'language',
                comments: undefined,
                expanded: false,
                reference: false,
                locked: false,
                error: false,
                description: 'Tervetuloa Sipoon Oma asiointiin!',
                labels: [
                  {
                    id: 'fi-main-label-1',
                    value: 'finnish',
                    nodeId: 'fi-main'
                  },
                  {
                    id: 'fi-main-label-2',
                    value: 'primary-language',
                    nodeId: 'fi-main'
                  }
                ]
              },
              {
                id: 'sv-main',
                name: 'sv',
                type: 'language',
                comments: undefined,
                expanded: false,
                reference: false,
                locked: false,
                error: false,
                labels: [
                  {
                    id: 'sv-main-label-1',
                    value: 'swedish',
                    nodeId: 'sv-main'
                  },
                  {
                    id: 'sv-main-label-2',
                    value: 'secondary-language',
                    nodeId: 'sv-main'
                  }
                ]
              },
              {
                id: 'en-main',
                name: 'en',
                type: 'language',
                comments: undefined,
                expanded: false,
                reference: false,
                locked: false,
                error: false,
                labels: [
                  {
                    id: 'en-main-label-1',
                    value: 'english',
                    nodeId: 'en-main'
                  },
                  {
                    id: 'en-main-label-2',
                    value: 'international',
                    nodeId: 'en-main'
                  }
                ]
              }
            ]
          },
          {
            id: 'info-gdpr.article',
            name: 'info-gdpr.article',
            type: 'article',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false,
            configOptions: [{
              devMode: true,
              disabledMode: true,
            }],
            labels: [
              {
                id: 'gdpr-label-1',
                value: 'privacy',
                nodeId: 'info-gdpr.article'
              },
              {
                id: 'gdpr-label-2',
                value: 'legal',
                nodeId: 'info-gdpr.article'
              },
              {
                id: 'gdpr-label-3',
                value: 'compliance',
                nodeId: 'info-gdpr.article'
              },
              {
                id: 'gdpr-label-4',
                value: 'data-protection',
                nodeId: 'info-gdpr.article'
              }
            ]
          },
          {
            id: 'general-message.service',
            name: 'general-message.service',
            type: 'service',
            comments: undefined,
            expanded: true,
            reference: false,
            locked: true,
            error: false,
            configOptions: [{
              devMode: true,
              assignableMode: true,
              disabledMode: true,
              anonymousMode: true,
            }],
            labels: [
              {
                id: 'general-msg-label-1',
                value: 'communication',
                nodeId: 'general-message.service'
              },
              {
                id: 'general-msg-label-2',
                value: 'messaging',
                nodeId: 'general-message.service'
              },
              {
                id: 'general-msg-label-3',
                value: 'contact-form',
                nodeId: 'general-message.service'
              },
              {
                id: 'general-msg-label-4',
                value: 'public-service',
                nodeId: 'general-message.service'
              },
              {
                id: 'general-msg-label-5',
                value: 'reusable',
                nodeId: 'general-message.service'
              }
            ],
            children: [
              {
                id: 'fi-general',
                name: 'fi',
                type: 'language',
                comments: undefined,
                expanded: false,
                reference: false,
                locked: false,
                error: false,
                description: 'Lähetä viesti',
                labels: [
                  {
                    id: 'fi-general-label-1',
                    value: 'finnish',
                    nodeId: 'fi-general'
                  },
                  {
                    id: 'fi-general-label-2',
                    value: 'send-message',
                    nodeId: 'fi-general'
                  }
                ]
              },
              {
                id: 'sv-general',
                name: 'sv',
                type: 'language',
                comments: undefined,
                expanded: false,
                reference: false,
                locked: false,
                error: false,
                labels: [
                  {
                    id: 'sv-general-label-1',
                    value: 'swedish',
                    nodeId: 'sv-general'
                  }
                ]
              }
            ]
          },
          {
            id: 'general-message.dialob',
            name: 'general-message.dialob',
            type: 'dialob',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false,
            labels: [
              {
                id: 'general-dialob-label-1',
                value: 'form',
                nodeId: 'general-message.dialob'
              },
              {
                id: 'general-dialob-label-2',
                value: 'user-input',
                nodeId: 'general-message.dialob'
              },
              {
                id: 'general-dialob-label-3',
                value: 'messaging',
                nodeId: 'general-message.dialob'
              }
            ]
          },
          {
            id: 'taskMsgFlow.flow',
            name: 'taskMsgFlow.flow',
            type: 'flow',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false,
            configOptions: [{
              assignableMode: true,
              anonymousMode: true,
            }],
            labels: [
              {
                id: 'msg-flow-label-1',
                value: 'workflow',
                nodeId: 'taskMsgFlow.flow'
              },
              {
                id: 'msg-flow-label-2',
                value: 'task-management',
                nodeId: 'taskMsgFlow.flow'
              },
              {
                id: 'msg-flow-label-3',
                value: 'message-routing',
                nodeId: 'taskMsgFlow.flow'
              }
            ]
          }
        ]
      },
      {
        id: '100_residence',
        name: '100_residence',
        type: 'folder',
        comments: undefined,
        expanded: false,
        reference: false,
        locked: false,
        error: false,
        labels: [
          {
            id: 'residence-label-1',
            value: 'housing',
            nodeId: '100_residence'
          },
          {
            id: 'residence-label-2',
            value: 'category-100',
            nodeId: '100_residence'
          }
        ],
        children: [
          {
            id: 'ref-article-ref1',
            name: 'ref.article',
            type: 'article',
            comments: undefined,
            expanded: false,
            reference: true,
            locked: false,
            error: false
          },
          {
            id: 'general-message-ref1',
            name: 'general-message.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: true,
            locked: false,
            error: false
          },
          {
            id: 'public-inforeq.service',
            name: 'public-inforeq.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false,
            configOptions: [{
              devMode: true,
              assignableMode: true,
            }]
          },
          {
            id: 'public-inforeq.dialob',
            name: 'public-inforeq.dialob',
            type: 'dialob',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'taskGenericFlow-ref1',
            name: 'taskGenericFlow.flow',
            type: 'flow',
            comments: undefined,
            expanded: false,
            reference: true,
            locked: false,
            error: false
          }
        ]
      },
      {
        id: '200_democracy',
        name: '200_democracy',
        type: 'folder',
        comments: undefined,
        expanded: false,
        reference: false,
        locked: false,
        error: false,
        labels: [
          {
            id: 'democracy-label-1',
            value: 'governance',
            nodeId: '200_democracy'
          },
          {
            id: 'democracy-label-2',
            value: 'sensitive',
            nodeId: '200_democracy'
          },
          {
            id: 'democracy-label-3',
            value: 'temporary',
            nodeId: '200_democracy'
          },
          {
            id: 'democracy-label-4',
            value: 'voting-rights',
            nodeId: '200_democracy'
          },
          {
            id: 'democracy-label-5',
            value: 'hasPrintout',
            nodeId: '200_democracy'
          }
        ],
        children: [
          {
            id: 'ref-article-ref2',
            name: 'ref.article',
            type: 'article',
            comments: undefined,
            expanded: false,
            reference: true,
            locked: false,
            error: false
          },
          {
            id: 'trustee-info-form.service',
            name: 'trustee-info-form.service',
            type: 'service',
            comments: undefined,
            expanded: true,
            reference: false,
            locked: false,
            error: false,
            children: [
              {
                id: 'fi-trustee',
                name: 'fi',
                type: 'language',
                comments: undefined,
                expanded: false,
                reference: false,
                locked: false,
                error: false,
                description: 'Luottamushenkilön tietolomake'
              }
            ]
          },
          {
            id: 'trustee-info.dialob',
            name: 'trustee-info.dialob',
            type: 'dialob',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'taskGenericFlow-ref2',
            name: 'taskGenericFlow.flow',
            type: 'flow',
            comments: undefined,
            expanded: false,
            reference: true,
            locked: false,
            error: false
          },
          {
            id: 'trustee-travel-pay.service',
            name: 'trustee-travel-pay.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'trustee-travel-pay.dialob',
            name: 'trustee-travel-pay.dialob',
            type: 'dialob',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'general-message-ref2',
            name: 'general-message.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: true,
            locked: false,
            error: false
          }
        ]
      },
      {
        id: '350_education',
        name: '350_education',
        type: 'folder',
        comments: undefined,
        expanded: false,
        reference: false,
        locked: false,
        error: false,
        children: [
          {
            id: 'ref-article-ref3',
            name: 'ref.article',
            type: 'article',
            comments: undefined,
            expanded: false,
            reference: true,
            locked: false,
            error: false
          },
          {
            id: 'wilma-preschool.service',
            name: 'wilma-preschool.service',
            type: 'service',
            comments: undefined,
            expanded: true,
            reference: false,
            locked: false,
            error: false,
            children: [
              {
                id: 'fi-wilma',
                name: 'fi',
                type: 'language',
                comments: undefined,
                expanded: false,
                reference: false,
                locked: false,
                error: false,
                description: 'Wilmatunnukset esiopetuslapsen huoltajalle'
              }
            ]
          },
          {
            id: 'wilma-preschool.dialob',
            name: 'wilma-preschool.dialob',
            type: 'dialob',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'taskGenericFlow-ref3',
            name: 'taskGenericFlow.flow',
            type: 'flow',
            comments: undefined,
            expanded: false,
            reference: true,
            locked: false,
            error: false
          },
          {
            id: 'protection-order-school.service',
            name: 'protection-order-school.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'protection-order-school.dialob',
            name: 'protection-order-school.dialob',
            type: 'dialob',
            error: true,
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
          },
          {
            id: 'general-message-ref3',
            name: 'general-message.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: true,
            locked: false,
            error: false
          }
        ]
      },
      {
        id: '400_traffic-and-roads',
        name: '400_traffic-and-roads',
        type: 'folder',
        comments: undefined,
        expanded: false,
        reference: false,
        locked: false,
        error: false,
        children: [
          {
            id: 'ref-article-ref4',
            name: 'ref.article',
            type: 'article',
            comments: undefined,
            expanded: false,
            reference: true,
            locked: false,
            error: false
          },
          {
            id: 'dig-permit.service',
            name: 'dig-permit.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'dig-permit.dialob',
            name: 'dig-permit.dialob',
            type: 'dialob',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'taskSplitFlow.flow',
            name: 'taskSplitFlow.flow',
            type: 'flow',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false,
            configOptions: [{
              devMode: true,
              disabledMode: true,
              anonymousMode: true,
            }]
          },
          {
            id: 'rent-street-area.service',
            name: 'rent-street-area.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'rent-street-area.dialob',
            name: 'rent-street-area.dialob',
            type: 'dialob',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'private-road-sign.service',
            name: 'private-road-sign.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'private-road-sign.dialob',
            name: 'private-road-sign.dialob',
            type: 'dialob',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'general-message-ref4',
            name: 'general-message.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: true,
            locked: false,
            error: false
          }
        ]
      },
      {
        id: '425_invoicing',
        name: '425_invoicing',
        type: 'folder',
        comments: undefined,
        expanded: false,
        reference: false,
        locked: false,
        error: false,
        children: [
          {
            id: 'main-invoicing.article',
            name: 'main.article',
            type: 'article',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false,
            description: 'Laskutus'
          },
          {
            id: 'invoicing-erapaiva.service',
            name: 'invoicing-erapaiva.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'invoicing-erapaiva.dialob',
            name: 'invoicing-erapaiva.dialob',
            type: 'dialob',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'taskGenericFlow-ref4',
            name: 'taskGenericFlow.flow',
            type: 'flow',
            comments: undefined,
            expanded: false,
            reference: true,
            locked: false,
            error: false
          },
          {
            id: 'invoice-copy-request.service',
            name: 'invoice-copy-request.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'invoice-copy-request.dialob',
            name: 'invoice-copy-request.dialob',
            type: 'dialob',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'bank-account-info.service',
            name: 'bank-account-info.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'bank-account-info.dialob',
            name: 'bank-account-info.dialob',
            type: 'dialob',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          }
        ]
      },
      {
        id: '517_sipoo-institute',
        name: '517_sipoo-institute',
        type: 'folder',
        comments: undefined,
        expanded: false,
        reference: false,
        locked: false,
        error: false,
        children: [
          {
            id: 'main-institute.article',
            name: 'main.article',
            type: 'article',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false,
            description: 'Sipoon opisto'
          },
          {
            id: 'teacher-info.service',
            name: 'teacher-info.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'teacher-info.dialob',
            name: 'teacher-info.dialob',
            type: 'dialob',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'taskGenericFlow-ref5',
            name: 'taskGenericFlow.flow',
            type: 'flow',
            comments: undefined,
            expanded: false,
            reference: true,
            locked: false,
            error: false
          },
          {
            id: 'teacher-travel-pay.service',
            name: 'teacher-travel-pay.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'teacher-travel-pay.dialob',
            name: 'teacher-travel-pay.dialob',
            type: 'dialob',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'study-voucher.service',
            name: 'study-voucher.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'study-voucher.dialob',
            name: 'study-voucher.dialob',
            type: 'dialob',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'general-message-ref5',
            name: 'general-message.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: true,
            locked: false,
            error: false
          }
        ]
      },
      {
        id: '520_sipoo-water',
        name: '520_sipoo-water',
        type: 'folder',
        comments: undefined,
        expanded: false,
        reference: false,
        locked: false,
        error: false,
        configOptions: [{
          disabledMode: true,
        }],
        children: [
          {
            id: 'main-water.article',
            name: 'main.article',
            type: 'article',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false,
            description: 'Sipoon Vesi'
          },
          {
            id: 'water-supply-maintenance.service',
            name: 'water-supply-maintenance.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'water-supply-maintenance.dialob',
            name: 'water-supply-maintenance.dialob',
            type: 'dialob',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'taskGenericFlow-ref6',
            name: 'taskGenericFlow.flow',
            type: 'flow',
            comments: undefined,
            expanded: false,
            reference: true,
            locked: false,
            error: false
          },
          {
            id: 'water-invoice-erapaiva.service',
            name: 'water-invoice-erapaiva.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'water-invoice-erapaiva.dialob',
            name: 'water-invoice-erapaiva.dialob',
            type: 'dialob',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'water-connection-statement.service',
            name: 'water-connection-statement.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'water-connection-statement.dialob',
            name: 'water-connection-statement.dialob',
            type: 'dialob',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'general-message-ref6',
            name: 'general-message.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: true,
            locked: false,
            error: false
          }
        ]
      },
      {
        id: '650_leisure-time-and-youth',
        name: '650_leisure-time-and-youth',
        type: 'folder',
        comments: undefined,
        expanded: false,
        reference: false,
        locked: false,
        error: false,
        children: [
          {
            id: 'main-leisure.article',
            name: 'main.article',
            type: 'article',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false,
            description: 'Vapaa-aika ja nuoret'
          },
          {
            id: 'sports-grant-settlement.service',
            name: 'sports-grant-settlement.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'sports-grant-settlement.dialob',
            name: 'sports-grant-settlement.dialob',
            type: 'dialob',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'taskGenericFlow-ref7',
            name: 'taskGenericFlow.flow',
            type: 'flow',
            comments: undefined,
            expanded: false,
            reference: true,
            locked: false,
            error: false
          },
          {
            id: 'sports-fee-return.service',
            name: 'sports-fee-return.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'sports-fee-return.dialob',
            name: 'sports-fee-return.dialob',
            type: 'dialob',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'children-sport-grant.service',
            name: 'children-sport-grant.service',
            type: 'service',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          },
          {
            id: 'children-sport-grant.dialob',
            name: 'children-sport-grant.dialob',
            type: 'dialob',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false
          }
        ]
      }
    ]
  },
  {
    id: 'shared',
    name: 'shared',
    type: 'folder',
    comments: undefined,
    expanded: false,
    reference: false,
    locked: false,
    error: false,
    children: [
      {
        id: 'shared-taskGenericFlow.flow',
        name: 'taskGenericFlow.flow',
        type: 'flow',
        comments: undefined,
        expanded: false,
        reference: false,
        locked: false,
        error: false
      },
      {
        id: 'shared-taskMsgFlow.flow',
        name: 'taskMsgFlow.flow',
        type: 'flow',
        comments: undefined,
        expanded: false,
        reference: false,
        locked: false,
        error: false
      },
      {
        id: 'shared-taskSplitFlow.flow',
        name: 'taskSplitFlow.flow',
        type: 'flow',
        comments: undefined,
        expanded: false,
        reference: false,
        locked: false,
        error: false
      }
    ]
  },
  {
    id: 'links',
    name: 'links',
    type: 'folder',
    comments: undefined,
    expanded: false,
    reference: false,
    locked: false,
    error: false,
    children: [
      {
        id: 'sipoo-main-site.link',
        name: 'sipoo-main-site.link',
        type: 'link',
        comments: undefined,
        expanded: false,
        reference: false,
        locked: false,
        error: false,
        description: 'https://www.sipoo.fi',
        configOptions: [{
          devMode: true,
        }]
      },
      {
        id: 'wilma-info.link',
        name: 'wilma-info.link',
        type: 'link',
        comments: undefined,
        expanded: false,
        reference: false,
        locked: false,
        error: false
      },
      {
        id: 'lupapiste.link',
        name: 'lupapiste.link',
        type: 'link',
        comments: undefined,
        expanded: false,
        reference: false,
        locked: false,
        error: false,
        configOptions: [{
          disabledMode: true,
        }]
      }
    ]
  },
  {
    id: 'printouts',
    name: 'printouts',
    type: 'folder',
    comments: undefined,
    expanded: false,
    reference: false,
    locked: false,
    error: false,
    children: [
      {
        id: 'sipoo-main-logo.png',
        name: 'sipoo-main-logo.png',
        type: 'image',
        comments: undefined,
        expanded: false,
        reference: false,
        locked: false,
        error: false,
        description: 'logo: black and white'
      },
      {
        id: 'sipoo-color-logo.png',
        name: 'sipoo-color-logo.png',
        type: 'image',
        comments: undefined,
        expanded: false,
        reference: false,
        locked: false,
        error: false,
        description: 'logo: full color version'
      },
      {
        id: 'municipal-seal.svg',
        name: 'municipal-seal.svg',
        type: 'image',
        comments: undefined,
        expanded: false,
        reference: false,
        locked: false,
        error: false,
        description: 'official municipal seal'
      },
      {
        id: 'new-construction-permit.printout',
        name: 'new-construction-permit.printout',
        type: 'printout',
        comments: undefined,
        expanded: false,
        reference: false,
        locked: false,
        error: false,
        children: [
          {
            id: 'fi-construction-permit',
            name: 'fi',
            type: 'template',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false,
            description: 'Finnish construction permit template'
          },
          {
            id: 'sv-construction-permit',
            name: 'sv',
            type: 'template',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false,
            description: 'Swedish construction permit template'
          }
        ]
      },
      {
        id: 'municipal-services-guide.printout',
        name: 'municipal-services-guide.printout',
        type: 'printout',
        comments: undefined,
        expanded: false,
        reference: false,
        locked: false,
        error: false,
        children: [
          {
            id: 'fi-services-guide',
            name: 'fi',
            type: 'template',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false,
            description: 'Kunnan palveluopas'
          },
          {
            id: 'sv-services-guide',
            name: 'sv',
            type: 'template',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false,
            description: 'Kommunal servicehandbok'
          },
          {
            id: 'en-services-guide',
            name: 'en',
            type: 'template',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false,
            description: 'Municipal services guide'
          }
        ]
      },
      {
        id: 'waste-management-info.printout',
        name: 'waste-management-info.printout',
        type: 'printout',
        comments: undefined,
        expanded: false,
        reference: false,
        locked: false,
        error: false,
        children: [
          {
            id: 'fi-waste-info',
            name: 'fi',
            type: 'template',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false,
            description: 'Jätehuolto-ohje'
          }
        ]
      },
      {
        id: 'tax-information-leaflet.printout',
        name: 'tax-information-leaflet.printout',
        type: 'printout',
        comments: undefined,
        expanded: false,
        reference: false,
        locked: false,
        error: false,
        children: [
          {
            id: 'fi-tax-info',
            name: 'fi',
            type: 'template',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false,
            description: 'Verotietoesite'
          },
          {
            id: 'sv-tax-info',
            name: 'sv',
            type: 'template',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false,
            description: 'Skatteinformationsbroschyr'
          },
          {
            id: 'en-tax-info',
            name: 'en',
            type: 'template',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false,
            description: 'Tax information leaflet'
          },
          {
            id: 'ee-tax-info',
            name: 'ee',
            type: 'template',
            comments: undefined,
            expanded: false,
            reference: false,
            locked: false,
            error: false,
            description: 'Maksuteabe leht'
          }
        ]
      }
    ]
  },
];