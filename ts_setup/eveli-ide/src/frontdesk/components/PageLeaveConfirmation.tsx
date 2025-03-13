import { useIntl } from 'react-intl';
import { useBlocker } from '@tanstack/react-router'
import { ConfirmationDialog } from './ConfirmationDialog';

interface Props {
  navigationConfirmationRequired: () => boolean;
}

export const PageLeavingConfirmation = ({ navigationConfirmationRequired }: Props) => {
  
  const intl = useIntl();
  const { proceed, reset, status } = useBlocker({
    shouldBlockFn: ({ current, next }) => {
      return navigationConfirmationRequired();
    },
    enableBeforeUnload: false,
    withResolver: true,
  })
  return (    
    <ConfirmationDialog
      open={status === 'blocked'}
      text={intl.formatMessage({ id: 'confirm.unsavedChanges' })}
      onClose={() => {
        if(reset) {
          reset();
        }
      }}
      onAccept={() => {
        if(proceed) {
          proceed() 
        }
      }}
    onCancel={() => {
      if(reset) {
        reset();
      }
    }}
    title={intl.formatMessage({ id: 'confirm.close.title' })}
  />
  );
};
