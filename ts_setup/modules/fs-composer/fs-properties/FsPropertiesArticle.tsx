import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs } from '@dxs-ts/fs-api';
import { SvgIconProps } from '@mui/material';
import { FsIcon, FsIcons } from '../fs-theme';
import { useUtilityClasses } from './useUtilityClasses';


function getTypeIcon(type: Fs.Type): React.ElementType<SvgIconProps> {
  switch (type) {
    case 'folder': return FsIcons.FolderClosed;
    case 'article': return FsIcons.Article;
    case 'service': return FsIcons.Settings;
    case 'dialob': return FsIcons.Form;
    case 'flow': return FsIcons.Flow;
    case 'link': return FsIcons.Link;
    case 'language': return FsIcons.Language;
    case 'printout': return FsIcons.Print;
    case 'image': return FsIcons.Image;
    case 'template': return FsIcons.Pdf;
    case 'phone': return FsIcons.Phone;
    default: return FsIcons.Article;
  }
}

export interface FsPropertiesArticleProps {
  direntProps: Fs.ArticleProps;
  children: Fs.Dirent[];
}

export const FsPropertiesArticle: React.FC<FsPropertiesArticleProps> = ({ children }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <div className={classes.propertyRow}>
      <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.children' })}</Typography>
      <div className={classes.commentList}>
        {children.map((child) => (
          <React.Fragment key={child.id}>
            <div className={classes.childRow}>
              <FsIcon small icon={getTypeIcon(child.type)} />
              <Typography className={classes.propertyValue}>{child.name}</Typography>
            </div>
            {child.children.map((grandchild) => (
              <div key={grandchild.id} className={classes.childRowIndented}>
                <FsIcon small icon={getTypeIcon(grandchild.type)} />
                <Typography className={classes.propertyValue}>{grandchild.name}</Typography>
              </div>
            ))}
          </React.Fragment>
        ))}
      </div>
    </div>
  );
};
