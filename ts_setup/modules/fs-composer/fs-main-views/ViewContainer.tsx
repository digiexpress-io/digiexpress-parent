import React from 'react';
import { Box, Typography, darken, lighten, styled } from '@mui/material';
import { FsColors } from '../fs-theme';
import { useFs } from '@dxs-ts/fs-api';

export interface ViewContainerProps {
  title: string;
  icon?: React.ReactNode;
  children: React.ReactNode;
  secondaryChildren?: React.ReactNode;
  noNodeMessage?: string;
  activeNode?: boolean;
}

export const ViewContainer: React.FC<ViewContainerProps> = ({
  title,
  icon,
  children,
  secondaryChildren,
  noNodeMessage,
  activeNode = true
}) => {
  const { isDarkMode } = useFs();

  return (
    <ViewContainerRoot isDarkMode={isDarkMode}>
      <Content>
        <Header>
          {icon && <Box sx={{ mr: 1 }}>{icon}</Box>}
          <Typography variant="body1" fontWeight={500}>{title}</Typography>
        </Header>

        <MainSection>
          {activeNode ? children : (
            <Typography variant="body2" color="text.secondary">
              {noNodeMessage || 'Select a node from the tree to view details.'}
            </Typography>
          )}
        </MainSection>

        {activeNode && secondaryChildren && (
          <SecondarySection>
            {secondaryChildren}
          </SecondarySection>
        )}
      </Content>
    </ViewContainerRoot>
  );
};

const ViewContainerRoot = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  flex: 1,
  height: '100%',
  backgroundColor: isDarkMode ? lighten(FsColors.dark.background, 0.03) : darken(FsColors.light.background, 0.01),
  color: isDarkMode ? FsColors.dark.text : FsColors.light.text,
  overflow: 'auto'
}));

const Content = styled(Box)(() => ({
  display: 'flex',
  flexDirection: 'column',
  padding: '8px'
}));

const Header = styled(Box)(() => ({
  display: 'flex',
  marginBottom: '10px'
}));

const MainSection = styled(Box)(() => ({
  marginBottom: '16px'
}));

const SecondarySection = styled(Box)(() => ({
  marginTop: '16px'
}));