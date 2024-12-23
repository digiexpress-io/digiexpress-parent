import { CSSInterpolation, CSSObject, Interpolation, Theme } from '@mui/material';
import { OverridesStyleRules } from '@mui/material/styles/overrides';

import { GLogoClassKey, GLogoProps } from '../g-logo';

import { GAppBarClassKey, GAppBarProps } from '../g-app-bar';
import { GArticleClassKey, GArticleProps } from '../g-article';
import { GArticleFeedbackClassKey, GArticleFeedbackProps } from '../g-article-feedback';


import { GPopoverButtonClassKey, GPopoverButtonProps } from '../g-popover-button';
import { GPopoverTopicsClassKey, GPopoverTopicsProps } from '../g-popover-topics';
import { GPopoverSearchClassKey, GPopoverSearchProps } from '../g-popover-search';

import { GConfirmClassKey, GConfirmProps } from '../g-confirm';
import { GLoaderClassKey, GLoaderProps } from '../g-loader';

import { GServicesClassKey, GServicesProps } from '../g-services';
import { GServicesSearchClassKey, GServicesSearchProps } from '../g-services-search';

import { GTooltipClassKey, GTooltipProps } from '../g-tooltip';

import { GInboxClassKey, GInboxProps } from '../g-inbox';
import { GInboxAttachmentsClassKey, GInboxAttachmentsProps } from '../g-inbox-attachments';
import { GInboxMessageNotAllowed, GInboxMessageNotAllowedProps } from '../g-inbox-messages';
import { GInboxMessagesClassKey, GInboxMessagesProps } from '../g-inbox-messages';
import { GInboxFormReviewClassKey, GInboxFormReviewProps } from '../g-inbox-form-review';

import { GLocalesClassKey, GLocalesProps } from '../g-locales';
import { GShellClassKey, GShellProps } from '../g-shell';
import { GLoginClassKey, GLoginProps } from '../g-login';
import { GLogoutClassKey, GLogoutProps } from '../g-logout';

import { GSearchListClassKey, GSearchListProps } from '../g-search-list';
import { GSearchListItemClassKey, GSearchListItemProps } from '../g-search-list-item';
import { GMarkdownClassKey, GMarkdownProps } from '../g-md';
import { GLayoutClassKey, GLayoutProps } from '../g-layout';
import { GFormBaseClassKey, GFormBaseProps } from '../g-form-base';
import { GFooterClassKey, GFooterProps } from '../g-footer';
import { GUserOverviewMenuClassKey, GUserOverviewMenuProps } from '../g-user-overview-menu';
import {
  GUserOverviewDetailClassKey, GUserOverviewDetailProps,
  GUserOverviewClassKey, GUserOverviewProps,
} from '../g-user-overview';

import { GContractsClassKey, GContractsProps, GContractItemProps, } from '../g-contracts';
import { GBookingsClassKey, GBookingsProps } from '../g-bookings';


import { GOffersProps, GOffersClassKey } from '../g-offers';

import { GLinksClassKey, GLinksProps } from '../g-links';
import { GLinksPageClassKey, GLinksPageProps } from '../g-links-page';


import {
  GLinkInfoClassKey, GLinkInfoProps,
  GLinkFormSecuredClassKey, GLinkFormSecuredProps,
  GLinkFormUnsecuredClassKey, GLinkFormUnsecuredProps,
  GLinkHyperClassKey, GLinkHyperProps,
  GLinkPhoneClassKey, GLinkPhoneProps
} from '../g-link';

import { GFormClassKey, GFormProps } from '../g-form';
import { GAuthClassKey, GAuthProps } from '../g-auth';
import { GAuthUnClassKey, GAuthUnProps } from '../g-auth-un';
import { GAuthUnRepCompanyProps, GAuthUnRepCompanyClassKey } from '../g-auth-un-rep-company';
import { GAuthUnRepPersonProps, GAuthUnRepPersonClassKey } from '../g-auth-un-rep-person';
import { GAuthRepCompanyProps, GAuthRepCompanyClassKey } from '../g-auth-rep-company';
import { GAuthRepPersonProps, GAuthRepPersonClassKey } from '../g-auth-rep-person';

import type { ItemProps, GroupItemProps, PageItemProps, QuestionnaireItemProps, SurveyProps } from '../g-form-review-components';
import {
  GFormReviewBooleanClassKey,
  GFormReviewChoiceClassKey,
  GFormReviewDateClassKey,
  GFormReviewDecimalClassKey,
  GFormReviewGroupClassKey,
  GFormReviewItemClassKey,
  GFormReviewMultiChoiceClassKey,
  GFormReviewNoteClassKey,
  GFormReviewPageClassKey,
  GFormReviewQuestionnaireClassKey,
  GFormReviewRowGroupClassKey,
  GFormReviewSurveyClassKey,
  GFormReviewSurveyGroupClassKey,
  GFormReviewTextClassKey,
  GFormReviewTimeClassKey
} from '../g-form-review-components';



declare module "@mui/material" {
  export interface Components<Theme = unknown> extends GComponents<Theme> { }
}



/**
 * MUI theme integration
 */
export interface GComponentsPropsList {
  GFormReviewBoolean: ItemProps;
  GFormReviewChoice: ItemProps;
  GFormReviewDate: ItemProps;
  GFormReviewDecimal: ItemProps;
  GFormReviewGroup: GroupItemProps;
  GFormReviewItem: ItemProps;
  GFormReviewNote: ItemProps;
  GFormReviewPage: PageItemProps;
  GFormReviewQuestionnaire: QuestionnaireItemProps;
  GFormReviewRowGroup: ItemProps;
  GFormReviewSurvey: SurveyProps;
  GFormReviewSurveyGroup: ItemProps;
  GFormReviewText: ItemProps;
  GFormReviewTime: ItemProps;
  GFormReviewMultiChoice: ItemProps;

  GAppBar: GAppBarProps;
  GArticle: GArticleProps;
  GArticleFeedback: GArticleFeedbackProps;
  GBookings: GBookingsProps;

  GPopoverButton: GPopoverButtonProps;
  GPopoverTopics: GPopoverTopicsProps;
  GPopoverSearch: GPopoverSearchProps;

  GConfirm: GConfirmProps;
  GContractItem: GContractItemProps;
  GContracts: GContractsProps;

  GFooter: GFooterProps;
  GForm: GFormProps;
  GLayout: GLayoutProps;
  GLoader: GLoaderProps;
  GLocales: GLocalesProps;
  GLogin: GLoginProps;
  GLogout: GLogoutProps;

  GLogo: GLogoProps;

  GServices: GServicesProps;
  GServicesSearch: GServicesSearchProps;

  GTooltip: GTooltipProps;
  GOffers: GOffersProps;

  GUserOverviewDetail: GUserOverviewDetailProps;
  GUserOverview: GUserOverviewProps;
  GUserOverviewMenu: GUserOverviewMenuProps;

  GInbox: GInboxProps;
  GInboxMessages: GInboxMessagesProps;
  GInboxAttachments: GInboxAttachmentsProps;
  GInboxFormReview: GInboxFormReviewProps;
  GInboxMessageNotAllowed: GInboxMessageNotAllowedProps;

  GLinks: GLinksProps;
  GLinkHyper: GLinkHyperProps;
  GLinkPhone: GLinkPhoneProps;
  GLinkFormSecured: GLinkFormSecuredProps;
  GLinkFormUnsecured: GLinkFormUnsecuredProps;
  GLinkInfo: GLinkInfoProps;
  GLinksPage: GLinksPageProps;

  GFormBase: GFormBaseProps;

  GMarkdown: GMarkdownProps;

  GShell: GShellProps;

  GSearchList: GSearchListProps;
  GSearchListItem: GSearchListItemProps;

  GAuth: GAuthProps;
  GAuthRepPerson: GAuthRepPersonProps;
  GAuthRepCompany: GAuthRepCompanyProps;
  GAuthUn: GAuthUnProps;
  GAuthUnRepCompany: GAuthUnRepCompanyProps;
  GAuthUnRepPerson: GAuthUnRepPersonProps;
}

export interface GComponentNameToClassKey {

  GFormReviewBoolean: GFormReviewBooleanClassKey;
  GFormReviewChoice: GFormReviewChoiceClassKey;
  GFormReviewDate: GFormReviewDateClassKey;
  GFormReviewDecimal: GFormReviewDecimalClassKey;
  GFormReviewGroup: GFormReviewGroupClassKey;
  GFormReviewItem: GFormReviewItemClassKey;
  GFormReviewNote: GFormReviewNoteClassKey;
  GFormReviewPage: GFormReviewPageClassKey;
  GFormReviewQuestionnaire: GFormReviewQuestionnaireClassKey;
  GFormReviewRowGroup: GFormReviewRowGroupClassKey;
  GFormReviewSurvey: GFormReviewSurveyClassKey;
  GFormReviewSurveyGroup: GFormReviewSurveyGroupClassKey;
  GFormReviewText: GFormReviewTextClassKey;
  GFormReviewTime: GFormReviewTimeClassKey;
  GFormReviewMultiChoice: GFormReviewMultiChoiceClassKey;


  GAppBar: GAppBarClassKey;
  GArticle: GArticleClassKey;
  GArticleFeedback: GArticleFeedbackClassKey;
  GBookings: GBookingsClassKey;
  GPopoverButton: GPopoverButtonClassKey;
  GPopoverSearch: GPopoverSearchClassKey;
  GPopoverTopics: GPopoverTopicsClassKey;

  GConfirm: GConfirmClassKey;
  GContracts: GContractsClassKey;

  GFooter: GFooterClassKey;
  GForm: GFormClassKey;

  GLayout: GLayoutClassKey;
  GLoader: GLoaderClassKey;
  GLocales: GLocalesClassKey;
  GLogin: GLoginClassKey;
  GLogout: GLogoutClassKey;

  GLogo: GLogoClassKey;

  GServices: GServicesClassKey;
  GServicesSearch: GServicesSearchClassKey;

  GTooltip: GTooltipClassKey;

  GOffers: GOffersClassKey;

  GUserOverviewDetail: GUserOverviewDetailClassKey;
  GUserOverview: GUserOverviewClassKey;
  GUserOverviewMenu: GUserOverviewMenuClassKey;


  GInbox: GInboxClassKey;
  GInboxMessages: GInboxMessagesClassKey;
  GInboxMessageNotAllowed: GInboxMessagesClassKey;

  GInboxAttachments: GInboxAttachmentsClassKey;
  GInboxFormReview: GInboxFormReviewClassKey;

  GLinks: GLinksClassKey;
  GLinkHyper: GLinkHyperClassKey;
  GLinkPhone: GLinkPhoneClassKey;
  GLinkFormSecured: GLinkFormSecuredClassKey;
  GLinkFormUnsecured: GLinkFormUnsecuredClassKey;
  GLinkInfo: GLinkInfoClassKey;
  GLinksPage: GLinksPageClassKey;

  GMarkdown: GMarkdownClassKey;

  GFormBase: GFormBaseClassKey;
  GShell: GShellClassKey;

  GSearchList: GSearchListClassKey;
  GSearchListItem: GSearchListItemClassKey;

  GAuth: GAuthClassKey;
  GAuthRepCompany: GAuthRepCompanyClassKey;
  GAuthRepPerson: GAuthRepPersonClassKey;
  GAuthUn: GAuthUnClassKey;
  GAuthUnRepCompany: GAuthUnRepCompanyClassKey;
  GAuthUnRepPerson: GAuthUnRepPersonClassKey;
}

export interface GComponents<Theme = unknown> {

  GFormReviewBoolean?: {
    defaultProps?: GComponentsProps['GFormReviewBoolean'];
    styleOverrides?: GComponentsOverrides<Theme>['GFormReviewBoolean'];
    variants?: GComponentsVariants['GFormReviewBoolean'];
  },
  GFormReviewChoice?: {
    defaultProps?: GComponentsProps['GFormReviewChoice'];
    styleOverrides?: GComponentsOverrides<Theme>['GFormReviewChoice'];
    variants?: GComponentsVariants['GFormReviewChoice'];
  },
  GFormReviewDate?: {
    defaultProps?: GComponentsProps['GFormReviewDate'];
    styleOverrides?: GComponentsOverrides<Theme>['GFormReviewDate'];
    variants?: GComponentsVariants['GFormReviewDate'];
  },
  GFormReviewMultiChoice?: {
    defaultProps?: GComponentsProps['GFormReviewMultiChoice'];
    styleOverrides?: GComponentsOverrides<Theme>['GFormReviewMultiChoice'];
    variants?: GComponentsVariants['GFormReviewMultiChoice'];
  },
  GFormReviewDecimal?: {
    defaultProps?: GComponentsProps['GFormReviewDecimal'];
    styleOverrides?: GComponentsOverrides<Theme>['GFormReviewDecimal'];
    variants?: GComponentsVariants['GFormReviewDecimal'];
  },
  GFormReviewGroup?: {
    defaultProps?: GComponentsProps['GFormReviewGroup'];
    styleOverrides?: GComponentsOverrides<Theme>['GFormReviewGroup'];
    variants?: GComponentsVariants['GFormReviewGroup'];
  },
  GFormReviewItem?: {
    defaultProps?: GComponentsProps['GFormReviewItem'];
    styleOverrides?: GComponentsOverrides<Theme>['GFormReviewItem'];
    variants?: GComponentsVariants['GFormReviewItem'];
  },
  GFormReviewNote?: {
    defaultProps?: GComponentsProps['GFormReviewNote'];
    styleOverrides?: GComponentsOverrides<Theme>['GFormReviewNote'];
    variants?: GComponentsVariants['GFormReviewNote'];
  },
  GFormReviewPage?: {
    defaultProps?: GComponentsProps['GFormReviewPage'];
    styleOverrides?: GComponentsOverrides<Theme>['GFormReviewPage'];
    variants?: GComponentsVariants['GFormReviewPage'];
  },
  GFormReviewQuestionnaire?: {
    defaultProps?: GComponentsProps['GFormReviewQuestionnaire'];
    styleOverrides?: GComponentsOverrides<Theme>['GFormReviewQuestionnaire'];
    variants?: GComponentsVariants['GFormReviewQuestionnaire'];
  },
  GFormReviewRowGroup?: {
    defaultProps?: GComponentsProps['GFormReviewRowGroup'];
    styleOverrides?: GComponentsOverrides<Theme>['GFormReviewRowGroup'];
    variants?: GComponentsVariants['GFormReviewRowGroup'];
  },
  GFormReviewSurvey?: {
    defaultProps?: GComponentsProps['GFormReviewSurvey'];
    styleOverrides?: GComponentsOverrides<Theme>['GFormReviewSurvey'];
    variants?: GComponentsVariants['GFormReviewSurvey'];
  },
  GFormReviewSurveyGroup?: {
    defaultProps?: GComponentsProps['GFormReviewSurveyGroup'];
    styleOverrides?: GComponentsOverrides<Theme>['GFormReviewSurveyGroup'];
    variants?: GComponentsVariants['GFormReviewSurveyGroup'];
  },
  GFormReviewText?: {
    defaultProps?: GComponentsProps['GFormReviewText'];
    styleOverrides?: GComponentsOverrides<Theme>['GFormReviewText'];
    variants?: GComponentsVariants['GFormReviewText'];
  },
  GFormReviewTime?: {
    defaultProps?: GComponentsProps['GFormReviewTime'];
    styleOverrides?: GComponentsOverrides<Theme>['GFormReviewTime'];
    variants?: GComponentsVariants['GFormReviewTime'];
  },


  GAppBar?: {
    defaultProps?: GComponentsProps['GAppBar'];
    styleOverrides?: GComponentsOverrides<Theme>['GAppBar'];
    variants?: GComponentsVariants['GAppBar'];
  },
  GArticle?: {
    defaultProps?: GComponentsProps['GArticle'];
    styleOverrides?: GComponentsOverrides<Theme>['GArticle'];
    variants?: GComponentsVariants['GArticle'];
  },
  GArticleFeedback?: {
    defaultProps?: GComponentsProps['GArticleFeedback'];
    styleOverrides?: GComponentsOverrides<Theme>['GArticleFeedback'];
    variants?: GComponentsVariants['GArticleFeedback'];
  },
  GBookings?: {
    defaultProps?: GComponentsProps['GBookings'];
    styleOverrides?: GComponentsOverrides<Theme>['GBookings'];
    variants?: GComponentsVariants['GBookings'];
  },
  GPopoverButton?: {
    defaultProps?: GComponentsProps['GPopoverButton'];
    styleOverrides?: GComponentsOverrides<Theme>['GPopoverButton'];
    variants?: GComponentsVariants['GPopoverButton'];
  },
  GPopoverSearch?: {
    defaultProps?: GComponentsProps['GPopoverSearch'];
    styleOverrides?: GComponentsOverrides<Theme>['GPopoverSearch'];
    variants?: GComponentsVariants['GPopoverSearch'];
  },
  GPopoverTopics?: {
    defaultProps?: GComponentsProps['GPopoverTopics'];
    styleOverrides?: GComponentsOverrides<Theme>['GPopoverTopics'];
    variants?: GComponentsVariants['GPopoverTopics'];
  },
  GConfirm?: {
    defaultProps?: GComponentsProps['GConfirm'];
    styleOverrides?: GComponentsOverrides<Theme>['GConfirm'];
    variants?: GComponentsVariants['GConfirm'];
  },
  GContracts?: {
    defaultProps?: GComponentsProps['GContracts'];
    styleOverrides?: GComponentsOverrides<Theme>['GContracts'];
    variants?: GComponentsVariants['GContracts'];
  },
  GFooter?: {
    defaultProps?: GComponentsProps['GFooter'];
    styleOverrides?: GComponentsOverrides<Theme>['GFooter'];
    variants?: GComponentsVariants['GFooter'];
  },
  GForm?: {
    defaultProps?: GComponentsProps['GForm'];
    styleOverrides?: GComponentsOverrides<Theme>['GForm'];
    variants?: GComponentsVariants['GForm'];
  },
  GLayout?: {
    defaultProps?: GComponentsProps['GLayout'];
    styleOverrides?: GComponentsOverrides<Theme>['GLayout'];
    variants?: GComponentsVariants['GLayout'];
  },
  GLoader?: {
    defaultProps?: GComponentsProps['GLoader'];
    styleOverrides?: GComponentsOverrides<Theme>['GLoader'];
    variants?: GComponentsVariants['GLoader'];
  },
  GLocales?: {
    defaultProps?: GComponentsProps['GLocales'];
    styleOverrides?: GComponentsOverrides<Theme>['GLocales'];
    variants?: GComponentsVariants['GLocales'];
  },
  GLogin?: {
    defaultProps?: GComponentsProps['GLogin'];
    styleOverrides?: GComponentsOverrides<Theme>['GLogin'];
    variants?: GComponentsVariants['GLogin'];
  },
  GLogout?: {
    defaultProps?: GComponentsProps['GLogout'];
    styleOverrides?: GComponentsOverrides<Theme>['GLogout'];
    variants?: GComponentsVariants['GLogout'];
  },
  GLogo?: {
    defaultProps?: GComponentsProps['GLogo'];
    styleOverrides?: GComponentsOverrides<Theme>['GLogo'];
    variants?: GComponentsVariants['GLogo'];
  },
  GServices?: {
    defaultProps?: GComponentsProps['GServices'];
    styleOverrides?: GComponentsOverrides<Theme>['GServices'];
    variants?: GComponentsVariants['GServices'];
  },
  GServicesSearch?: {
    defaultProps?: GComponentsProps['GServicesSearch'];
    styleOverrides?: GComponentsOverrides<Theme>['GServicesSearch'];
    variants?: GComponentsVariants['GServicesSearch'];
  },
  GTooltip?: {
    defaultProps?: GComponentsProps['GTooltip'];
    styleOverrides?: GComponentsOverrides<Theme>['GTooltip'];
    variants?: GComponentsVariants['GTooltip'];
  },
  GOffers?: {
    defaultProps?: GComponentsProps['GOffers'];
    styleOverrides?: GComponentsOverrides<Theme>['GOffers'];
    variants?: GComponentsVariants['GOffers'];
  },
  GUserOverviewDetail?: {
    defaultProps?: GComponentsProps['GUserOverviewDetail'];
    styleOverrides?: GComponentsOverrides<Theme>['GUserOverviewDetail'];
    variants?: GComponentsVariants['GUserOverviewDetail'];
  },
  GUserOverview?: {
    defaultProps?: GComponentsProps['GUserOverview'];
    styleOverrides?: GComponentsOverrides<Theme>['GUserOverview'];
    variants?: GComponentsVariants['GUserOverview'];
  },
  GUserOverviewMenu?: {
    defaultProps?: GComponentsProps['GUserOverviewMenu'];
    styleOverrides?: GComponentsOverrides<Theme>['GUserOverviewMenu'];
    variants?: GComponentsVariants['GUserOverviewMenu'];
  },
  GInbox?: {
    defaultProps?: GComponentsProps['GInbox'];
    styleOverrides?: GComponentsOverrides<Theme>['GInbox'];
    variants?: GComponentsVariants['GInbox'];
  },
  GInboxMessages?: {
    defaultProps?: GComponentsProps['GInboxMessages'];
    styleOverrides?: GComponentsOverrides<Theme>['GInboxMessages'];
    variants?: GComponentsVariants['GInboxMessages'];
  },
  GInboxMessageNotAllowed?: {
    defaultProps?: GComponentsProps['GInboxMessageNotAllowed'];
    styleOverrides?: GComponentsOverrides<Theme>['GInboxMessageNotAllowed'];
    variants?: GComponentsVariants['GInboxMessageNotAllowed'];
  },
  GInboxAttachments?: {
    defaultProps?: GComponentsProps['GInboxAttachments'];
    styleOverrides?: GComponentsOverrides<Theme>['GInboxAttachments'];
    variants?: GComponentsVariants['GInboxAttachments'];
  },
  GInboxFormReview?: {
    defaultProps?: GComponentsProps['GInboxFormReview'];
    styleOverrides?: GComponentsOverrides<Theme>['GInboxFormReview'];
    variants?: GComponentsVariants['GInboxFormReview'];
  },

  GLinks?: {
    defaultProps?: GComponentsProps['GLinks'];
    styleOverrides?: GComponentsOverrides<Theme>['GLinks'];
    variants?: GComponentsVariants['GLinks'];
  },
  GLinkHyper?: {
    defaultProps?: GComponentsProps['GLinkHyper'];
    styleOverrides?: GComponentsOverrides<Theme>['GLinkHyper'];
    variants?: GComponentsVariants['GLinkHyper'];
  },
  GLinkPhone?: {
    defaultProps?: GComponentsProps['GLinkPhone'];
    styleOverrides?: GComponentsOverrides<Theme>['GLinkPhone'];
    variants?: GComponentsVariants['GLinkPhone'];
  },
  GLinkFormSecured?: {
    defaultProps?: GComponentsProps['GLinkFormSecured'];
    styleOverrides?: GComponentsOverrides<Theme>['GLinkFormSecured'];
    variants?: GComponentsVariants['GLinkFormSecured'];
  },
  GLinkFormUnsecured?: {
    defaultProps?: GComponentsProps['GLinkFormUnsecured'];
    styleOverrides?: GComponentsOverrides<Theme>['GLinkFormUnsecured'];
    variants?: GComponentsVariants['GLinkFormUnsecured'];
  },
  GLinkInfo?: {
    defaultProps?: GComponentsProps['GLinkInfo'];
    styleOverrides?: GComponentsOverrides<Theme>['GLinkInfo'];
    variants?: GComponentsVariants['GLinkInfo'];
  },
  GLinksPage?: {
    defaultProps?: GComponentsProps['GLinksPage'];
    styleOverrides?: GComponentsOverrides<Theme>['GLinksPage'];
    variants?: GComponentsVariants['GLinksPage'];
  },


  GMarkdown?: {
    defaultProps?: GComponentsProps['GMarkdown'];
    styleOverrides?: GComponentsOverrides<Theme>['GMarkdown'];
    variants?: GComponentsVariants['GMarkdown'];
  },
  // ----------------------- COMPLETED UNTIL HERE ---------------------------


  GFormBase?: {
    defaultProps?: GComponentsProps['GFormBase'];
    styleOverrides?: GComponentsOverrides<Theme>['GFormBase'];
    variants?: GComponentsVariants['GFormBase'];
  },
  GSearchList?: {
    defaultProps?: GComponentsProps['GSearchList'];
    styleOverrides?: GComponentsOverrides<Theme>['GSearchList'];
    variants?: GComponentsVariants['GSearchList'];
  },
  GSearchListItem?: {
    defaultProps?: GComponentsProps['GSearchListItem'];
    styleOverrides?: GComponentsOverrides<Theme>['GSearchListItem'];
    variants?: GComponentsVariants['GSearchListItem'];
  },
  GShell?: {
    defaultProps?: GComponentsProps['GShell'];
    styleOverrides?: GComponentsOverrides<Theme>['GShell'];
    variants?: GComponentsVariants['GShell'];
  },



  // Authentication related technical components... they do not contain styling  
  GAuth?: {
    defaultProps?: GComponentsProps['GAuth'];
    styleOverrides?: GComponentsOverrides<Theme>['GAuth'];
    variants?: GComponentsVariants['GAuth'];
  },
  GAuthRepCompany?: {
    defaultProps?: GComponentsProps['GAuthRepCompany'];
    styleOverrides?: GComponentsOverrides<Theme>['GAuthRepCompany'];
    variants?: GComponentsVariants['GAuthRepCompany'];
  },
  GAuthRepPerson?: {
    defaultProps?: GComponentsProps['GAuthRepPerson'];
    styleOverrides?: GComponentsOverrides<Theme>['GAuthRepPerson'];
    variants?: GComponentsVariants['GAuthRepPerson'];
  },
  GAuthUn?: {
    defaultProps?: GComponentsProps['GAuthUn'];
    styleOverrides?: GComponentsOverrides<Theme>['GAuthUn'];
    variants?: GComponentsVariants['GAuthUn'];
  },
  GAuthUnRepCompany?: {
    defaultProps?: GComponentsProps['GAuthUnRepCompany'];
    styleOverrides?: GComponentsOverrides<Theme>['GAuthUnRepCompany'];
    variants?: GComponentsVariants['GAuthUnRepCompany'];
  },
  GAuthUnRepPerson?: {
    defaultProps?: GComponentsProps['GAuthUnRepPerson'];
    styleOverrides?: GComponentsOverrides<Theme>['GAuthUnRepPerson'];
    variants?: GComponentsVariants['GAuthUnRepPerson'];
  },
}


/**
 * MUI module overrides 
 */
export type GComponentsProps = {
  [Name in keyof GComponentsPropsList]?: Partial<GComponentsPropsList[Name]>;
};

export type GComponentsVariants = {
  [Name in keyof GComponentsPropsList]?: Array<{
    props: Partial<GComponentsPropsList[Name]>;
    style: Interpolation<{ theme: Theme }>;
  }>;
}
export type GComponentsOverrides<Theme = unknown> = {
  [Name in keyof GComponentNameToClassKey]?: Partial<
    OverridesStyleRules<GComponentNameToClassKey[Name], Name, Theme>
  >;
} & {
  MuiCssBaseline?: CSSObject | string | ((theme: Theme) => CSSInterpolation);
}