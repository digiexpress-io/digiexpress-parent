import React from 'react';
import { Box, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { SvgIconProps } from '@mui/material';
import { FsIcon, FsIcons } from '../fs-theme';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesArticleProps {
  dirent: Fs.DirentBase;
  children: Fs.DirentBase[];
}

export const FsPropertiesArticle: React.FC<FsPropertiesArticleProps> = ({ dirent }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { getParentDirent, getArticleName } = useFsDirent();

  if (dirent.type !== 'ARTICLE') {
    return undefined;
  }

  const parentFolder = getParentDirent(dirent.id);
  const descendants = collectDescendants(parentFolder?.children ?? [], dirent.id, 0);

  return (
    <div className={classes.propertyRow}>
      <Typography className={classes.childPropertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.children' })}</Typography>
      <div className={classes.childContainer}>
        {descendants.map(({ dirent: child, depth }) => (
          <Box key={child.id} className={classes.childRow} sx={{ paddingLeft: `${depth * 16}px` }}> {/* TODO remove sx */}
            <FsIcon small icon={getTypeIcon(child.type)} />
            <Typography className={classes.propertyValue}>{child.type === 'ARTICLE' ? getArticleName(child.id) : child.name}</Typography>
          </Box>
        ))}
      </div>
    </div>
  );
};

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

function collectDescendants(items: Fs.DirentBase[], excludeId: string, depth: number): Array<{ dirent: Fs.DirentBase; depth: number }> {
  return items.flatMap(child => {
    if (child.id === excludeId) {
      return [];
    }
    if (child.type !== 'FOLDER') {
      return [{ dirent: child, depth }];
    }
    const nextDepth = child.children.some(c => c.type === 'ARTICLE') ? depth + 1 : depth;
    return [{ dirent: child, depth }, ...collectDescendants(child.children, excludeId, nextDepth)];
  });
}