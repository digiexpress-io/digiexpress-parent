// Mock data structure for EveliTree component
export interface TreeNode {
  id: string;
  name: string;
  type: 'folder' | 'article' | 'service' | 'form' | 'flow' | 'link' | 'language';
  description?: string;
  children?: TreeNode[];
  isExpanded?: boolean;
  isReference?: boolean;
}

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
            id: 'general-message.form',
            name: 'general-message.form',
            type: 'form'
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
            id: 'public-inforeq.form',
            name: 'public-inforeq.form',
            type: 'form'
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
            id: 'trustee-info.form',
            name: 'trustee-info.form',
            type: 'form'
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
            id: 'trustee-travel-pay.form',
            name: 'trustee-travel-pay.form',
            type: 'form'
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
            id: 'wilma-preschool.form',
            name: 'wilma-preschool.form',
            type: 'form'
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
            id: 'protection-order-school.form',
            name: 'protection-order-school.form',
            type: 'form'
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
            id: 'dig-permit.form',
            name: 'dig-permit.form',
            type: 'form'
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
            id: 'rent-street-area.form',
            name: 'rent-street-area.form',
            type: 'form'
          },
          {
            id: 'private-road-sign.service',
            name: 'private-road-sign.service',
            type: 'service'
          },
          {
            id: 'private-road-sign.form',
            name: 'private-road-sign.form',
            type: 'form'
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
            id: 'invoicing-erapaiva.form',
            name: 'invoicing-erapaiva.form',
            type: 'form'
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
            id: 'invoice-copy-request.form',
            name: 'invoice-copy-request.form',
            type: 'form'
          },
          {
            id: 'bank-account-info.service',
            name: 'bank-account-info.service',
            type: 'service'
          },
          {
            id: 'bank-account-info.form',
            name: 'bank-account-info.form',
            type: 'form'
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
            id: 'teacher-info.form',
            name: 'teacher-info.form',
            type: 'form'
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
            id: 'teacher-travel-pay.form',
            name: 'teacher-travel-pay.form',
            type: 'form'
          },
          {
            id: 'study-voucher.service',
            name: 'study-voucher.service',
            type: 'service'
          },
          {
            id: 'study-voucher.form',
            name: 'study-voucher.form',
            type: 'form'
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
            id: 'water-supply-maintenance.form',
            name: 'water-supply-maintenance.form',
            type: 'form'
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
            id: 'water-invoice-erapaiva.form',
            name: 'water-invoice-erapaiva.form',
            type: 'form'
          },
          {
            id: 'water-connection-statement.service',
            name: 'water-connection-statement.service',
            type: 'service'
          },
          {
            id: 'water-connection-statement.form',
            name: 'water-connection-statement.form',
            type: 'form'
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
            id: 'sports-grant-settlement.form',
            name: 'sports-grant-settlement.form',
            type: 'form'
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
            id: 'sports-fee-return.form',
            name: 'sports-fee-return.form',
            type: 'form'
          },
          {
            id: 'children-sport-grant.service',
            name: 'children-sport-grant.service',
            type: 'service'
          },
          {
            id: 'children-sport-grant.form',
            name: 'children-sport-grant.form',
            type: 'form'
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
  }
];