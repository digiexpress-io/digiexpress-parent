import { TreeNode } from '../eveli-tree-api'


export const mockTreeData: TreeNode[] = [
  {
    id: 'content',
    name: 'content',
    type: 'folder',
    isExpanded: true,
    children: [
      {
        id: '000_index',
        name: '000_index',
        type: 'folder',
        isExpanded: true,
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
            isExpanded: true,
            children: [
              {
                id: 'fi-main',
                name: 'fi',
                type: 'language',
                description: 'Tervetuloa Sipoon Oma asiointiin!'
              },
              {
                id: 'sv-main',
                name: 'sv',
                type: 'language'
              },
              {
                id: 'en-main',
                name: 'en',
                type: 'language'
              }
            ]
          },
          {
            id: 'info-gdpr.article',
            name: 'info-gdpr.article',
            type: 'article'
          },
          {
            id: 'general-message.service',
            name: 'general-message.service',
            type: 'service',
            isExpanded: true,
            children: [
              {
                id: 'fi-general',
                name: 'fi',
                type: 'language',
                description: 'Lähetä viesti'
              },
              {
                id: 'sv-general',
                name: 'sv',
                type: 'language'
              }
            ]
          },
          {
            id: 'general-message.dialob',
            name: 'general-message.dialob',
            type: 'dialob'
          },
          {
            id: 'taskMsgFlow.flow',
            name: 'taskMsgFlow.flow',
            type: 'flow'
          }
        ]
      },
      {
        id: '100_residence',
        name: '100_residence',
        type: 'folder',
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
            id: 'main-residence.article',
            name: 'main.article',
            type: 'article',
            description: 'Asuminen'
          },
          {
            id: 'general-message-ref1',
            name: 'general-message.service',
            type: 'service',
            isReference: true
          },
          {
            id: 'public-inforeq.service',
            name: 'public-inforeq.service',
            type: 'service'
          },
          {
            id: 'public-inforeq.dialob',
            name: 'public-inforeq.dialob',
            type: 'dialob'
          },
          {
            id: 'taskGenericFlow-ref1',
            name: 'taskGenericFlow.flow',
            type: 'flow',
            isReference: true
          }
        ]
      },
      {
        id: '200_democracy',
        name: '200_democracy',
        type: 'folder',
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
            id: 'main-democracy.article',
            name: 'main.article',
            type: 'article',
            description: 'Demokratia'
          },
          {
            id: 'trustee-info-form.service',
            name: 'trustee-info-form.service',
            type: 'service',
            isExpanded: true,
            children: [
              {
                id: 'fi-trustee',
                name: 'fi',
                type: 'language',
                description: 'Luottamushenkilön tietolomake'
              }
            ]
          },
          {
            id: 'trustee-info.dialob',
            name: 'trustee-info.dialob',
            type: 'dialob'
          },
          {
            id: 'taskGenericFlow-ref2',
            name: 'taskGenericFlow.flow',
            type: 'flow',
            isReference: true
          },
          {
            id: 'trustee-travel-pay.service',
            name: 'trustee-travel-pay.service',
            type: 'service'
          },
          {
            id: 'trustee-travel-pay.dialob',
            name: 'trustee-travel-pay.dialob',
            type: 'dialob'
          },
          {
            id: 'general-message-ref2',
            name: 'general-message.service',
            type: 'service',
            isReference: true
          }
        ]
      },
      {
        id: '350_education',
        name: '350_education',
        type: 'folder',
        children: [
          {
            id: 'main-education.article',
            name: 'main.article',
            type: 'article',
            description: 'Koulutus'
          },
          {
            id: 'wilma-preschool.service',
            name: 'wilma-preschool.service',
            type: 'service',
            isExpanded: true,
            children: [
              {
                id: 'fi-wilma',
                name: 'fi',
                type: 'language',
                description: 'Wilmatunnukset esiopetuslapsen huoltajalle'
              }
            ]
          },
          {
            id: 'wilma-preschool.dialob',
            name: 'wilma-preschool.dialob',
            type: 'dialob'
          },
          {
            id: 'taskGenericFlow-ref3',
            name: 'taskGenericFlow.flow',
            type: 'flow',
            isReference: true
          },
          {
            id: 'protection-order-school.service',
            name: 'protection-order-school.service',
            type: 'service'
          },
          {
            id: 'protection-order-school.dialob',
            name: 'protection-order-school.dialob',
            type: 'dialob'
          },
          {
            id: 'general-message-ref3',
            name: 'general-message.service',
            type: 'service',
            isReference: true
          }
        ]
      },
      {
        id: '400_traffic-and-roads',
        name: '400_traffic-and-roads',
        type: 'folder',
        children: [
          {
            id: 'main-traffic.article',
            name: 'main.article',
            type: 'article',
            description: 'Kadut ja viheralueet'
          },
          {
            id: 'dig-permit.service',
            name: 'dig-permit.service',
            type: 'service'
          },
          {
            id: 'dig-permit.dialob',
            name: 'dig-permit.dialob',
            type: 'dialob'
          },
          {
            id: 'taskSplitFlow.flow',
            name: 'taskSplitFlow.flow',
            type: 'flow'
          },
          {
            id: 'rent-street-area.service',
            name: 'rent-street-area.service',
            type: 'service'
          },
          {
            id: 'rent-street-area.dialob',
            name: 'rent-street-area.dialob',
            type: 'dialob'
          },
          {
            id: 'private-road-sign.service',
            name: 'private-road-sign.service',
            type: 'service'
          },
          {
            id: 'private-road-sign.dialob',
            name: 'private-road-sign.dialob',
            type: 'dialob'
          },
          {
            id: 'general-message-ref4',
            name: 'general-message.service',
            type: 'service',
            isReference: true
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
            name: 'main.article',
            type: 'article',
            description: 'Laskutus'
          },
          {
            id: 'invoicing-erapaiva.service',
            name: 'invoicing-erapaiva.service',
            type: 'service'
          },
          {
            id: 'invoicing-erapaiva.dialob',
            name: 'invoicing-erapaiva.dialob',
            type: 'dialob'
          },
          {
            id: 'taskGenericFlow-ref4',
            name: 'taskGenericFlow.flow',
            type: 'flow',
            isReference: true
          },
          {
            id: 'invoice-copy-request.service',
            name: 'invoice-copy-request.service',
            type: 'service'
          },
          {
            id: 'invoice-copy-request.dialob',
            name: 'invoice-copy-request.dialob',
            type: 'dialob'
          },
          {
            id: 'bank-account-info.service',
            name: 'bank-account-info.service',
            type: 'service'
          },
          {
            id: 'bank-account-info.dialob',
            name: 'bank-account-info.dialob',
            type: 'dialob'
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
            name: 'main.article',
            type: 'article',
            description: 'Sipoon opisto'
          },
          {
            id: 'teacher-info.service',
            name: 'teacher-info.service',
            type: 'service'
          },
          {
            id: 'teacher-info.dialob',
            name: 'teacher-info.dialob',
            type: 'dialob'
          },
          {
            id: 'taskGenericFlow-ref5',
            name: 'taskGenericFlow.flow',
            type: 'flow',
            isReference: true
          },
          {
            id: 'teacher-travel-pay.service',
            name: 'teacher-travel-pay.service',
            type: 'service'
          },
          {
            id: 'teacher-travel-pay.dialob',
            name: 'teacher-travel-pay.dialob',
            type: 'dialob'
          },
          {
            id: 'study-voucher.service',
            name: 'study-voucher.service',
            type: 'service'
          },
          {
            id: 'study-voucher.dialob',
            name: 'study-voucher.dialob',
            type: 'dialob'
          },
          {
            id: 'general-message-ref5',
            name: 'general-message.service',
            type: 'service',
            isReference: true
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
            name: 'main.article',
            type: 'article',
            description: 'Sipoon Vesi'
          },
          {
            id: 'water-supply-maintenance.service',
            name: 'water-supply-maintenance.service',
            type: 'service'
          },
          {
            id: 'water-supply-maintenance.dialob',
            name: 'water-supply-maintenance.dialob',
            type: 'dialob'
          },
          {
            id: 'taskGenericFlow-ref6',
            name: 'taskGenericFlow.flow',
            type: 'flow',
            isReference: true
          },
          {
            id: 'water-invoice-erapaiva.service',
            name: 'water-invoice-erapaiva.service',
            type: 'service'
          },
          {
            id: 'water-invoice-erapaiva.dialob',
            name: 'water-invoice-erapaiva.dialob',
            type: 'dialob'
          },
          {
            id: 'water-connection-statement.service',
            name: 'water-connection-statement.service',
            type: 'service'
          },
          {
            id: 'water-connection-statement.dialob',
            name: 'water-connection-statement.dialob',
            type: 'dialob'
          },
          {
            id: 'general-message-ref6',
            name: 'general-message.service',
            type: 'service',
            isReference: true
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
            name: 'main.article',
            type: 'article',
            description: 'Vapaa-aika ja nuoret'
          },
          {
            id: 'sports-grant-settlement.service',
            name: 'sports-grant-settlement.service',
            type: 'service'
          },
          {
            id: 'sports-grant-settlement.dialob',
            name: 'sports-grant-settlement.dialob',
            type: 'dialob'
          },
          {
            id: 'taskGenericFlow-ref7',
            name: 'taskGenericFlow.flow',
            type: 'flow',
            isReference: true
          },
          {
            id: 'sports-fee-return.service',
            name: 'sports-fee-return.service',
            type: 'service'
          },
          {
            id: 'sports-fee-return.dialob',
            name: 'sports-fee-return.dialob',
            type: 'dialob'
          },
          {
            id: 'children-sport-grant.service',
            name: 'children-sport-grant.service',
            type: 'service'
          },
          {
            id: 'children-sport-grant.dialob',
            name: 'children-sport-grant.dialob',
            type: 'dialob'
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
        type: 'flow'
      },
      {
        id: 'shared-taskMsgFlow.flow',
        name: 'taskMsgFlow.flow',
        type: 'flow'
      },
      {
        id: 'shared-taskSplitFlow.flow',
        name: 'taskSplitFlow.flow',
        type: 'flow'
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
        description: 'https://www.sipoo.fi'
      },
      {
        id: 'wilma-info.link',
        name: 'wilma-info.link',
        type: 'link'
      },
      {
        id: 'lupapiste.link',
        name: 'lupapiste.link',
        type: 'link'
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
        description: 'logo: black and white'
      },
      {
        id: 'sipoo-color-logo.png',
        name: 'sipoo-color-logo.png',
        type: 'image',
        description: 'logo: full color version'
      },
      {
        id: 'municipal-seal.svg',
        name: 'municipal-seal.svg',
        type: 'image',
        description: 'official municipal seal'
      },
      {
        id: 'new-construction-permit.printout',
        name: 'new-construction-permit.printout',
        type: 'printout',
        isExpanded: false,
        children: [
          {
            id: 'fi-construction-permit',
            name: 'fi',
            type: 'template',
            description: 'Finnish construction permit template'
          },
           {
            id: 'sv-construction-permit',
            name: 'sv',
            type: 'template',
            description: 'Swedish construction permit template'
          }
        ]
      },
      {
        id: 'municipal-services-guide.printout',
        name: 'municipal-services-guide.printout',
        type: 'printout',
        isExpanded: false,
        children: [
          {
            id: 'fi-services-guide',
            name: 'fi',
            type: 'template',
            description: 'Kunnan palveluopas'
          },
          {
            id: 'sv-services-guide',
            name: 'sv',
            type: 'template',
            description: 'Kommunal servicehandbok'
          },
          {
            id: 'en-services-guide',
            name: 'en',
            type: 'template',
            description: 'Municipal services guide'
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
            description: 'Jätehuolto-ohje'
          }
        ]
      },
      {
        id: 'tax-information-leaflet.printout',
        name: 'tax-information-leaflet.printout',
        type: 'printout',
        isExpanded: false,
        children: [
          {
            id: 'fi-tax-info',
            name: 'fi',
            type: 'template',
            description: 'Verotietoesite'
          },
          {
            id: 'sv-tax-info',
            name: 'sv',
            type: 'template',
            description: 'Skatteinformationsbroschyr'
          },
          {
            id: 'en-tax-info',
            name: 'en',
            type: 'template',
            description: 'Tax information leaflet'
          },
          {
            id: 'ee-tax-info',
            name: 'ee',
            type: 'template',
            description: 'Maksuteabe leht'
          }
        ]
      }
    ]
  },
];