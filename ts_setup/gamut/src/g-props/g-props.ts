import { CSSInterpolation, CSSObject, Interpolation, Theme } from '@mui/material';
import { OverridesStyleRules } from '@mui/material/styles/overrides';

import { GLogoClassKey, GLogoProps } from '../g-logo';

import { GAppBarClassKey, GAppBarProps } from '../g-app-bar';
import { GArticleClassKey, GArticleProps } from '../g-article';

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
import { GInboxMessagesClassKey, GInboxMessagesProps } from '../g-inbox-messages';
import { GInboxMessageNotAllowed, GInboxMessageNotAllowedProps } from '../g-inbox-messages';
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


declare module "@mui/material" {
  export interface Components<Theme = unknown> extends GComponents<Theme> { }
}



/**
 * MUI theme integration
 */
export interface GComponentsPropsList {
  GAppBar: GAppBarProps;
  GArticle: GArticleProps;
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

  // ----------------------- COMPLETED UNTIL HERE ---------------------------

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
  GAppBar: GAppBarClassKey;
  GArticle: GArticleClassKey;
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

  // ----------------------- COMPLETED UNTIL HERE ---------------------------

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