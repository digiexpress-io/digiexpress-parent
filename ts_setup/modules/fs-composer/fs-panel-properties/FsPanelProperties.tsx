import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { FsPanelPropertiesProps } from './FsPanelPropertiesProps';
import { useOwnerState } from './useOwnerState';
import { FsPanelPropertiesRoot, useUtilityClasses } from './useUtilityClasses';
import { FsPropertiesArticle } from './FsPropertiesArticle';
import { FsPropertiesWorkflow } from './FsPropertiesWorkflow';
import { FsPropertiesDialob } from './FsPropertiesDialob';
import { FsPropertiesLanguage } from './FsPropertiesLanguage';
import { FsPropertiesPrintout } from './FsPropertiesPrintout';
import { FsPropertiesLink } from './FsPropertiesLink';
import { FsPropertiesPhone } from './FsPropertiesPhone';
import { FsPropertiesTemplate } from './FsPropertiesTemplate';
import { FsPropertiesPrintoutResource } from './FsPropertiesPrintoutResource';
import { FsPropertiesPrintoutPage } from './FsPropertiesPrintoutPage';
import { FsPropertiesArticlePage } from './FsPropertiesArticlePage';



function renderTypeSpecificRows(dirent: Fs.DirentBase): React.ReactNode {
  switch (dirent.type) {
    case 'ARTICLE': return null;
    case 'ARTICLE_WORKFLOW': return <FsPropertiesWorkflow dirent={dirent} />;
    case 'DIALOB_FORM': return <FsPropertiesDialob dirent={dirent} />;
    case 'LOCALE': return <FsPropertiesLanguage dirent={dirent} />;
    case 'PRINTOUT': return null;
    case 'ARTICLE_LINK': return <FsPropertiesLink dirent={dirent} />;
    // case 'ARTICLE_LINK': return <FsPropertiesPhone dirent={direntProps} />;
    case 'ARTICLE_TEMPLATE': return <FsPropertiesTemplate dirent={dirent} />;
    case 'PRINTOUT_RESOURCE': return <FsPropertiesPrintoutResource dirent={dirent} />;
    case 'PRINTOUT_PAGE': return <FsPropertiesPrintoutPage dirent={dirent} />;
    case 'ARTICLE_PAGE': return <FsPropertiesArticlePage dirent={dirent} />;
    default: return null;
  }
}


export const FsPanelProperties: React.FC<FsPanelPropertiesProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  const { dirent } = ownerState;
  const { getDirentName } = useFsDirent();
  const displayName = props.dirent ? getDirentName(props.dirent.id) : undefined;

  if (!dirent) {
    return (
      <FsPanel title={intl.formatMessage({ id: 'fs.properties.title' })} icon={<FsIcon icon={FsIcons.Settings} large />}
        activeDirent={false}
        noDirentMessage={intl.formatMessage({ id: 'fs.properties.message.selectDirent' })}>
        <></>
      </FsPanel>
    );
  }

  const labels = (dirent.props?.labels ?? []).map(l => l.value);
  const comments = dirent.props?.comments ?? [];
  const description = dirent.props?.assetDescription;

  return (
    <FsPanel title={intl.formatMessage({ id: 'fs.properties.title.direntName' }, { direntName: displayName })} icon={<FsIcon icon={FsIcons.Settings} large />} activeDirent={true}>
      <FsPanelPropertiesRoot className={classes.root} ownerState={ownerState}>


        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.description' })}</Typography>
          <Typography className={classes.propertyValue}>{description}</Typography>
        </div>


        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.labels' })}</Typography>
          <div className={classes.propertyList}>
            {labels.map((label, index) => (
              <div key={index} className={classes.label}>
                <Typography component="span">{label ?? "-"}</Typography>
              </div>
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
                  <Typography component="span" className={classes.commentAuthor}>{c.author}{', '}{c.created}</Typography>
                </div>
              </div>
            ))}
          </div>
        </div>

        {renderTypeSpecificRows(dirent)}

        {dirent.type === 'ARTICLE' && (
          <FsPropertiesArticle direntId={dirent.id} />
        )}

        {dirent.type === 'PRINTOUT' && (
          <FsPropertiesPrintout dirent={dirent} children={dirent.children} />
        )}

      </FsPanelPropertiesRoot>
    </FsPanel>
  );
};
