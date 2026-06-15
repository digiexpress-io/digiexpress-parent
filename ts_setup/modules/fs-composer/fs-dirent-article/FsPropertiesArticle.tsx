import React from 'react';
import { Box, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { usePanelProperties } from '../fs-panel-properties';
import { createWidget } from '../fs-factory';


export interface FsPropertiesArticleProps {
  direntId: string;
}

export const FsPropertiesArticle: React.FC<FsPropertiesArticleProps> = ({ direntId }) => {
  const intl = useIntl();
  const classes = usePanelProperties();
  const { getParentDirent, getDirentName, selectOptions, getDirent } = useFsDirent();

  const dirent = getDirent(direntId);

  if (!dirent || dirent.type !== 'ARTICLE') {
    return undefined;
  }

  const parentFolder = getParentDirent(dirent.id);
  const descendants = collectDescendants(parentFolder?.children ?? [], dirent.id, 0);
  const associatedLinks = Object.values(selectOptions.direntProps)
    .filter(p => p.type === 'ARTICLE_LINK' && (p as Fs.LinkProps).articles?.includes(dirent.id))
    .map(p => getDirent(p.id)?.name ?? p.id);
  const configOptionsEnabled = dirent.props?.configOptions ?? [];

  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.configOptionsEnabled' })}</Typography>
        <div className={classes.propertyList}>
          {configOptionsEnabled.map((option, index) => (
            <Box key={index} className={classes.configOptionsListItem}>
              {intl.formatMessage({ id: `fs.dirent.configOption.${option}` })}
            </Box>
          ))}
        </div>
      </div>
      {associatedLinks.length > 0 && (
        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.associatedLinks' })}</Typography>
          <ul className={classes.propertyBulletList}>
            {associatedLinks.map((name, index) => <li key={index}><Typography className={classes.propertyValue}>{name}</Typography></li>)}
          </ul>
        </div>
      )}
      <div className={classes.propertyRow}>
        <Typography className={classes.childPropertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.children' })}</Typography>
        <div className={classes.childContainer}>
          {descendants.map(({ dirent: child, depth }) => {
            const widget = createWidget({ type: child.type });
            return (
              <Box key={child.id} className={classes.childRow} sx={{ paddingLeft: `${depth * 16}px` }}> {/* TODO remove sx */}
                <widget.icons.dirent.Expanded small />
                <Typography className={classes.propertyValue}>{child.type === 'ARTICLE' ? getDirentName(child.id) : child.name}</Typography>
              </Box>
            )
          })}
        </div>
      </div>
    </>
  );
};



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