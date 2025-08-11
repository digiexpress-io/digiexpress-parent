import { CSSInterpolation, CSSObject, Interpolation, Theme } from '@mui/material';
import { OverridesStyleRules } from '@mui/material/styles/overrides';

import { GLogoClassKey, GLogoProps } from '@dxs-ts/gamut-primitives';

import { GAppBarClassKey, GAppBarProps } from '@dxs-ts/gamut-primitives';
import { GArticleClassKey, GArticleProps } from '@dxs-ts/gamut-primitives';
import { GArticleFeedbackClassKey, GArticleFeedbackProps } from '@dxs-ts/gamut-primitives';
import { GArticleFeedbackViewerClassKey, GArticleFeedbackViewerProps } from '@dxs-ts/gamut-primitives';


import { GPopoverButtonClassKey, GPopoverButtonProps } from '@dxs-ts/gamut-primitives';
import { GPopoverTopicsClassKey, GPopoverTopicsProps } from '@dxs-ts/gamut-primitives';
import { GPopoverSearchClassKey, GPopoverSearchProps } from '@dxs-ts/gamut-primitives';

import { GConfirmClassKey, GConfirmProps } from '@dxs-ts/gamut-primitives';
import { GLoaderClassKey, GLoaderProps } from '@dxs-ts/gamut-primitives';

import { GSecuredServicesSearchClassKey, GSecuredServicesSearchProps } from '@dxs-ts/gamut-primitives';

import { GTooltipClassKey, GTooltipProps } from '@dxs-ts/gamut-primitives';

import { GInboxClassKey, GInboxProps } from '@dxs-ts/gamut-primitives';
import { GInboxAttachmentsClassKey, GInboxAttachmentsProps } from '@dxs-ts/gamut-primitives';
import { GInboxMessageNotAllowed, GInboxMessageNotAllowedProps } from '@dxs-ts/gamut-primitives';
import { GInboxMessagesClassKey, GInboxMessagesProps } from '@dxs-ts/gamut-primitives';
import { GInboxFormReviewClassKey, GInboxFormReviewProps } from '@dxs-ts/gamut-primitives';

import { GSortClassKey, GSortProps } from '@dxs-ts/gamut-primitives';

import { GLocalesClassKey, GLocalesProps } from '@dxs-ts/gamut-primitives';
import { GShellClassKey, GShellProps } from '@dxs-ts/gamut-shell';
import { GLoginClassKey, GLoginProps } from '@dxs-ts/gamut-primitives';
import { GLogoutClassKey, GLogoutProps } from '@dxs-ts/gamut-primitives';

import { GMarkdownClassKey, GMarkdownProps } from '@dxs-ts/gamut-md';
import { GLayoutClassKey, GLayoutProps } from '@dxs-ts/gamut-primitives';
import { GFormBaseClassKey, GFormBaseProps } from '@dxs-ts/gamut-form';

import { GFormGroupClassKey, GFormGroupProps } from '@dxs-ts/gamut-form';
import { GInputMultilistClassKey, GInputMultilistProps } from '@dxs-ts/gamut-form';

import { GFooterClassKey, GFooterProps } from '@dxs-ts/gamut-primitives';
import { GUserOverviewMenuClassKey, GUserOverviewMenuProps } from '@dxs-ts/gamut-primitives';
import {
  GUserOverviewDetailClassKey, GUserOverviewDetailProps,
  GUserOverviewClassKey, GUserOverviewProps,
} from '@dxs-ts/gamut-primitives';

import { GContractsClassKey, GContractsProps, GContractItemProps, } from '@dxs-ts/gamut-primitives';
import { GBookingsClassKey, GBookingsProps } from '@dxs-ts/gamut-primitives';

import { GOffersProps, GOffersClassKey } from '@dxs-ts/gamut-primitives';

import { GLinksClassKey, GLinksProps } from '@dxs-ts/gamut-primitives';
import { GLinksPageClassKey, GLinksPageProps } from '@dxs-ts/gamut-primitives';


import {
  GLinkInfoClassKey, GLinkInfoProps,
  GLinkFormLockedClassKey, GLinkFormLockedProps,
  GLinkFormUnlockedClassKey, GLinkFormUnlockedProps,

  GLinkFormUnlockedSearchResultsClassKey, GLinkFormUnlockedSearchResultsProps,

  GLinkHyperClassKey, GLinkHyperProps,
  GLinkPhoneClassKey, GLinkPhoneProps
} from '@dxs-ts/gamut-primitives';

import { GFormClassKey, GFormProps } from '@dxs-ts/gamut-form';
import { GAuthClassKey, GAuthProps } from '@dxs-ts/gamut-primitives';
import { GAuthUnClassKey, GAuthUnProps } from '@dxs-ts/gamut-primitives';
import { GAuthUnRepCompanyProps, GAuthUnRepCompanyClassKey } from '@dxs-ts/gamut-primitives';
import { GAuthUnRepPersonProps, GAuthUnRepPersonClassKey } from '@dxs-ts/gamut-primitives';
import { GAuthRepCompanyProps, GAuthRepCompanyClassKey } from '@dxs-ts/gamut-primitives';
import { GAuthRepPersonProps, GAuthRepPersonClassKey } from '@dxs-ts/gamut-primitives';

import type { ItemProps, GroupItemProps, PageItemProps, QuestionnaireItemProps, SurveyProps } from '@dxs-ts/gamut-form-review';
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
} from '@dxs-ts/gamut-form-review';
import { GAuthFormStartClassKey, GAuthFormStartProps } from '@dxs-ts/gamut-primitives';


import { GRouterBookingsClassKey, GRouterBookingsProps } from '../g-router-bookings';
import { GRouterFormsAwaitingDecisionClassKey, GRouterFormsAwaitingDecisionProps } from '../g-router-forms-awaiting-decision';
import { GRouterFormsWithDecisionClassKey, GRouterFormsWithDecisionProps } from '../g-router-forms-with-decision';
import { GRouterInboxClassKey, GRouterInboxProps } from '../g-router-inbox';
import { GRouterInboxSubjectClassKey, GRouterInboxSubjectProps } from '../g-router-inbox-subject';
import { GRouterOfferClassKey, GRouterOfferProps } from '../g-router-offer';
import { GRouterOfferSummaryClassKey, GRouterOfferSummaryProps } from '../g-router-offer-summary';
import { GRouterProductClassKey, GRouterProductProps } from '../g-router-product';
import { GRouterSecuredServicesClassKey, GRouterSecuredServicesProps } from '../g-router-secured-services';
import { GRouterUnfinishedFormsClassKey, GRouterUnfinishedFormsProps } from '../g-router-unfinished-forms';
import { GRouterUnsecuredClassKey, GRouterUnsecuredProps } from '../g-router-unsecured';
import { GRouterUserOverviewClassKey, GRouterUserOverviewProps } from '../g-router-user-overview';



/**
 * MUI theme integration
 */
export interface GComponentsPropsList {

  GRouterBookings: GRouterBookingsProps;
  GRouterFormsAwaitingDecision: GRouterFormsAwaitingDecisionProps;
  GRouterFormsWithDecision: GRouterFormsWithDecisionProps;
  GRouterInbox: GRouterInboxProps;
  GRouterInboxSubject: GRouterInboxSubjectProps;
  GRouterOffer: GRouterOfferProps;
  GRouterOfferSummary: GRouterOfferSummaryProps;
  GRouterProduct: GRouterProductProps;
  GRouterSecuredServices: GRouterSecuredServicesProps;
  GRouterUnfinishedForms: GRouterUnfinishedFormsProps;
  GRouterUnsecured: GRouterUnsecuredProps;
  GRouterUserOverview: GRouterUserOverviewProps;

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

  GFormGroup: GFormGroupProps;
  GInputMultilist: GInputMultilistProps;

  GAppBar: GAppBarProps;
  GArticle: GArticleProps;
  GArticleFeedback: GArticleFeedbackProps;
  GArticleFeedbackViewer: GArticleFeedbackViewerProps;
  GBookings: GBookingsProps;

  GSort: GSortProps;

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

  GSecuredServicesSearch: GSecuredServicesSearchProps;

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
  GLinkFormLocked: GLinkFormLockedProps;
  GLinkFormUnlocked: GLinkFormUnlockedProps;
  GLinkFormUnlockedSearchResults: GLinkFormUnlockedSearchResultsProps,

  GLinkInfo: GLinkInfoProps;
  GLinksPage: GLinksPageProps;

  GFormBase: GFormBaseProps;

  GMarkdown: GMarkdownProps;

  GShell: GShellProps;

  GAuth: GAuthProps;
  GAuthFormStart: GAuthFormStartProps;
  GAuthRepPerson: GAuthRepPersonProps;
  GAuthRepCompany: GAuthRepCompanyProps;
  GAuthUn: GAuthUnProps;
  GAuthUnRepCompany: GAuthUnRepCompanyProps;
  GAuthUnRepPerson: GAuthUnRepPersonProps;
}

export interface GComponentNameToClassKey {

  GRouterBookings: GRouterBookingsClassKey;
  GRouterFormsAwaitingDecision: GRouterFormsAwaitingDecisionClassKey;
  GRouterFormsWithDecision: GRouterFormsWithDecisionClassKey;
  GRouterInbox: GRouterInboxClassKey;
  GRouterInboxSubject: GRouterInboxSubjectClassKey;
  GRouterOffer: GRouterOfferClassKey;
  GRouterOfferSummary: GRouterOfferSummaryClassKey;
  GRouterProduct: GRouterProductClassKey;
  GRouterSecuredServices: GRouterSecuredServicesClassKey;
  GRouterUnfinishedForms: GRouterUnfinishedFormsClassKey;
  GRouterUnsecured: GRouterUnsecuredClassKey;
  GRouterUserOverview: GRouterUserOverviewClassKey;

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

  GFormGroup: GFormGroupClassKey;
  GInputMultilist: GInputMultilistClassKey;

  GSort: GSortClassKey;

  GAppBar: GAppBarClassKey;
  GArticle: GArticleClassKey;
  GArticleFeedback: GArticleFeedbackClassKey;
  GArticleFeedbackViewer: GArticleFeedbackViewerClassKey;
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

  GSecuredServicesSearch: GSecuredServicesSearchClassKey;

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
  GLinkFormLocked: GLinkFormLockedClassKey;
  GLinkFormUnlocked: GLinkFormUnlockedClassKey;
  GLinkFormUnlockedSearchResults: GLinkFormUnlockedSearchResultsClassKey,

  GLinkInfo: GLinkInfoClassKey;
  GLinksPage: GLinksPageClassKey;

  GMarkdown: GMarkdownClassKey;

  GFormBase: GFormBaseClassKey;
  GShell: GShellClassKey;

  GAuth: GAuthClassKey;
  GAuthFormStart: GAuthFormStartClassKey;
  GAuthRepCompany: GAuthRepCompanyClassKey;
  GAuthRepPerson: GAuthRepPersonClassKey;
  GAuthUn: GAuthUnClassKey;
  GAuthUnRepCompany: GAuthUnRepCompanyClassKey;
  GAuthUnRepPerson: GAuthUnRepPersonClassKey;
}

export interface GComponents<Theme = unknown> {

  GRouterBookings?: {
    defaultProps?: GComponentsProps['GRouterBookings'];
    styleOverrides?: GComponentsOverrides<Theme>['GRouterBookings'];
    variants?: GComponentsVariants['GRouterBookings'];
  },
  GRouterFormsAwaitingDecision?: {
    defaultProps?: GComponentsProps['GRouterFormsAwaitingDecision'];
    styleOverrides?: GComponentsOverrides<Theme>['GRouterFormsAwaitingDecision'];
    variants?: GComponentsVariants['GRouterFormsAwaitingDecision'];
  },
  GRouterFormsWithDecision?: {
    defaultProps?: GComponentsProps['GRouterFormsWithDecision'];
    styleOverrides?: GComponentsOverrides<Theme>['GRouterFormsWithDecision'];
    variants?: GComponentsVariants['GRouterFormsWithDecision'];
  },
  GRouterInbox?: {
    defaultProps?: GComponentsProps['GRouterInbox'];
    styleOverrides?: GComponentsOverrides<Theme>['GRouterInbox'];
    variants?: GComponentsVariants['GRouterInbox'];
  },
  GRouterInboxSubject?: {
    defaultProps?: GComponentsProps['GRouterInboxSubject'];
    styleOverrides?: GComponentsOverrides<Theme>['GRouterInboxSubject'];
    variants?: GComponentsVariants['GRouterInboxSubject'];
  },
  GRouterOffer?: {
    defaultProps?: GComponentsProps['GRouterOffer'];
    styleOverrides?: GComponentsOverrides<Theme>['GRouterOffer'];
    variants?: GComponentsVariants['GRouterOffer'];
  },
  GRouterOfferSummary?: {
    defaultProps?: GComponentsProps['GRouterOfferSummary'];
    styleOverrides?: GComponentsOverrides<Theme>['GRouterOfferSummary'];
    variants?: GComponentsVariants['GRouterOfferSummary'];
  },
  GRouterProduct?: {
    defaultProps?: GComponentsProps['GRouterProduct'];
    styleOverrides?: GComponentsOverrides<Theme>['GRouterProduct'];
    variants?: GComponentsVariants['GRouterProduct'];
  },
  GRouterSecuredServices?: {
    defaultProps?: GComponentsProps['GRouterSecuredServices'];
    styleOverrides?: GComponentsOverrides<Theme>['GRouterSecuredServices'];
    variants?: GComponentsVariants['GRouterSecuredServices'];
  },
  GRouterUnfinishedForms?: {
    defaultProps?: GComponentsProps['GRouterUnfinishedForms'];
    styleOverrides?: GComponentsOverrides<Theme>['GRouterUnfinishedForms'];
    variants?: GComponentsVariants['GRouterUnfinishedForms'];
  },
  GRouterUnsecured?: {
    defaultProps?: GComponentsProps['GRouterUnsecured'];
    styleOverrides?: GComponentsOverrides<Theme>['GRouterUnsecured'];
    variants?: GComponentsVariants['GRouterUnsecured'];
  },
  GRouterUserOverview?: {
    defaultProps?: GComponentsProps['GRouterUserOverview'];
    styleOverrides?: GComponentsOverrides<Theme>['GRouterUserOverview'];
    variants?: GComponentsVariants['GRouterUserOverview'];
  },
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

  GFormGroup?: {
    defaultProps?: GComponentsProps['GFormGroup'];
    styleOverrides?: GComponentsOverrides<Theme>['GFormGroup'];
    variants?: GComponentsVariants['GFormGroup'];
  },

  GInputMultilist?: {
    defaultProps?: GComponentsProps['GInputMultilist'];
    styleOverrides?: GComponentsOverrides<Theme>['GInputMultilist'];
    variants?: GComponentsVariants['GInputMultilist'];
  },

  GSort?: {
    defaultProps?: GComponentsProps['GSort'];
    styleOverrides?: GComponentsOverrides<Theme>['GSort'];
    variants?: GComponentsVariants['GSort'];
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
  GArticleFeedbackViewer?: {
    defaultProps?: GComponentsProps['GArticleFeedbackViewer'];
    styleOverrides?: GComponentsOverrides<Theme>['GArticleFeedbackViewer'];
    variants?: GComponentsVariants['GArticleFeedbackViewer'];
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
  GSecuredServicesSearch?: {
    defaultProps?: GComponentsProps['GSecuredServicesSearch'];
    styleOverrides?: GComponentsOverrides<Theme>['GSecuredServicesSearch'];
    variants?: GComponentsVariants['GSecuredServicesSearch'];
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
  GLinkFormLocked?: {
    defaultProps?: GComponentsProps['GLinkFormLocked'];
    styleOverrides?: GComponentsOverrides<Theme>['GLinkFormLocked'];
    variants?: GComponentsVariants['GLinkFormLocked'];
  },
  GLinkFormUnlocked?: {
    defaultProps?: GComponentsProps['GLinkFormUnlocked'];
    styleOverrides?: GComponentsOverrides<Theme>['GLinkFormUnlocked'];
    variants?: GComponentsVariants['GLinkFormUnlocked'];
  },
  GLinkFormUnlockedSearchResults?: {
    defaultProps?: GComponentsProps['GLinkFormUnlockedSearchResults'];
    styleOverrides?: GComponentsOverrides<Theme>['GLinkFormUnlockedSearchResults'];
    variants?: GComponentsVariants['GLinkFormUnlockedSearchResults'];
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


  GFormBase?: {
    defaultProps?: GComponentsProps['GFormBase'];
    styleOverrides?: GComponentsOverrides<Theme>['GFormBase'];
    variants?: GComponentsVariants['GFormBase'];
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
  GAuthFormStart?: {
    defaultProps?: GComponentsProps['GAuthFormStart'];
    styleOverrides?: GComponentsOverrides<Theme>['GAuthFormStart'];
    variants?: GComponentsVariants['GAuthFormStart'];
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