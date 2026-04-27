import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs } from '@dxs-ts/fs-api';
import { SvgIconProps } from '@mui/material';
import { FsIcon, FsIcons } from '../fs-theme';
import { useUtilityClasses } from './useUtilityClasses';


function getTypeIcon(type: Fs.BodyType): React.ElementType<SvgIconProps> {
  switch (type) {
    case 'FOLDER': return FsIcons.FolderClosed;
    case 'DIALOB_FORM': return FsIcons.Form;
    case 'FLOW': return FsIcons.Flow;
    case 'ARTICLE_LINK': return FsIcons.Link;
    case 'LOCALE': return FsIcons.Language;
    case 'PRINTOUT': return FsIcons.Print;
    case 'UNKNOWN': return FsIcons.Image;
    case 'ARTICLE_TEMPLATE': return FsIcons.Pdf;
    case 'ARTICLE_PAGE': return FsIcons.Page;
    case 'ARTICLE': return FsIcons.Article;
    case 'ARTICLE_WORKFLOW': return FsIcons.Settings;

    //case '': return FsIcons.Phone;
    default: return FsIcons.Article;
  }
}

export interface FsPropertiesArticleProps {
  dirent: Fs.Dirent;
  children: Fs.DirentBase[];
}

export const FsPropertiesArticle: React.FC<FsPropertiesArticleProps> = ({ dirent, children }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  if (dirent.type !== 'ARTICLE') {
    return undefined;
  }

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
