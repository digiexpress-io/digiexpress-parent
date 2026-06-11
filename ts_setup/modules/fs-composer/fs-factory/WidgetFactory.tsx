import React from 'react';
import { Fs } from '@dxs-ts/fs-api';

import { DeviceUnknown } from '@mui/icons-material';
import { ArticleWidget } from './ArticleWidget';
import { ArticleLinkWidget } from './ArticleLinkWidget';
import { FolderWidget } from './FolderWidget';
import { ArticlePageWidget } from './ArticlePageWidget';
import { ArticleTemplateWidget } from './ArticleTemplateWidget';
import { ArticleWorkflowWidget } from './ArticleWorkflowWidget';
import { LocaleWidget } from './LocaleWidget';
import { FlowWidget } from './FlowWidget';
import { PrintoutWidget } from './PrintoutWidget';
import { PrintoutResourceWidget } from './PrintoutResourceWidget';
import { PrintoutPageWidget } from './PrintoutPageWidget';
import { FlowTaskWidget } from './FlowTaskWidget';
import { DecisionTableWidget } from './DecisionTableWidget';
import { DialobFormWidget } from './DialobFormWidget';

export interface DirentWidgetIconProps {
  xsmall?: boolean;
  small?: boolean;
  medium?: boolean;
  large?: boolean;
  className?: string;
  tooltip?: string;
  color?: string;
}


export interface DirentWidget {
  classNames: {
    //  dirent: Content.tsx
    dirent: string;

    //      iconClassName -  FsDirent/useUtilityClasses: function _getIconClassName(dirent: Fs.DirentBase): keyof FsDirentClasses { /modules/fs-composer/fs-dirent/useOwnerState.ts
    icon: string
  };
  icons: {
    /* 
    FsDirentMenuNew.tsx       const DIRENT_TYPE_ICONS: { [key: string]: React.ElementType<SvgIconProps> } = {
    FsExplorer.tsx            const TYPE_ICONS: Partial<Record<Fs.BodyType, React.ElementType<SvgIconProps>>> = {
    FsPropertiesArticle.tsx   function getTypeIcon(type: Fs.BodyType): React.ElementType<SvgIconProps> {
    Supports.tsx              export const DirentIcon = (props: { dirent: Fs.DirentBase }) => {
    */
    
    dirent: {
      /*   
      Supports.tsx      export const DirentIcon = (props: { dirent: Fs.DirentBase }) => {
      FsDirent.tsx    
                        <IconButton size='small'>
                         {expanded ?
                            <FsIcon small icon={FsIcons.ExpandMore} className={classes.iconExpand} /> :
                            <FsIcon small icon={FsIcons.ChevronRight} className={classes.iconExpand} />}
                        </IconButton>
          */
      Marker: React.ElementType<DirentWidgetIconProps>;
      Collapsed: React.ElementType<DirentWidgetIconProps>;
      Expanded: React.ElementType<DirentWidgetIconProps>;
    };
  };

  colors: {
    /*   fs-colors:  export function getDirentColor(direntType: Fs.BodyType, isDarkTheme: boolean) {
        FsDirentRoot: /fs-dirent/useUtilityClasses.tsx
    */
    direntDark: string;
    direntLight: string;
  };
  views: {
    // FsDirentArticle.tsx
    CreateView: React.ElementType;
    UpdateView: React.ElementType<{ direntId: string }>;
    PropertiesView: React.ElementType<{direntId: string}>;
    HelpView: React.ElementType<{direntId: string}>;
  },
  meta: {
    // FsPanelProperties: function renderTypeSpecificRows(dirent: Fs.DirentBase): React.ReactNode {
    type: Fs.BodyType;

    // helpers.ts: export function getExtension(type: Fs.BodyType): string | undefined {
    extension: string,

    // helpers.ts export function getConfigOptionsForType(type: Fs.BodyType): Fs.SelectOption[] {
    configOptions: Fs.ConfigOption[],

    supportedViews: Fs.SecondaryView[],
  }
};


export function createWidget(dirent: { type: Fs.BodyType }): DirentWidget {
  switch (dirent.type) {
    case 'ARTICLE': return ArticleWidget;
    case 'ARTICLE_LINK': return ArticleLinkWidget;
    case 'ARTICLE_TEMPLATE': return ArticleTemplateWidget;
    case 'ARTICLE_PAGE': return ArticlePageWidget;
    case 'ARTICLE_WORKFLOW': return ArticleWorkflowWidget;
    case 'LOCALE': return LocaleWidget;
    case 'FLOW': return FlowWidget;
    case 'PRINTOUT': return PrintoutWidget;
    case 'PRINTOUT_RESOURCE': return PrintoutResourceWidget;
    case 'PRINTOUT_PAGE': return PrintoutPageWidget;
    case 'FLOW_TASK': return FlowTaskWidget;
    case 'DECISION_TABLE': return DecisionTableWidget;
    case 'DIALOB_FORM_META': return DialobFormWidget;
    case 'FOLDER': return FolderWidget;
    default: return _UN_IMPLEMENTED;
  }
}

export const allWidgets: DirentWidget[] = [
  ArticleWidget,
  ArticleLinkWidget,
  ArticleTemplateWidget,
  ArticlePageWidget,
  ArticleWorkflowWidget,
  LocaleWidget,
  FlowWidget,
  FlowTaskWidget,
  DecisionTableWidget,
  DialobFormWidget,
  PrintoutWidget,
  PrintoutPageWidget,
  PrintoutResourceWidget,
  FolderWidget,
];



const _EMPTY_COLOR = "";

function _EMPTY_ICON() {
  return (<DeviceUnknown />);
}

function _EMPTY_STUB() {
  return (<></>);
}

const _EMPTY_CONFIG_OPTIONS: [] = [];
const _EMPTY_FILE_EXTENSION = '';
const _EMPTY_TYPE: Fs.BodyType = 'ARTICLE';

const _UN_IMPLEMENTED: DirentWidget = {
  views: {
    CreateView: _EMPTY_STUB,
    UpdateView: _EMPTY_STUB,
    PropertiesView: _EMPTY_STUB,
    HelpView: _EMPTY_STUB
  },
  icons: {
    dirent: {
      Marker: _EMPTY_ICON,
      Collapsed: _EMPTY_ICON,
      Expanded: _EMPTY_ICON,
    }
  },
  colors: {
    direntDark: _EMPTY_COLOR,
    direntLight: _EMPTY_COLOR
  },
  classNames: {
    dirent: "",
    icon: ''
  },
  meta: {
    type: _EMPTY_TYPE,
    configOptions: _EMPTY_CONFIG_OPTIONS,
    extension: _EMPTY_FILE_EXTENSION,
    supportedViews: [],
  }
}