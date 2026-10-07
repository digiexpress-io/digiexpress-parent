import React from 'react';
import { Box, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { usePanelProperties } from '../fs-panel-properties';


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
  const associatedWorkflows = Object.values(selectOptions.direntProps)
    .filter(p => p.type === 'ARTICLE_WORKFLOW' && (p as Fs.WorkflowProps).articles.includes(dirent.id))
    .map(p => getDirent(p.id)?.name ?? p.id);

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
      {associatedWorkflows.length > 0 && (
        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.associatedWorkflows' })}</Typography>
          <ul className={classes.propertyBulletList}>
            {associatedWorkflows.map((name, index) => <li key={index}><Typography className={classes.propertyValue}>{name}</Typography></li>)}
          </ul>
        </div>
      )}
      {descendants.length > 0 && (
        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.children' })}</Typography>
          <ul className={classes.propertyBulletList}>
            {descendants.map(({ dirent: child, depth }) => (
              <li key={child.id} style={{ marginLeft: depth * 16 }}><Typography className={classes.propertyValue}>{child.type === 'ARTICLE' ? getDirentName(child.id) : child.name}</Typography></li>
            ))}
          </ul>
        </div>
      )}
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
    return collectDescendants(child.children, excludeId, nextDepth);
  });
}