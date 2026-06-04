import React from 'react';
import { MenuItem, Typography, SvgIconProps } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { FsDirentMenuNewProps } from './FsDirentMenuNewProps';
import { useOwnerState } from './useOwnerState';
import { useUtilityClasses, FsDirentMenuNewRoot } from './useUtilityClasses';
import { FsIcon, FsIcons } from '../fs-theme';

const DIRENT_TYPE_ICONS: { [key: string]: React.ElementType<SvgIconProps> } = {
  FOLDER: FsIcons.FolderClosed,
  ARTICLE: FsIcons.Article,
  ARTICLE_WORKFLOW: FsIcons.Settings,
  DIALOB_FORM: FsIcons.Form,
  FLOW: FsIcons.Flow,
  ARTICLE_LINK: FsIcons.Link,
  LOCALE: FsIcons.Language,
  PRINTOUT: FsIcons.Print,
  PRINTOUT_RESOURCE: FsIcons.Image,
  ARTICLE_TEMPLATE: FsIcons.Pdf,
  ARTICLE_PAGE: FsIcons.Form,
  DIALOB_FORM_META: 'symbol',
  FLOW_TASK: FsIcons.Assignment,
  DECISION_TABLE: FsIcons.DecisionTable,
  PRINTOUT_PAGE: FsIcons.Page,
};

export const FsDirentMenuNew: React.FC<FsDirentMenuNewProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();
  const { openCreateTab } = useFsNav();
  const { creatableTypes } = useFsDirent();

  function handleTypeClick(type: Fs.BodyType) {
    openCreateTab(type, props.dirent);
    props.onClose();
  }

  return (
    <FsDirentMenuNewRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntNew.title' })}</Typography>
      {creatableTypes.map((type) => (
        <MenuItem key={type} className={classes.listItem} onClick={() => handleTypeClick(type)} disableRipple>
          <FsIcon icon={DIRENT_TYPE_ICONS[type]} small />
          {intl.formatMessage({ id: `fs.direntNew.type.${type.toLocaleLowerCase()}` })}
        </MenuItem>
      ))}
    </FsDirentMenuNewRoot>
  );
};
