import React from 'react';
import { Box, Typography, styled } from '@mui/material';
import { FsNode } from '@dxs-ts/fs-api';
import { FsColors, FsIcons } from '../fs-theme';
import { useFs } from '@dxs-ts/fs-api';
import { ViewContainer } from './ViewContainer';

interface ReferencesViewProps {
  node: FsNode | undefined;
}

export const ReferencesView: React.FC<ReferencesViewProps> = ({ node }) => {
  const { isDarkMode } = useFs();
  const { findReferencesToNode } = useFs();

  if (!node) {
    return (
      <ViewContainer
        title="References"
        icon={<FsIcons.Tree />}
        activeNode={false}
        noNodeMessage="Select a node from the tree to view references."
      >
        <></>
      </ViewContainer>
    );
  }

  const references = findReferencesToNode(node);

  const mainContent = (
    <ReferenceSection>
      {references.length > 0 ? (
        <ReferencesContainer isDarkMode={isDarkMode}>
          {references.map((ref, index) => (
            <ReferenceRow key={index} isDarkMode={isDarkMode}>
              <ReferenceLocation isDarkMode={isDarkMode}>{ref.location}</ReferenceLocation>
              {/*<ReferenceAssetName isDarkMode={isDarkMode}>{ref.assetName}</ReferenceAssetName> */}
            </ReferenceRow>
          ))}
        </ReferencesContainer>
      ) : (
        <Typography variant="body2" color="text.secondary">
            This node does not contain any references.
        </Typography>
      )}
    </ReferenceSection>
  );

  const secondaryContent = node.children && node.children.length > 0 ? (
    <ChildrenSection>
      <Typography variant="subtitle2" sx={{ mb: 1 }}>
        Child References
      </Typography>
      {node.children.map((child) => (
        <Box key={child.id} sx={{ mb: 1 }}>
          <Typography variant="body2">{child.name}</Typography>
          <Typography variant="caption" color="text.secondary">
            Type: {child.type} {child.reference && ' (REF)'}
          </Typography>
        </Box>
      ))}
    </ChildrenSection>
  ) : undefined;

  return (
    <ViewContainer
      title={`References: ${node.name}`}
      icon={<FsIcons.Tree />}
      secondaryChildren={secondaryContent}
      activeNode={true}
    >
      {mainContent}
    </ViewContainer>
  );
};



const ReferenceSection = styled(Box)(() => ({
  marginBottom: '16px'
}));

const ReferencesContainer = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  display: 'flex',
  flexDirection: 'column',
  border: `1px solid ${isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
  '& > div:nth-of-type(odd)': {
    backgroundColor: isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
  },
}));

const ReferenceRow = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  display: 'flex',
  flexDirection: 'column',
  padding: '8px 12px',
  backgroundColor: isDarkMode ? FsColors.dark.background : FsColors.light.background,
  borderBottom: `1px solid ${isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
  '&:last-child': {
    borderBottom: 'none'
  }
}));

const ReferenceLocation = styled(Typography, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode, theme }) => ({
  ...theme.typography.subtitle2,
  color: isDarkMode ? FsColors.dark.text : FsColors.light.text,
}));

const ReferenceAssetName = styled(Typography, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  fontSize: '14px',
  color: isDarkMode ? FsColors.dark.text : FsColors.light.text,
  marginTop: '2px'
}));

const ChildrenSection = styled(Box)(() => ({
  marginTop: '16px'
}));