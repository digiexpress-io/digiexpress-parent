import React from 'react';
import { Typography, Box, styled } from '@mui/material';
import { FsNode } from '@dxs-ts/fs-api';
import { FsColors, FsIcons } from '../fs-theme';
import { useFs } from '@dxs-ts/fs-api';
import { ViewContainer } from './ViewContainer';

export interface PropertiesViewProps {
  node: FsNode | undefined;
}

export const PropertiesView: React.FC<PropertiesViewProps> = ({ node }) => {
  const { isDarkMode } = useFs();

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

  // Dummy data structure
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

  const mainContent = (
    <PropertiesContainer isDarkMode={isDarkMode}>


      <PropertyRow isDarkMode={isDarkMode}>
        <PropertyLabel className="property-label">Page Locales</PropertyLabel>
        <PropertyList className="property-list">
          {properties.pages.map((locale, index) => (
            <PropertyListItem key={index} className="property-list-item">{locale}</PropertyListItem>
          ))}
        </PropertyList>
      </PropertyRow>

      <PropertyRow isDarkMode={isDarkMode}>
        <PropertyLabel className="property-label">Labels</PropertyLabel>
        <PropertyList className="property-value">
          {properties.labels.map((label, index) => <PropertyListItem key={index} className="property-list-item">{label}</PropertyListItem>)}
        </PropertyList>
        <TagLabel>label</TagLabel>
      </PropertyRow>

      <PropertyRow isDarkMode={isDarkMode}>
        <PropertyLabel className="property-label">Service Name</PropertyLabel>
        <PropertyValue className="property-value">{properties.serviceName}</PropertyValue>
      </PropertyRow>


      <PropertyRow isDarkMode={isDarkMode}>
        <PropertyLabel className="property-label">Service Locales</PropertyLabel>
        <PropertyList className="property-list">
          {properties.serviceLocaleLabels.map((label, index) => (
            <PropertyListItem key={index} className="property-list-item">{label}</PropertyListItem>
          ))}
        </PropertyList>
      </PropertyRow>

      <PropertyRow isDarkMode={isDarkMode}>
        <PropertyLabel className="property-label">Service Validity Start</PropertyLabel>
        <PropertyValue className="property-value">{properties.validityStart}</PropertyValue>
      </PropertyRow>

      <PropertyRow isDarkMode={isDarkMode}>
        <PropertyLabel className="property-label"> Service Validity End</PropertyLabel>
        <PropertyValue className="property-value">{properties.validityEnd}</PropertyValue>
      </PropertyRow>

      <PropertyRow isDarkMode={isDarkMode}>
        <PropertyLabel className="property-label">Service Validity Period</PropertyLabel>
        <PropertyValue className="property-value">{properties.validityPeriod}</PropertyValue>
      </PropertyRow>


      <PropertyRow isDarkMode={isDarkMode}>
        <PropertyLabel className="property-label">Dialob Form Name</PropertyLabel>
        <PropertyValue className="property-value">{properties.dialobFormName}</PropertyValue>
      </PropertyRow>

      <PropertyRow isDarkMode={isDarkMode}>
        <PropertyLabel className="property-label">Dialob Form Tag</PropertyLabel>
        <PropertyValue className="property-value">{properties.dialobFormTag}</PropertyValue>
      </PropertyRow>

      <PropertyRow isDarkMode={isDarkMode}>
        <PropertyLabel className="property-label">Flow Name</PropertyLabel>
        <PropertyValue className="property-value">{properties.flowName}</PropertyValue>
      </PropertyRow>


      <PropertyRow isDarkMode={isDarkMode}>
        <PropertyLabel className="property-label">Config Options Enabled</PropertyLabel>
        <PropertyList className="property-list">
          {properties.configOptionsEnabled.map((option, index) => (
            <PropertyListItem key={index} className="property-list-item">{option}</PropertyListItem>
          ))}
        </PropertyList>
      </PropertyRow>

      <PropertyRow isDarkMode={isDarkMode}>
        <PropertyLabel className="property-label">Selected Articles</PropertyLabel>
        <PropertyList className="property-list">
          {properties.selectedArticles.map((article, index) => (
            <PropertyListItem key={index} className="property-list-item">{article}</PropertyListItem>
          ))}
        </PropertyList>
      </PropertyRow>

      <PropertyRow isDarkMode={isDarkMode}>
        <PropertyLabel className="property-label">Comments</PropertyLabel>
        <PropertyValue className="property-value">{properties.comments}</PropertyValue>
      </PropertyRow>

    </PropertiesContainer>
  );

  return (
    <ViewContainer
      title={`Properties: ${node.name}`}
      icon={<FsIcons.Settings />}
      activeNode={true}
    >
      {mainContent}
    </ViewContainer>
  );
};

const PropertiesContainer = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  display: 'flex',
  flexDirection: 'column',
  border: `1px solid ${isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
  '& > div:nth-of-type(odd)': {
    backgroundColor: isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
  },
}));

const PropertyRow = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode?: boolean }>(({ isDarkMode }) => ({
  display: 'flex',
  gap: '16px',
  padding: '8px 12px',
  backgroundColor: isDarkMode ? FsColors.dark.background : FsColors.light.background,
  borderBottom: `1px solid ${isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
  '&:last-child': {
    borderBottom: 'none'
  },
  '& .property-label': {
    color: isDarkMode ? FsColors.dark.text : FsColors.light.text,
    width: '230px',
    flexShrink: 0,
    paddingRight: '15px'
  },
  '& .property-value': {
    color: isDarkMode ? FsColors.dark.text : FsColors.light.text,
    flex: 1
  },
  '& .property-list': {
    flex: 1
  },
  '& .property-list-item': {
    color: isDarkMode ? FsColors.dark.text : FsColors.light.text,
    backgroundColor: isDarkMode ? 'rgba(255, 255, 255, 0.05)' : 'rgba(0, 0, 0, 0.05)',
    borderColor: isDarkMode ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)'
  }
}));

const PropertyLabel = styled(Typography)(({ theme }) => ({
  ...theme.typography.subtitle2,
  alignContent: 'center',
  fontWeight: 500,
  textTransform: 'uppercase',
}));

const PropertyValue = styled(Typography)(({ theme }) => ({
  ...theme.typography.subtitle2,
  fontWeight: 400,
}));

const PropertyList = styled(Box)(() => ({
  display: 'flex',
  flexWrap: 'wrap',
  gap: '6px',
}));

const PropertyListItem = styled(Box)(({ theme }) => ({
  fontWeight: 400,
  ...theme.typography.subtitle2,
  border: '1px solid',
  paddingLeft: '5px',
  paddingRight: '5px'
}));



const TagRoot = styled(Box)(({ theme }) => ({
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
}));

const TagLabel: React.FC<{ children: any }> = ({ children }) => {
  return (
    <TagRoot>
      <Typography
        component="span"
        sx={{ fontSize: "inherit", fontWeight: "inherit" }}
      >
        {children}
      </Typography>
    </TagRoot>
  );
}