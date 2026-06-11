import { FsDirentArticleTemplateCreate, FsDirentArticleTemplateUpdate } from '../fs-dirent-article-template';
import { FsPropertiesTemplate } from '../fs-panel-properties/FsPropertiesTemplate';
import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './WidgetFactory';



export const ArticleTemplateWidget: DirentWidget = {
  views: {
    CreateView: FsDirentArticleTemplateCreate,
    UpdateView: FsDirentArticleTemplateUpdate,
    PropertiesView: FsPropertiesTemplate,
    HelpView: _article_template_help_view
  },
  icons: {
    dirent: {
      Marker: _article_template_icon,
      Collapsed: _article_template_icon,
      Expanded: _article_template_icon
    }
  },
  colors: {
    direntDark: FsColors.direntTypes.dark.article,
    direntLight: FsColors.direntTypes.light.article
  },
  classNames: {
    dirent: '',
    icon: 'iconArticleTemplate'
  },
  meta: {
    type: 'ARTICLE_TEMPLATE',
    extension: '.template',
    configOptions: [],
    supportedViews: ['properties', 'preview', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats', 'help'],
  }
};

// TODO
function _article_template_help_view() {
  return (<>HELP MARKDOWN</>);
}
function _article_template_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.ArticleOutlined} {...props} />;
}
