
import {
  GFormReviewPage, GFormReviewGroup, GFormReviewDateItem,
  GFormReviewTime, GFormReviewText, GFormReviewBoolean, GFormReviewChoice,
  GFormReviewMultiChoice, GFormReviewNote, GFormReviewDecimal, GFormReviewSurveyGroup, GFormReviewRowGroup
} from './';
import { ItemconfigType } from './GFormReviewContext';


export const DEFAULT_ITEM_CONFIG: ItemconfigType = {
  items: [
    {
      matcher: (_item, isMainGFormReviewGroupItem) => isMainGFormReviewGroupItem,
      component: GFormReviewPage,
      answerRequired: false,
      childrenRequired: true
    },
    {
      matcher: item => item.type === 'group' && item.view === 'page',
      component: GFormReviewPage,
      answerRequired: false,
      childrenRequired: true
    },
    {
      matcher: item => item.type === 'group',
      component: GFormReviewGroup,
      answerRequired: false,
      childrenRequired: true
    },
    {
      matcher: item => item.type === 'surveygroup',
      component: GFormReviewSurveyGroup,
      answerRequired: false,
      childrenRequired: false
    },
    {
      matcher: item => item.type === 'rowgroup',
      component: GFormReviewRowGroup,
      answerRequired: false,
      childrenRequired: false
    },
    {
      matcher: item => item.type === 'boolean',
      component: GFormReviewBoolean,
      answerRequired: true,
      childrenRequired: false
    },
    {
      matcher: item => item.type === 'text',
      component: GFormReviewText,
      answerRequired: true,
      childrenRequired: false
    },
    {
      matcher: item => item.type === 'list',
      component: GFormReviewChoice,
      answerRequired: true,
      childrenRequired: false
    },
    {
      matcher: item => item.type === 'multichoice',
      component: GFormReviewMultiChoice,
      answerRequired: true,
      childrenRequired: false
    },
    /*
    {
      matcher: item => item.type === 'survey', // Survey is handled within survey GFormReviewGroup
      component: SurveyItem,
      answerRequired: true,
      childrenRequired: false
    },
    */
    {
      matcher: item => item.type === 'note',
      component: GFormReviewNote,
      answerRequired: false,
      childrenRequired: false
    },
    {
      matcher: item => item.type === 'date',
      component: GFormReviewDateItem,
      answerRequired: true,
      childrenRequired: false
    },
    {
      matcher: item => item.type === 'time',
      component: GFormReviewTime,
      answerRequired: true,
      childrenRequired: false
    },
    {
      matcher: item => item.type === 'number' || item.type === 'decimal',
      component: GFormReviewDecimal,
      answerRequired: true,
      childrenRequired: false
    }
  ]
};
