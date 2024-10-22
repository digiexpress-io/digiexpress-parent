import React from 'react';
import { useIntl } from 'react-intl';
import { useLocation } from 'react-router-dom';
import { ConfirmationDialog } from './ConfirmationDialog';
import ReactRouterPrompt from 'react-router-prompt';

interface Props {
  when?: boolean;
  navigate: (path: string) => void;
  navigationConfirmationRequired: () => boolean;
}

export const PageLeavingConfirmation = ({
  navigate,
  navigationConfirmationRequired,
}: Props) => {
  const intl = useIntl();
  const location = useLocation(); 
  
  const shouldBlockNavigation = navigationConfirmationRequired();

  return (
    <ReactRouterPrompt when={shouldBlockNavigation}>
      {({ isActive, onConfirm, onCancel }) => (
        <ConfirmationDialog
          open={isActive}
          text={intl.formatMessage({ id: 'confirm.unsavedChanges' })}
          onClose={onCancel}
          onAccept={() => {
            onConfirm(); 
            navigate(location.pathname); 
          }}
          onCancel={onCancel}
          title={intl.formatMessage({ id: 'confirm.close.title' })}
        />
      )}
    </ReactRouterPrompt>
  );
};
