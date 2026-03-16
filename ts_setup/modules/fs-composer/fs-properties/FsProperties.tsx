import React from 'react';
import { Typography, Box } from '@mui/material';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { FsPropertiesProps, propertiesMock} from './FsPropertiesProps';
import { useOwnerState } from './useOwnerState';
import { FsPropertiesRoot, useUtilityClasses } from './useUtilityClasses';




export const FsProperties: React.FC<FsPropertiesProps> = (props) => {
  const ownerState = useOwnerState(props);
  const {node} = props;
  const classes = useUtilityClasses();

  if (!node) {
    return (
      <FsPanel title="Properties" icon={<FsIcon icon={FsIcons.Settings} large />} activeNode={false} noNodeMessage="Select a node from the tree to view properties.">
        <></>
      </FsPanel>
    );
  }

  return (
    <FsPanel title={`Properties: ${node.name}`} icon={<FsIcon icon={FsIcons.Settings} large />} activeNode={true}>
      <FsPropertiesRoot className={classes.root} ownerState={ownerState}>
        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Page Locales</Typography>
          <div className={classes.propertyList}>
          {propertiesMock.pages.map((locale, index) => (
            <Box key={index} className={classes.propertyListItem}>{locale}</Box>
          ))}
          </div>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Labels</Typography>
          <div className={classes.propertyList}>
            {propertiesMock.labels.map((label, index) => <Box key={index} className={classes.propertyListItem}>{label}</Box>)}
          </div>
          <div className={classes.tagLabel}>
            <Typography component="span">label</Typography>
          </div>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Service Name</Typography>
          <Typography className={classes.propertyValue}>{propertiesMock.serviceName}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Service Locales</Typography>
          <div className={classes.propertyList}>
          {propertiesMock.serviceLocaleLabels.map((label, index) => (
            <Box key={index} className={classes.propertyListItem}>{label}</Box>
          ))}
          </div>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Service Validity Start</Typography>
          <Typography className={classes.propertyValue}>{propertiesMock.validityStart}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Service Validity End</Typography>
          <Typography className={classes.propertyValue}>{propertiesMock.validityEnd}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Service Validity Period</Typography>
          <Typography className={classes.propertyValue}>{propertiesMock.validityPeriod}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Dialob Form Name</Typography>
          <Typography className={classes.propertyValue}>{propertiesMock.dialobFormName}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Dialob Form Tag</Typography>
          <Typography className={classes.propertyValue}>{propertiesMock.dialobFormTag}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Flow Name</Typography>
          <Typography className={classes.propertyValue}>{propertiesMock.flowName}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Config Options Enabled</Typography>
          <div className={classes.propertyList}>
          {propertiesMock.configOptionsEnabled.map((option, index) => (
            <Box key={index} className={classes.propertyListItem}>{option}</Box>
          ))}
          </div>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Selected Articles</Typography>
          <div className={classes.propertyList}>
          {propertiesMock.selectedArticles.map((article, index) => (
            <Box key={index} className={classes.propertyListItem}>{article}</Box>
          ))}
          </div>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Comments</Typography>
          <Typography className={classes.propertyValue}>{propertiesMock.comments}</Typography>
        </div>

      </FsPropertiesRoot>
    </FsPanel>
  );
};
