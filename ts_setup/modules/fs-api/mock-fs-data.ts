import { FsDirent } from './fs-types'

export const mockFsData: FsDirent[] = [
  {
    id: 'content',
    name: 'content',
    type: 'folder',
    children: [
      {
        id: 'index.article',
        name: 'index.article',
        type: 'article',
        children: [
          { id: 'fi-index', name: 'fi.page', type: 'page', children: [] },
          { id: 'sv-index', name: 'sv.page', type: 'page', children: [] },
          { id: 'en-index', name: 'en.page', type: 'page', children: [] },
        ]
      },
      {
        id: 'gdpr.article',
        name: 'gdpr.article',
        type: 'article',
        children: [
          {
            id: 'gdpr-child.article',
            name: 'gdpr-child.article',
            type: 'article',
            children: [
              { id: 'fi-gdpr-child', name: 'fi.page', type: 'page', children: [] },
              { id: 'sv-gdpr-child', name: 'sv.page', type: 'page', children: [] },
            ]
          },
          { id: 'fi-gdpr', name: 'fi.page', type: 'page', children: [] },
          { id: 'sv-gdpr', name: 'sv.page', type: 'page', children: [] },
        ]
      },
      {
        id: 'democracy.article',
        name: 'democracy.article',
        type: 'article',
        children: [
          { id: 'fi-democracy', name: 'fi.page', type: 'page', children: [] },
          { id: 'sv-democracy', name: 'sv.page', type: 'page', children: [] },
          { id: 'en-democracy', name: 'en.page', type: 'page', children: [] },
        ]
      },
      {
        id: 'city-living.article',
        name: 'city-living.article',
        type: 'article',
        children: [
          { id: 'fi-city-living', name: 'fi.page', type: 'page', children: [] },
          { id: 'sv-city-living', name: 'sv.page', type: 'page', children: [] },
        ]
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
    id: 'languages',
    name: 'languages',
    type: 'folder',
    children: [
      { id: 'fi.language', name: 'fi.language', type: 'language', children: [] },
      { id: 'sv.language', name: 'sv.language', type: 'language', children: [] },
      { id: 'en.language', name: 'en.language', type: 'language', children: [] },
    ],
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
        name: 'fi.template',
        type: 'template',
        children: []
      },
      {
        id: 'sv-construction-permit',
        name: 'sv.template',
        type: 'template',
        children: []
      },
      {
        id: 'en-construction-permit',
        name: 'en.template',
        type: 'template',
        children: []
      }
    ]
  }
];
