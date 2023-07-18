import React from 'react';

import LibraryBooksIcon from '@material-ui/icons/LibraryBooks';
import CachedIcon from '@material-ui/icons/Cached';
import AddCircleIcon from '@material-ui/icons/AddCircle';
import PlayCircleFilledIcon from '@material-ui/icons/PlayCircleFilled';
import ExtensionIcon from '@material-ui/icons/Extension';
import InfoIcon from '@material-ui/icons/Info';
import NewReleasesIcon from '@material-ui/icons/NewReleases';
import FileCopyIcon from '@material-ui/icons/FileCopy';
import ImportExportIcon from '@material-ui/icons/ImportExport';
import { FormattedMessage } from 'react-intl';
import fileDownload from 'js-file-download'

import { Layout } from '../deps';
import Explorer from '../explorer';
import AddType from '../add-type';
import CopyAs from '../copy-as';
import Info from '../info';
import Resources from '../';
import { SaveDialog, SaveButton } from '../save';


const saveEnabled = (props: {
  resource: Resources.Session.ContextType,
  layout: Layout.Session.ContextType
}) => {
  const tabs = props.layout.session.tabs;
  const active = tabs[props.layout.session.history.open];
  if (!active) {
    return false;
  }
  const editor = props.resource.session.editor.getContent(active.id);

  if (!editor) {
    return false;
  }

  return editor.saved === false;
}

const createToolbar = (props: {
  resource: Resources.Session.ContextType,
  layout: Layout.Session.ContextType
}): Layout.Session.ToolbarItem[] => {

  return [
    {
      id: 'toolbar.explorer',
      icon: <LibraryBooksIcon />,
      type: () => ({ page: () => (<Explorer.View />) })
    },

    {
      id: 'toolbar.save',
      enabled: saveEnabled(props),
      icon: <SaveButton />,
      type: () => ({ dialog: (onClose: () => void) => (<SaveDialog handleClose={onClose} />) })
    },

    {
      id: 'toolbar.export',
      icon: <ImportExportIcon />,
      type: () => ({ button: () => 
        props.resource.service.resources.export().then(data => {
          fileDownload(data, 'migration.json')  
        })
      })
    },

    {
      id: 'toolbar.debug',
      icon: <PlayCircleFilledIcon />,
      type: () => ({
        button: () => props.layout.actions.handleTabAdd({ id: 'debug', label: <FormattedMessage id={"debug.tab.name"} /> })
      })
    },

    {
      id: 'toolbar.add',
      icon: <AddCircleIcon />,
      type: () => ({
        dialog: (onClose: () => void) => (<AddType handleClose={onClose} onSave={(asset) => Explorer.openTab(asset, props.layout.actions)} />)
      })
    },

    {
      id: 'toolbar.copyas',
      icon: <FileCopyIcon />,
      type: () => ({
        dialog: (onClose: () => void) => (<CopyAs handleClose={onClose} onSave={(asset) => Explorer.openTab(asset, props.layout.actions)} />)
      })
    },

    {
      id: 'toolbar.extensions',
      icon: <ExtensionIcon />,
      type: () => ({ button: () => (console.log("refresh")) })
    },

    {
      id: 'toolbar.releases',
      icon: <NewReleasesIcon />,
      type: () => ({
        button: () => props.layout.actions.handleTabAdd({ id: 'releases', label: 'Releases' })
      })
    },

    {
      id: 'toolbar.reload',
      icon: <CachedIcon />,
      type: () => ({ button: () => (props.resource.actions.setReload()) })
    },

    {
      id: 'toolbar.info',
      icon: <InfoIcon />,
      type: () => ({ dialog: (onClose: () => void) => (<Info handleClose={onClose} />) })
    },
  ];
}

export { createToolbar };


