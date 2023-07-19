import {Group, SimpleField, Note} from '../items';

export const DEFAULT_ITEM_CONFIG = {
  items: [
    {
      matcher: item => item.get('type') === 'group',
      component: Group,
      props: {
        icon: 'square outline',
        placeholder: 'Group label',
        treeCollapsible: true,
      }
    },
    {
      matcher: item => item.get('type') === 'surveygroup',
      component: Group,
      props: {
        icon: 'braille',
        placeholder: 'Survey group label',
        treeCollapsible: true
      }
    },
    {
      matcher: item => item.get('type') === 'rowgroup',
      component: Group,
      props: {
        icon: 'table',
        placeholder: 'Multi-row group label',
        treeCollapsible: true
      }
    },
    {
      matcher: item => item.get('type') === 'survey',
      component: SimpleField,
      props: {
        icon: 'ellipsis horizontal',
        placeholder: 'Survey field label'
      }
    },
    {
      matcher: item => item.get('view') === 'address',
      component: SimpleField,
      props: {
        icon: 'map marker alternate',
        placeholder: 'Address field label'
      }
    },
    {
      matcher: item => item.get('type') === 'text',
      component: SimpleField,
      props: {
        icon: 'font',
        placeholder: 'Text field label'
      }
    },
    {
      matcher: item => item.get('type') === 'time',
      component: SimpleField,
      props: {
        icon: 'time',
        placeholder: 'Time field label'
      }
    },
    {
      matcher: item => item.get('type') === 'date',
      component: SimpleField,
      props: {
        icon: 'calendar',
        placeholder: 'Date field label'
      }
    },
    {
      matcher: item => item.get('type') === 'number',
      component: SimpleField,
      props: {
        icon: 'hashtag',
        placeholder: 'Number field label'
      }
    },
    {
      matcher: item => item.get('type') === 'decimal',
      component: SimpleField,
      props: {
        icon: 'currency',
        placeholder: 'Decimal field label'
      }
    },
    {
      matcher: item => item.get('type') === 'boolean',
      component: SimpleField,
      props: {
        icon: 'checkmark box',
        placeholder: 'Boolean field label'
      }
    },
    {
      matcher: item => item.get('type') === 'list',
      component: SimpleField,
      props: {
        icon: 'angle down',
        placeholder: 'List field label'
      }
    },
    {
      matcher: item => item.get('type') === 'multichoice',
      component: SimpleField,
      props: {
        icon: 'list',
        placeholder: 'Multi-choice field label'
      }
    },
    {
      matcher: item => item.get('type') === 'note',
      component: Note,
      props: {
        icon: 'file outline',
        placeholder: 'List field label'
      }
    }
  ]
};

