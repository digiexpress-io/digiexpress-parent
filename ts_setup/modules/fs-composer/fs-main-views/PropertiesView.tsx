import React from 'react';
import { Typography, Box, styled, generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsNode } from '@dxs-ts/fs-api';
import { FsColors, FsIcons } from '../fs-theme';
import { useFs } from '@dxs-ts/fs-api';
import { ViewContainer } from './ViewContainer';

const properties = {
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

export interface PropertiesViewProps {
  node: FsNode | undefined;
}

export const PropertiesView: React.FC<PropertiesViewProps> = ({ node }) => {
  const { isDarkMode } = useFs();
  const classes = useUtilityClasses(isDarkMode);

  if (!node) {
    return (
      <ViewContainer
        title="Properties"
        icon={<FsIcons.Settings />}
        activeNode={false}
        noNodeMessage="Select a node from the tree to view properties."
      >
        <></>
      </ViewContainer>
    );
  }

  return (
    <ViewContainer
      title={`Properties: ${node.name}`}
      icon={<FsIcons.Settings />}
      activeNode={true}
    >
      <PropertiesViewRoot className={classes.root} isDarkMode={isDarkMode}>
        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Page Locales</Typography>
          <div className={classes.propertyList}>
          {properties.pages.map((locale, index) => (
            <Box key={index} className={classes.propertyListItem}>{locale}</Box>
          ))}
          </div>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Labels</Typography>
          <div className={classes.propertyList}>
            {properties.labels.map((label, index) => <Box key={index} className={classes.propertyListItem}>{label}</Box>)}
          </div>
          <div className={classes.tagLabel}>
            <Typography component="span">label</Typography>
          </div>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Service Name</Typography>
          <Typography className={classes.propertyValue}>{properties.serviceName}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Service Locales</Typography>
          <div className={classes.propertyList}>
          {properties.serviceLocaleLabels.map((label, index) => (
            <Box key={index} className={classes.propertyListItem}>{label}</Box>
          ))}
          </div>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Service Validity Start</Typography>
          <Typography className={classes.propertyValue}>{properties.validityStart}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Service Validity End</Typography>
          <Typography className={classes.propertyValue}>{properties.validityEnd}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Service Validity Period</Typography>
          <Typography className={classes.propertyValue}>{properties.validityPeriod}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Dialob Form Name</Typography>
          <Typography className={classes.propertyValue}>{properties.dialobFormName}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Dialob Form Tag</Typography>
          <Typography className={classes.propertyValue}>{properties.dialobFormTag}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Flow Name</Typography>
          <Typography className={classes.propertyValue}>{properties.flowName}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Config Options Enabled</Typography>
          <div className={classes.propertyList}>
          {properties.configOptionsEnabled.map((option, index) => (
            <Box key={index} className={classes.propertyListItem}>{option}</Box>
          ))}
          </div>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Selected Articles</Typography>
          <div className={classes.propertyList}>
          {properties.selectedArticles.map((article, index) => (
            <Box key={index} className={classes.propertyListItem}>{article}</Box>
          ))}
          </div>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>Comments</Typography>
          <Typography className={classes.propertyValue}>{properties.comments}</Typography>
        </div>

      </PropertiesViewRoot>
    </ViewContainer>
  );
};

const MUI_NAME = 'PropertiesView';

export interface PropertiesViewClasses {
  root: string;
  propertyRow: string;
  propertyLabel: string;
  propertyValue: string;
  propertyList: string;
  propertyListItem: string;
  tagLabel: string;
}

export type PropertiesViewClassKey = keyof PropertiesViewClasses;

const useUtilityClasses = (isDarkMode: boolean) => {
  const slots = {
    root: ['root'],
    propertyRow: ['propertyRow'],
    propertyLabel: ['propertyLabel'],
    propertyValue: ['propertyValue'],
    propertyList: ['propertyList'],
    propertyListItem: ['propertyListItem'],
    tagLabel: ['tagLabel'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

const PropertiesViewRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'isDarkMode',
})<{ isDarkMode: boolean }>(({ theme, isDarkMode }) => ({
  display: 'flex',
  flexDirection: 'column',
  border: `1px solid ${isDarkMode ? FsColors.dark.border : FsColors.light.border}`,

  [`& > .${MUI_NAME}-propertyRow:nth-of-type(odd)`]: {
    backgroundColor: isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
  },

  [`& .${MUI_NAME}-propertyRow`]: {
    display: 'flex',
    gap: theme.spacing(2),
    padding: theme.spacing(1, 1.5),
    backgroundColor: isDarkMode ? FsColors.dark.background : FsColors.light.background,
    borderBottom: `1px solid ${isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
    '&:last-child': {
      borderBottom: 'none'
    },
  },

  [`& .${MUI_NAME}-propertyLabel`]: {
    ...theme.typography.subtitle2,
    alignContent: 'center',
    fontWeight: 500,
    textTransform: 'uppercase',
    color: isDarkMode ? FsColors.dark.text : FsColors.light.text,
    width: '230px',
    flexShrink: 0,
    paddingRight: theme.spacing(1.875),
  },

  [`& .${MUI_NAME}-propertyValue`]: {
    ...theme.typography.subtitle2,
    fontWeight: 400,
    color: isDarkMode ? FsColors.dark.text : FsColors.light.text,
    flex: 1,
  },

  [`& .${MUI_NAME}-propertyList`]: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: theme.spacing(0.75),
    flex: 1,
  },

  [`& .${MUI_NAME}-propertyListItem`]: {
    fontWeight: 400,
    ...theme.typography.subtitle2,
    color: isDarkMode ? FsColors.dark.text : FsColors.light.text,
    backgroundColor: isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
    borderColor: isDarkMode ? FsColors.dark.border : FsColors.light.border,
    border: '1px solid',
    paddingLeft: theme.spacing(0.625),
    paddingRight: theme.spacing(0.625),
  },

  [`& .${MUI_NAME}-tagLabel`]: {
    position: "relative",
    display: "inline-flex",
    alignItems: "center",
    height: 28,
    padding: "0 12px 0 16px",
    background: theme.palette.primary.main,
    color: theme.palette.primary.contrastText,
    fontSize: 13,
    borderRadius: 0,

    // left triangle (arrow)
    "&::before": {
      content: '""',
      position: "absolute",
      left: -12,
      width: 0,
      height: 0,
      borderTop: "14px solid transparent",
      borderBottom: "14px solid transparent",
      borderRight: `12px solid ${theme.palette.primary.main}`,
    },

    // hole
    "&::after": {
      content: '""',
      position: "absolute",
      left: -6,
      width: 6,
      height: 6,
      borderRadius: "50%",
      background: theme.palette.background.paper,
    },

    '& .MuiTypography-root': {
      fontSize: "inherit",
      fontWeight: "inherit",
    },
  },
}));