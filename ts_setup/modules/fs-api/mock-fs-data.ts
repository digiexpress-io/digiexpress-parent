import { FsDirent } from './fs-types'

export const mockFsData: FsDirent[] = [
  {
    id: 'content',
    name: 'content',
    type: 'folder',
    children: [
      {
        id: '000_index',
        name: '000_index',
        type: 'folder',
        children: [
          {
            id: 'main.article',
            name: 'main.article',
            type: 'article',
            children: [
              {
                id: 'ref.article',
                name: 'ref.article',
                type: 'article',
                children: [
                  {
                    id: 'fi-ref',
                    name: 'fi.language',
                    type: 'language',
                    children: []
                  },
                  {
                    id: 'en-ref',
                    name: 'en.language',
                    type: 'language',
                    children: []
                  }
                ]
              },
              {
                id: 'fi-main',
                name: 'fi.language',
                type: 'language',
                children: []
              },
              {
                id: 'sv-main',
                name: 'sv.language',
                type: 'language',
                children: []
              },
              {
                id: 'en-main',
                name: 'en.language',
                type: 'language',
                children: []
              }
            ]
          },
          {
            id: 'info-gdpr.article',
            name: 'info-gdpr.article',
            type: 'article',
            children: []
          },
          {
            id: 'general-message.service',
            name: 'general-message.service',
            type: 'service',
            children: [
              {
                id: 'fi-general',
                name: 'fi.language',
                type: 'language',
                children: []
              },
              {
                id: 'sv-general',
                name: 'sv.language',
                type: 'language',
                children: []
              }
            ]
          },
          {
            id: 'general-message.dialob',
            name: 'general-message.dialob',
            type: 'dialob',
            children: []
          },
          {
            id: 'taskMsgFlow.flow',
            name: 'taskMsgFlow.flow',
            type: 'flow',
            children: []
          }
        ]
      },
      {
        id: '100_residence',
        name: '100_residence',
        type: 'folder',
        children: [
          {
            id: 'ref-article-ref1',
            name: 'ref-residence.article',
            type: 'article',
            children: []
          },
          {
            id: 'general-message-ref1',
            name: 'general-message.service',
            type: 'service',
            children: []
          },
          {
            id: 'public-inforeq.service',
            name: 'public-inforeq.service',
            type: 'service',
            children: []
          },
          {
            id: 'public-inforeq.dialob',
            name: 'public-inforeq.dialob',
            type: 'dialob',
            children: []
          },
          {
            id: 'taskGenericFlow-ref1',
            name: 'taskGenericFlow.flow',
            type: 'flow',
            children: []
          }
        ]
      },
      {
        id: '200_democracy',
        name: '200_democracy',
        type: 'folder',
        children: [
          {
            id: 'ref-article-ref2',
            name: 'ref-democracy.article',
            type: 'article',
            children: []
          },
          {
            id: 'trustee-info-form.service',
            name: 'trustee-info-form.service',
            type: 'service',
            children: [
              {
                id: 'fi-trustee',
                name: 'fi.language',
                type: 'language',
                children: []
              }
            ]
          },
          {
            id: 'trustee-info.dialob',
            name: 'trustee-info.dialob',
            type: 'dialob',
            children: []
          },
          {
            id: 'taskGenericFlow-ref2',
            name: 'taskGenericFlow.flow',
            type: 'flow',
            children: []
          },
          {
            id: 'trustee-travel-pay.service',
            name: 'trustee-travel-pay.service',
            type: 'service',
            children: []
          },
          {
            id: 'trustee-travel-pay.dialob',
            name: 'trustee-travel-pay.dialob',
            type: 'dialob',
            children: []
          },
          {
            id: 'general-message-ref2',
            name: 'general-message.service',
            type: 'service',
            children: []
          }
        ]
      },
      {
        id: '350_education',
        name: '350_education',
        type: 'folder',
        children: [
          {
            id: 'ref-article-ref3',
            name: 'ref-education.article',
            type: 'article',
            children: []
          },
          {
            id: 'wilma-preschool.service',
            name: 'wilma-preschool.service',
            type: 'service',
            children: [
              {
                id: 'fi-wilma',
                name: 'fi.language',
                type: 'language',
                children: []
              }
            ]
          },
          {
            id: 'wilma-preschool.dialob',
            name: 'wilma-preschool.dialob',
            type: 'dialob',
            children: []
          },
          {
            id: 'taskGenericFlow-ref3',
            name: 'taskGenericFlow.flow',
            type: 'flow',
            children: []
          },
          {
            id: 'protection-order-school.service',
            name: 'protection-order-school.service',
            type: 'service',
            children: []
          },
          {
            id: 'protection-order-school.dialob',
            name: 'protection-order-school.dialob',
            type: 'dialob',
            children: []
          },
          {
            id: 'general-message-ref3',
            name: 'general-message.service',
            type: 'service',
            children: []
          }
        ]
      },
      {
        id: '400_traffic-and-roads',
        name: '400_traffic-and-roads',
        type: 'folder',
        children: [
          {
            id: 'ref-article-ref4',
            name: 'ref-traffic.article',
            type: 'article',
            children: []
          },
          {
            id: 'dig-permit.service',
            name: 'dig-permit.service',
            type: 'service',
            children: []
          },
          {
            id: 'dig-permit.dialob',
            name: 'dig-permit.dialob',
            type: 'dialob',
            children: []
          },
          {
            id: 'taskSplitFlow.flow',
            name: 'taskSplitFlow.flow',
            type: 'flow',
            children: []
          },
          {
            id: 'rent-street-area.service',
            name: 'rent-street-area.service',
            type: 'service',
            children: []
          },
          {
            id: 'rent-street-area.dialob',
            name: 'rent-street-area.dialob',
            type: 'dialob',
            children: []
          },
          {
            id: 'private-road-sign.service',
            name: 'private-road-sign.service',
            type: 'service',
            children: []
          },
          {
            id: 'private-road-sign.dialob',
            name: 'private-road-sign.dialob',
            type: 'dialob',
            children: []
          },
          {
            id: 'general-message-ref4',
            name: 'general-message.service',
            type: 'service',
            children: []
          }
        ]
      },
      {
        id: '425_invoicing',
        name: '425_invoicing',
        type: 'folder',
        children: [
          {
            id: 'main-invoicing.article',
            name: 'main-invoicing.article',
            type: 'article',
            children: []
          },
          {
            id: 'invoicing-erapaiva.service',
            name: 'invoicing-erapaiva.service',
            type: 'service',
            children: []
          },
          {
            id: 'invoicing-erapaiva.dialob',
            name: 'invoicing-erapaiva.dialob',
            type: 'dialob',
            children: []
          },
          {
            id: 'taskGenericFlow-ref4',
            name: 'taskGenericFlow.flow',
            type: 'flow',
            children: []
          },
          {
            id: 'invoice-copy-request.service',
            name: 'invoice-copy-request.service',
            type: 'service',
            children: []
          },
          {
            id: 'invoice-copy-request.dialob',
            name: 'invoice-copy-request.dialob',
            type: 'dialob',
            children: []
          },
          {
            id: 'bank-account-info.service',
            name: 'bank-account-info.service',
            type: 'service',
            children: []
          },
          {
            id: 'bank-account-info.dialob',
            name: 'bank-account-info.dialob',
            type: 'dialob',
            children: []
          }
        ]
      },
      {
        id: '517_sipoo-institute',
        name: '517_sipoo-institute',
        type: 'folder',
        children: [
          {
            id: 'main-institute.article',
            name: 'main-institute.article',
            type: 'article',
            children: []
          },
          {
            id: 'teacher-info.service',
            name: 'teacher-info.service',
            type: 'service',
            children: []
          },
          {
            id: 'teacher-info.dialob',
            name: 'teacher-info.dialob',
            type: 'dialob',
            children: []
          },
          {
            id: 'taskGenericFlow-ref5',
            name: 'taskGenericFlow.flow',
            type: 'flow',
            children: []
          },
          {
            id: 'teacher-travel-pay.service',
            name: 'teacher-travel-pay.service',
            type: 'service',
            children: []
          },
          {
            id: 'teacher-travel-pay.dialob',
            name: 'teacher-travel-pay.dialob',
            type: 'dialob',
            children: []
          },
          {
            id: 'study-voucher.service',
            name: 'study-voucher.service',
            type: 'service',
            children: []
          },
          {
            id: 'study-voucher.dialob',
            name: 'study-voucher.dialob',
            type: 'dialob',
            children: []
          },
          {
            id: 'general-message-ref5',
            name: 'general-message.service',
            type: 'service',
            children: []
          }
        ]
      },
      {
        id: '520_sipoo-water',
        name: '520_sipoo-water',
        type: 'folder',
        children: [
          {
            id: 'main-water.article',
            name: 'main-water.article',
            type: 'article',
            children: []
          },
          {
            id: 'water-supply-maintenance.service',
            name: 'water-supply-maintenance.service',
            type: 'service',
            children: []
          },
          {
            id: 'water-supply-maintenance.dialob',
            name: 'water-supply-maintenance.dialob',
            type: 'dialob',
            children: []
          },
          {
            id: 'taskGenericFlow-ref6',
            name: 'taskGenericFlow.flow',
            type: 'flow',
            children: []
          },
          {
            id: 'water-invoice-erapaiva.service',
            name: 'water-invoice-erapaiva.service',
            type: 'service',
            children: []
          },
          {
            id: 'water-invoice-erapaiva.dialob',
            name: 'water-invoice-erapaiva.dialob',
            type: 'dialob',
            children: []
          },
          {
            id: 'water-connection-statement.service',
            name: 'water-connection-statement.service',
            type: 'service',
            children: []
          },
          {
            id: 'water-connection-statement.dialob',
            name: 'water-connection-statement.dialob',
            type: 'dialob',
            children: []
          },
          {
            id: 'general-message-ref6',
            name: 'general-message.service',
            type: 'service',
            children: []
          }
        ]
      },
      {
        id: '650_leisure-time-and-youth',
        name: '650_leisure-time-and-youth',
        type: 'folder',
        children: [
          {
            id: 'main-leisure.article',
            name: 'main-leisure.article',
            type: 'article',
            children: []
          },
          {
            id: 'sports-grant-settlement.service',
            name: 'sports-grant-settlement.service',
            type: 'service',
            children: []
          },
          {
            id: 'sports-grant-settlement.dialob',
            name: 'sports-grant-settlement.dialob',
            type: 'dialob',
            children: []
          },
          {
            id: 'taskGenericFlow-ref7',
            name: 'taskGenericFlow.flow',
            type: 'flow',
            children: []
          },
          {
            id: 'sports-fee-return.service',
            name: 'sports-fee-return.service',
            type: 'service',
            children: []
          },
          {
            id: 'sports-fee-return.dialob',
            name: 'sports-fee-return.dialob',
            type: 'dialob',
            children: []
          },
          {
            id: 'children-sport-grant.service',
            name: 'children-sport-grant.service',
            type: 'service',
            children: []
          },
          {
            id: 'children-sport-grant.dialob',
            name: 'children-sport-grant.dialob',
            type: 'dialob',
            children: []
          }
        ]
      }
    ]
  },
  {
    id: 'shared',
    name: 'shared',
    type: 'folder',
    children: [
      {
        id: 'shared-taskGenericFlow.flow',
        name: 'taskGenericFlow.flow',
        type: 'flow',
        children: []
      },
      {
        id: 'shared-taskMsgFlow.flow',
        name: 'taskMsgFlow.flow',
        type: 'flow',
        children: []
      },
      {
        id: 'shared-taskSplitFlow.flow',
        name: 'taskSplitFlow.flow',
        type: 'flow',
        children: []
      }
    ]
  },
  {
    id: 'links',
    name: 'links',
    type: 'folder',
    children: [
      {
        id: 'sipoo-main-site.link',
        name: 'sipoo-main-site.link',
        type: 'link',
        children: []
      },
      {
        id: 'wilma-info.link',
        name: 'wilma-info.link',
        type: 'link',
        children: []
      },
      {
        id: 'lupapiste.link',
        name: 'lupapiste.link',
        type: 'link',
        children: []
      }
    ]
  },
  {
    id: 'printouts',
    name: 'printouts',
    type: 'folder',
    children: [
      {
        id: 'sipoo-main-logo.png',
        name: 'sipoo-main-logo.png',
        type: 'image',
        children: []
      },
      {
        id: 'sipoo-color-logo.png',
        name: 'sipoo-color-logo.png',
        type: 'image',
        children: []
      },
      {
        id: 'municipal-seal.svg',
        name: 'municipal-seal.svg',
        type: 'image',
        children: []
      },
      {
        id: 'new-construction-permit.printout',
        name: 'new-construction-permit.printout',
        type: 'printout',
        children: [
          {
            id: 'fi-construction-permit',
            name: 'fi',
            type: 'template',
            children: []
          },
          {
            id: 'sv-construction-permit',
            name: 'sv',
            type: 'template',
            children: []
          }
        ]
      },
      {
        id: 'municipal-services-guide.printout',
        name: 'municipal-services-guide.printout',
        type: 'printout',
        children: [
          {
            id: 'fi-services-guide',
            name: 'fi',
            type: 'template',
            children: []
          },
          {
            id: 'sv-services-guide',
            name: 'sv',
            type: 'template',
            children: []
          },
          {
            id: 'en-services-guide',
            name: 'en',
            type: 'template',
            children: []
          }
        ]
      },
      {
        id: 'waste-management-info.printout',
        name: 'waste-management-info.printout',
        type: 'printout',
        children: [
          {
            id: 'fi-waste-info',
            name: 'fi',
            type: 'template',
            children: []
          }
        ]
      },
      {
        id: 'tax-information-leaflet.printout',
        name: 'tax-information-leaflet.printout',
        type: 'printout',
        children: [
          {
            id: 'fi-tax-info',
            name: 'fi',
            type: 'template',
            children: []
          },
          {
            id: 'sv-tax-info',
            name: 'sv',
            type: 'template',
            children: []
          },
          {
            id: 'en-tax-info',
            name: 'en',
            type: 'template',
            children: []
          },
          {
            id: 'ee-tax-info',
            name: 'ee',
            type: 'template',
            children: []
          }
        ]
      }
    ]
  }
];
