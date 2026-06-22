import { FsDirentArticleTemplateCreate, FsDirentArticleTemplateUpdate, FsPropertiesTemplate, FsHelpArticleTemplate } from '../fs-dirent-article-template';
import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './WidgetFactory';



export const ArticleTemplateWidget: DirentWidget = {
  views: {
    CreateView: FsDirentArticleTemplateCreate,
    UpdateView: FsDirentArticleTemplateUpdate,
    PropertiesView: FsPropertiesTemplate,
    HelpView: FsHelpArticleTemplate
  },
  icons: {
    dirent: {
      Marker: _article_template_icon,
      Collapsed: _article_template_icon,
      Expanded: _article_template_icon
    }
  },
  colors: {
    dirent: FsColors.direntTypes.light.article
  },
  classNames: {
    dirent: '',
    icon: 'iconArticleTemplate'
  },
  meta: {
    type: 'ARTICLE_TEMPLATE',
    extension: '.article-template',
    configOptions: [],
    supportedViews: ['properties', 'preview', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats', 'help'],
  }
};
function _article_template_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.ArticleOutlined} {...props} />;
}
