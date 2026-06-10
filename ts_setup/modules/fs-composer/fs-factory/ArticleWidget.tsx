import React from 'react';
import { FsDirentArticleCreate } from '../fs-dirent-article/FsDirentArticleCreate';
import { FsDirentArticleUpdate } from '../fs-dirent-article/FsDirentArticleUpdate';
import { FsPropertiesArticle } from '../fs-panel-properties/FsPropertiesArticle';
import { FsColors, FsIcons } from '../fs-theme';
import { DirentWidget } from './index';


// TODO
const FsPanelHelpArticle: React.FC = () => {
  return (<>HELP MARKDOWN</>);
};

export const ArticleWidget: DirentWidget = {
  views: {
    CreateView: FsDirentArticleCreate,
    UpdateView: FsDirentArticleUpdate,
    PropertiesView: FsPropertiesArticle,
    HelpView: FsPanelHelpArticle
  },
  icons: {
    dirent: {
      Collapsed: FsIcons.Article,
      Expanded: FsIcons.Article
    }
  },
  colors: {
    direntDark: FsColors.direntTypes.dark.article,
    direntLight: FsColors.direntTypes.light.article
  },
  classNames: {
    dirent: '',
    icon: 'iconArticle'
  },
  meta: {
    type: 'ARTICLE',
    extension: '.article',
    configOptions: ['DEV_MODE', 'DISABLED_MODE']
  }
};
