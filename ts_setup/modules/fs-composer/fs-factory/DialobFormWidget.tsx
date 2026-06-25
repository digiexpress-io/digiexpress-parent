import { FsDirentDialobCreate, FsDirentDialobUpdate, FsPropertiesDialob, FsHelpDialob } from '../fs-dirent-dialob';
import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './WidgetFactory';

export const DialobFormWidget: DirentWidget = {
  views: {
    CreateView: FsDirentDialobCreate,
    UpdateView: FsDirentDialobUpdate,
    PropertiesView: FsPropertiesDialob,
    HelpView: FsHelpDialob
  },
  icons: {
    dirent: {
      Marker: _dialob_form_icon,
      Collapsed: _dialob_form_icon,
      Expanded: _dialob_form_icon
    }
  },
  colors: {
    dirent: FsColors.direntTypes.form
  },
  classNames: {
    dirent: '',
    icon: 'iconDialobForm'
  },
  meta: {
    type: 'DIALOB_FORM',
    extension: '.dialob',
    configOptions: [],
    supportedViews: ['properties', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats', 'help'],
  }
};


function _dialob_form_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.Form} {...props} />;
}
