import React from 'react';
import { Typography, Box, styled } from '@mui/material';
import { TreeNode } from '@dxs-ts/eveli-tree-api';
import { TreeColors, TreeIcons } from '../tree-theme';
import { useEveliTree } from '@dxs-ts/eveli-tree-api';
import { ViewContainer } from './ViewContainer';

export interface ErrorsViewProps {
  node: TreeNode | undefined;
}

export const ErrorsView: React.FC<ErrorsViewProps> = ({ node }) => {
  const { isDarkMode } = useEveliTree();

  if (!node) {
    return (
      <ViewContainer
        title="Errors"
        icon={<TreeIcons.Error />}
        activeNode={false}
        noNodeMessage="Select a node from the tree to view errors1."
      >
        <></>
      </ViewContainer>
    );
  }

  // Dummy error data with fix instructions
  const errors = [
    {
      id: 1,
      severity: 'WARNING',
      title: 'Missing Translation Warning',
      description: 'Main.article does not have a Finnish language page',
      affectedFile: 'fi.language',
      timestamp: '12.02.2025 14:30'
    },
    {
      id: 2,
      severity: 'ERROR',
      title: 'Missing Markdown level 1 heading error',
      description: 'Page in main.article cannot be rendered in portal if no level 1 heading is defined.',
      affectedFile: 'main.article',
      timestamp: '12.02.2025 14:28'
    },
    {
      id: 3,
      severity: 'WARNING',
      title: 'Deprecated Service Reference',
      description: 'This node references "old-message.service" which has been marked as deprecated.',
      affectedFile: 'main.article',
      timestamp: '10.02.2025 09:15'
    },
    {
      id: 4,
      severity: 'ERROR',
      title: 'Broken Reference Link',
      description: 'The reference to "ref.article" could not be resolved in the tree structure.',
      affectedFile: 'main.article',
      timestamp: '11.02.2025 16:45'
    }
  ];

  const errorSummary = {
    total: errors.length,
    error: errors.filter(e => e.severity === 'ERROR').length,
    warning: errors.filter(e => e.severity === 'WARNING').length
  };

  const mainContent = (
    <ErrorsContainer isDarkMode={isDarkMode}>
      <ErrorSummary isDarkMode={isDarkMode}>
        <SummaryTitle>Error Summary</SummaryTitle>
        <SummaryStats isDarkMode={isDarkMode}>
          <StatItem severity="ERROR" isDarkMode={isDarkMode}>
            <StatCount>{errorSummary.error}</StatCount>
            <StatLabel>Errors</StatLabel>
          </StatItem>
          <StatItem severity="WARNING" isDarkMode={isDarkMode}>
            <StatCount>{errorSummary.warning}</StatCount>
            <StatLabel>Warnings</StatLabel>
          </StatItem>
        </SummaryStats>
      </ErrorSummary>

      <ErrorList>
        {errors.map((error) => (
          <ErrorCard key={error.id} severity={error.severity} isDarkMode={isDarkMode}>
            <ErrorHeader severity={error.severity} isDarkMode={isDarkMode}>
              <ErrorIcon>
                {error.severity === 'ERROR' ?
                  <TreeIcons.Error sx={{ color: isDarkMode ? TreeColors.semantic.dangerDark : TreeColors.semantic.dangerLight }} /> :
                  <TreeIcons.Warning sx={{ color: isDarkMode ? TreeColors.semantic.warning : TreeColors.semantic.warningLight }} />}
              </ErrorIcon>
              <ErrorTitle>{error.title}</ErrorTitle>
              <ErrorTimestamp isDarkMode={isDarkMode}>{error.timestamp}</ErrorTimestamp>
            </ErrorHeader>

            <ErrorDescription isDarkMode={isDarkMode}>
              {error.description}
            </ErrorDescription>

          </ErrorCard>
        ))}
      </ErrorList>
    </ErrorsContainer>
  );

  return (
    <ViewContainer
      title={`Errors: ${node.name}`}
      icon={<TreeIcons.Error />}
      activeNode={true}
    >
      {mainContent}
    </ViewContainer>
  );
};

const ErrorsContainer = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  gap: theme.spacing(1),
  padding: theme.spacing(1),
}));

const ErrorSummary = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode, theme }) => ({
  padding: theme.spacing(1),
  backgroundColor: isDarkMode ? TreeColors.dark.surface : TreeColors.light.surface,
  border: `1px solid ${isDarkMode ? TreeColors.dark.border : TreeColors.light.border}`,
}));

const SummaryTitle = styled(Typography)(({ theme }) => ({
  ...theme.typography.subtitle2,
  fontWeight: 500,
  marginBottom: theme.spacing(1)
}));

const SummaryStats = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ theme }) => ({
  display: 'flex',
  gap: theme.spacing(1),
}));

const StatItem = styled(Box, {
  shouldForwardProp: (prop) => !['severity', 'isDarkMode'].includes(prop as string)
})<{ severity: string; isDarkMode: boolean }>(({ severity, isDarkMode, theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  padding: theme.spacing(1),
  backgroundColor: isDarkMode ? TreeColors.dark.surface : TreeColors.light.surface,
  border: `1px solid ${severity === 'ERROR' ?
    (isDarkMode ? TreeColors.semantic.dangerDark : TreeColors.semantic.dangerLight) :
    (isDarkMode ? TreeColors.semantic.warning : TreeColors.semantic.warningLight)}`
}));

const StatCount = styled(Typography)(({ theme }) => ({
  ...theme.typography.subtitle2,
  fontWeight: 500,
}));

const StatLabel = styled(Typography)(({ theme }) => ({
  ...theme.typography.caption,
  textTransform: 'uppercase',
  fontWeight: 500,
}));

const ErrorList = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  gap: theme.spacing(1),
}));

const ErrorCard = styled(Box, {
  shouldForwardProp: (prop) => !['severity', 'isDarkMode'].includes(prop as string)
})<{ severity: string; isDarkMode: boolean }>(({ severity, isDarkMode, theme }) => ({
  padding: theme.spacing(1),
  marginTop: theme.spacing(1),
  backgroundColor: isDarkMode ? TreeColors.dark.background : TreeColors.light.background,
  border: `1px solid ${isDarkMode ? TreeColors.dark.border : TreeColors.light.border}`,
  borderLeft: `4px solid ${severity === 'ERROR' ?
    (isDarkMode ? TreeColors.semantic.dangerDark : TreeColors.semantic.dangerLight) :
    (isDarkMode ? TreeColors.semantic.warning : TreeColors.semantic.warningLight)}`,
}));

const ErrorHeader = styled(Box, {
  shouldForwardProp: (prop) => !['severity', 'isDarkMode'].includes(prop as string)
})<{ severity: string; isDarkMode: boolean }>(({ theme }) => ({
  display: 'flex',
  gap: theme.spacing(1),
  marginBottom: theme.spacing(1),
}));

const ErrorIcon = styled(Box)(() => ({
  fontSize: '16px',
}));

const ErrorTitle = styled(Typography)(({ theme }) => ({
  ...theme.typography.subtitle2,
  fontWeight: 500,
  flex: 1,
}));

const ErrorTimestamp = styled(Typography, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ theme, isDarkMode }) => ({
  ...theme.typography.caption,
  color: isDarkMode ? TreeColors.dark.textSecondary : TreeColors.light.textSecondary,
}));

const ErrorDescription = styled(Typography, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ theme, isDarkMode }) => ({
  ...theme.typography.subtitle2,
  color: isDarkMode ? TreeColors.dark.text : TreeColors.light.text,
  marginBottom: '8px',
}));
