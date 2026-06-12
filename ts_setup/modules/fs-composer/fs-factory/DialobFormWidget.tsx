import { FsDirentDialobCreate, FsDirentDialobUpdate, FsPropertiesDialob } from '../fs-dirent-dialob';
import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './WidgetFactory';

export const DialobFormWidget: DirentWidget = {
  views: {
    CreateView: FsDirentDialobCreate,
    UpdateView: FsDirentDialobUpdate,
    PropertiesView: FsPropertiesDialob,
    HelpView: _dialob_form_help_view
  },
  icons: {
    dirent: {
      Marker: _dialob_form_icon,
      Collapsed: _dialob_form_icon,
      Expanded: _dialob_form_icon
    }
  },
  colors: {
    direntDark: FsColors.direntTypes.dark.form,
    direntLight: FsColors.direntTypes.light.form
  },
  classNames: {
    dirent: '',
    icon: 'iconDialobForm'
  },
  meta: {
    type: 'DIALOB_FORM_META',
    extension: '.dialob',
    configOptions: [],
    supportedViews: ['properties', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats', 'help'],
  }
};

function _dialob_form_help_view() {
  return (<>HELP MARKDOWN</>);
}
function _dialob_form_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.Form} {...props} />;
}
