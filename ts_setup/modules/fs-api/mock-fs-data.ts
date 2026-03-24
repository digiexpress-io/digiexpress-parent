import { FsDirent } from './fs-types'

export const mockFsData: FsDirent[] = [
  {
    id: 'content',
    name: 'content',
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
            children: []
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
        children: []
      },
      {
        id: 'public-inforeq.service',
        name: 'public-inforeq.service',
        type: 'service',
        children: []
      },
      {
        id: 'wilma-preschool.service',
        name: 'wilma-preschool.service',
        type: 'service',
        children: []
      },
      {
        id: 'general-message.dialob',
        name: 'general-message.dialob',
        type: 'dialob',
        children: []
      },
      {
        id: 'public-inforeq.dialob',
        name: 'public-inforeq.dialob',
        type: 'dialob',
        children: []
      },
      {
        id: 'wilma-preschool.dialob',
        name: 'wilma-preschool.dialob',
        type: 'dialob',
        children: []
      },
      {
        id: 'index-main.phone',
        name: 'index-main.phone',
        type: 'phone',
        children: []
      },
      {
        id: 'democracy-info.phone',
        name: 'democracy-info.phone',
        type: 'phone',
        children: []
      },
      {
        id: 'water-services.phone',
        name: 'water-services.phone',
        type: 'phone',
        children: []
      }
    ]
  },
  {
    id: 'shared',
    name: 'shared',
    type: 'folder',
    children: [
      {
        id: 'taskMsgFlow.flow',
        name: 'taskMsgFlow.flow',
        type: 'flow',
        children: []
      },
      {
        id: 'taskGenericFlow.flow',
        name: 'taskGenericFlow.flow',
        type: 'flow',
        children: []
      },
      {
        id: 'taskSplitFlow.flow',
        name: 'taskSplitFlow.flow',
        type: 'flow',
        children: []
      },
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
        children: []
      },
      {
        id: 'municipal-services-guide.printout',
        name: 'municipal-services-guide.printout',
        type: 'printout',
        children: []
      },
      {
        id: 'waste-management-info.printout',
        name: 'waste-management-info.printout',
        type: 'printout',
        children: []
      },
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
      },
      {
        id: 'en-construction-permit',
        name: 'en',
        type: 'template',
        children: []
      }
    ]
  }
];
