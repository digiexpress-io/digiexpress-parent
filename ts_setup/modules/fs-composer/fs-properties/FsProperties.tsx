import React from 'react';
import { Typography, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirent, FsDirentProps } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { FsPropertiesProps } from './FsPropertiesProps';
import { useOwnerState } from './useOwnerState';
import { FsPropertiesRoot, useUtilityClasses } from './useUtilityClasses';
import { FsPropertiesArticle } from './FsPropertiesArticle';
import { FsPropertiesService } from './FsPropertiesService';
import { FsPropertiesDialob } from './FsPropertiesDialob';
import { FsPropertiesLanguage } from './FsPropertiesLanguage';
import { FsPropertiesPrintout } from './FsPropertiesPrintout';
import { FsPropertiesLink } from './FsPropertiesLink';
import { FsPropertiesPhone } from './FsPropertiesPhone';



function renderTypeSpecificRows(direntProps: FsDirentProps, dirent: FsDirent): React.ReactNode {
  switch (direntProps.type) {
    case 'article': return null;
    case 'service': return <FsPropertiesService direntProps={direntProps} />;
    case 'dialob': return <FsPropertiesDialob direntProps={direntProps} />;
    case 'language': return <FsPropertiesLanguage direntProps={direntProps} />;
    case 'printout': return <FsPropertiesPrintout direntProps={direntProps} />;
    case 'link': return <FsPropertiesLink direntProps={direntProps} />;
    case 'phone': return <FsPropertiesPhone direntProps={direntProps} />;
    default: return null;
  }
}


export const FsProperties: React.FC<FsPropertiesProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const { dirent } = props;
  const { direntProps } = ownerState;
  const classes = useUtilityClasses();

  if (!dirent || !direntProps) {
    return (
      <FsPanel title={intl.formatMessage({ id: 'fs.properties.title' })} icon={<FsIcon icon={FsIcons.Settings} large />}
        activeDirent={false}
        noDirentMessage={intl.formatMessage({ id: 'fs.properties.message.selectDirent' })}>
        <></>
      </FsPanel>
    );
  }

  const labels = direntProps.labels.map(l => l.value);
  const configOptionsEnabled = direntProps.configOptions;
  const comments = direntProps.comments;

  return (
    <FsPanel title={intl.formatMessage({ id: 'fs.properties.title.direntName' }, { direntName: dirent.name })} icon={<FsIcon icon={FsIcons.Settings} large />} activeDirent={true}>
      <FsPropertiesRoot className={classes.root} ownerState={ownerState}>

        {renderTypeSpecificRows(direntProps, dirent)}

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.labels' })}</Typography>
          <div className={classes.propertyList}>
            {labels.map((label, index) => <Box key={index} className={classes.propertyListItem}>{label}</Box>)}
          </div>
          <div className={classes.tagLabel}>
            <Typography component="span">{intl.formatMessage({ id: 'fs.properties.tagLabel.label' })}</Typography>
          </div>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.configOptionsEnabled' })}</Typography>
          <div className={classes.propertyList}>
            {configOptionsEnabled.map((option, index) => (
              <Box key={index} className={classes.propertyListItem}>{option}</Box>
            ))}
          </div>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.comments' })}</Typography>
          <div className={classes.commentList}>
            {comments.map((c, index) => (
              <div key={index} className={classes.commentItem}>
                <Typography className={classes.commentText}>{c.comment}</Typography>
                <div className={classes.commentMeta}>
                  <Typography component="span" className={classes.commentAuthor}>{c.author}</Typography>
                  <Typography component="span" className={classes.commentDate}>{c.created}</Typography>
                </div>
              </div>
            ))}
          </div>
        </div>

        {direntProps.type === 'article' && (
          <FsPropertiesArticle direntProps={direntProps} children={dirent.children} />
        )}

      </FsPropertiesRoot>
    </FsPanel>
  );
};
