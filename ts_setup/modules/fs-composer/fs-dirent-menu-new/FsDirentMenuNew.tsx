import React from 'react';
import { Typography, SvgIconProps } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsNav } from '@dxs-ts/fs-api';
import { FsDirentMenuNewProps } from './FsDirentMenuNewProps';
import { useOwnerState } from './useOwnerState';
import { useUtilityClasses, FsDirentMenuNewRoot } from './useUtilityClasses';
import { FsIcon, FsIcons } from '../fs-theme';

const DIRENT_TYPE_ICONS: Record<Fs.Type, React.ElementType<SvgIconProps>> = {
  folder: FsIcons.FolderClosed,
  article: FsIcons.Article,
  service: FsIcons.Settings,
  dialob: FsIcons.Form,
  flow: FsIcons.Flow,
  link: FsIcons.Link,
  language: FsIcons.Language,
  printout: FsIcons.Print,
  image: FsIcons.Image,
  template: FsIcons.Pdf,
  phone: FsIcons.Phone,
  page: FsIcons.Form
};

export const FsDirentMenuNew: React.FC<FsDirentMenuNewProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();
  const { openCreateTab } = useFsNav();

  function handleTypeClick(type: Fs.Type) {
    openCreateTab(type, props.dirent);
    props.onClose();
  }

  return (
    <FsDirentMenuNewRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntNew.title' })}</Typography>
      {(['folder', 'article', 'service', 'dialob', 'flow', 'link', 'language', 'printout', 'image', 'template', 'phone', 'page'] as Fs.Type[]).map((type) => (
        <div key={type} className={classes.listItem} onClick={() => handleTypeClick(type)}>
          <FsIcon icon={DIRENT_TYPE_ICONS[type]} small />
          {intl.formatMessage({ id: `fs.direntNew.type.${type}` })}
        </div>
      ))}
    </FsDirentMenuNewRoot>
  );
};
