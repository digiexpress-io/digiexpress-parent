import React from 'react';
import { Typography, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { FsPropertiesProps } from './FsPropertiesProps';
import { useOwnerState } from './useOwnerState';
import { FsPropertiesRoot, useUtilityClasses } from './useUtilityClasses';




export const FsProperties: React.FC<FsPropertiesProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const {node} = props;
  const classes = useUtilityClasses();

  if (!node) {
    return (
      <FsPanel title={intl.formatMessage({ id: 'fs.properties.title' })} icon={<FsIcon icon={FsIcons.Settings} large />} activeNode={false} noNodeMessage={intl.formatMessage({ id: 'fs.properties.message.selectNode' })}>
        <></>
      </FsPanel>
    );
  }

  return (
    <FsPanel title={intl.formatMessage({ id: 'fs.properties.title.nodeName' }, { nodeName: node.name })} icon={<FsIcon icon={FsIcons.Settings} large />} activeNode={true}>
      <FsPropertiesRoot className={classes.root} ownerState={ownerState}>
        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.pageLocales' })}</Typography>
          <div className={classes.propertyList}>
          {propertiesMock.pages.map((locale, index) => (
            <Box key={index} className={classes.propertyListItem}>{locale}</Box>
          ))}
          </div>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.labels' })}</Typography>
          <div className={classes.propertyList}>
            {propertiesMock.labels.map((label, index) => <Box key={index} className={classes.propertyListItem}>{label}</Box>)}
          </div>
          <div className={classes.tagLabel}>
            <Typography component="span">{intl.formatMessage({ id: 'fs.properties.tagLabel.label' })}</Typography>
          </div>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.serviceName' })}</Typography>
          <Typography className={classes.propertyValue}>{propertiesMock.serviceName}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.serviceLocales' })}</Typography>
          <div className={classes.propertyList}>
          {propertiesMock.serviceLocaleLabels.map((label, index) => (
            <Box key={index} className={classes.propertyListItem}>{label}</Box>
          ))}
          </div>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.serviceValidityStart' })}</Typography>
          <Typography className={classes.propertyValue}>{propertiesMock.validityStart}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.serviceValidityEnd' })}</Typography>
          <Typography className={classes.propertyValue}>{propertiesMock.validityEnd}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.serviceValidityPeriod' })}</Typography>
          <Typography className={classes.propertyValue}>{propertiesMock.validityPeriod}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.dialobFormName' })}</Typography>
          <Typography className={classes.propertyValue}>{propertiesMock.dialobFormName}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.dialobFormTag' })}</Typography>
          <Typography className={classes.propertyValue}>{propertiesMock.dialobFormTag}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.flowName' })}</Typography>
          <Typography className={classes.propertyValue}>{propertiesMock.flowName}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.configOptionsEnabled' })}</Typography>
          <div className={classes.propertyList}>
          {propertiesMock.configOptionsEnabled.map((option, index) => (
            <Box key={index} className={classes.propertyListItem}>{option}</Box>
          ))}
          </div>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.selectedArticles' })}</Typography>
          <div className={classes.propertyList}>
          {propertiesMock.selectedArticles.map((article, index) => (
            <Box key={index} className={classes.propertyListItem}>{article}</Box>
          ))}
          </div>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.comments' })}</Typography>
          <Typography className={classes.propertyValue}>{propertiesMock.comments}</Typography>
        </div>

      </FsPropertiesRoot>
    </FsPanel>
  );
};

const propertiesMock = {
  serviceLocaleLabels: ['en', 'sv', 'fi'],
  serviceName: 'General Message',
  comments: 'Still needs spellcheck in Swedish language translations',
  dialobFormName: 'feedback_form',
  dialobFormTag: 'v1.0',
  flowName: 'General_Message_Flow',
  validityStart: '11.01.2025',
  validityEnd: '12.03.2025',
  validityPeriod: '30 days',
  configOptionsEnabled: ['Assignable', 'DevMode'],
  selectedArticles: ['000_index', '230_send_feedback', '400_contact_us'],
  pages: ['en', 'fi', 'sv'],
  labels: ['protected', 'gdpr']
};
