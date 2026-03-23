import React from 'react';
import { Typography, SvgIconProps } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentType, FsDirentTypes, useFsNav } from '@dxs-ts/fs-api';
import { FsDirentNewProps } from './FsDirentNewProps';
import { useOwnerState } from './useOwnerState';
import { useUtilityClasses, FsDirentNewRoot } from './useUtilityClasses';
import { FsIcon, FsIcons } from '../fs-theme';

const DIRENT_TYPE_ICONS: Record<FsDirentType, React.ElementType<SvgIconProps>> = {
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
  phone: FsIcons.Phone
};

export const FsDirentNew: React.FC<FsDirentNewProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();
  const { openCreateTab } = useFsNav();

  function handleTypeClick(type: FsDirentType) {
    openCreateTab(type, props.dirent);
    props.onClose();
  }

  return (
    <FsDirentNewRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntNew.title' })}</Typography>
      {(Object.keys(FsDirentTypes) as FsDirentType[]).map((type) => (
        <div key={type} className={classes.listItem} onClick={() => handleTypeClick(type)}>
          <FsIcon icon={DIRENT_TYPE_ICONS[type]} small />
          {intl.formatMessage({ id: `fs.direntNew.type.${type}` })}
        </div>
      ))}
    </FsDirentNewRoot>
  );
};
