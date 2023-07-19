
import * as PropEditors from '../components/propEditors';
import GenericOptionEditor from '../components/options/GenericOptionEditor';

export const DEFAULT_ITEMTYPE_CONFIG = {
  categories: [
    {
      title: 'Structure',
      type: 'structure',
      items: [
        {
          title: 'Group',
          optionEditors: [
            {name: 'Additional option', editor: GenericOptionEditor}
          ],
          propEditors: {
            columns: {
              component: PropEditors.InputProp,
              props: {
                type: 'number',
                min: 1,
                max: 20
              }
            }
          },
          config: {
            type: 'group',
            props: {
              columns: 1
            }
          }
        },
        {
          title: 'Survey group',
          convertible: ['verticalSurveygroup'],
          config: {
            type: 'surveygroup'
          }
        },
        {
          title: 'Survey group (vertical)',
          convertible: ['surveygroup'],
          config: {
            type: 'surveygroup',
            view: 'verticalSurveygroup',
          }
        },
        {
          title: 'Multi-Row',
          config: {
            type: 'rowgroup'
          }
        }
      ]
    },
    {
      title: 'Inputs',
      type: 'input',
      items: [
        {
          title: 'Survey item',
          config: {
            type: 'survey',
            view: 'survey'
          }
        },
        {
          title: 'Text',
          convertible: ['textBox', 'address'],
          config: {
            type: 'text',
            view: 'text'
          }
        },
        {
          title: 'Text box',
          convertible: ['text', 'address'],
          config: {
            type: 'text',
            view: 'textBox'
          }
        },
        {
          title: 'Address',
          convertible: ['text', 'textBox'],
          propEditors: {
            country: {
            component: PropEditors.MultiChoiceProp,
            props: {
              allowAdditions: true,
              options: [
                {key: 'fi', label: 'Finland'},
                {key: 'sv', label: 'Sweden'},
                {key: 'ee', label: 'Estonia'}
              ]
            }
          }
          },
          config: {
            type: 'text',
            view: 'address',
            props: {
              country: []
            }
          }
        },
        {
          title: 'Decimal',
          convertible: ['number'],
          config: {
            type: 'decimal'
          }
        },
        {
          title: 'Integer',
          convertible: ['decimal'],
          config: {
            type: 'number'
          }
        },
        {
          title: 'Boolean',
          config: {
            type: 'boolean'
          }
        },
        {
          title: 'Date',
          config: {
            type: 'date'
          }
        },
        {
          title: 'Time',
          config: {
            type: 'time'
          }
        },
        {
          title: 'Choice',
          convertible: ['multichoice'],
          config: {
            type: 'list'
          }
        },
        {
          title: 'Multi-choice',
          convertible: ['list'],
          config: {
            type: 'multichoice'
          }
        }
      ]
    },
    {
      title: 'Outputs',
      type: 'output',
      items: [
        {
          title: 'Note',
          config: {
            type: 'note'
          }
        }
      ]
    }
  ]
};