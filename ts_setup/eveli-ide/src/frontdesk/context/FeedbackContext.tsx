import React, { PropsWithChildren, useState } from 'react';
import type Feedback from '@resys/feedback';

interface State {
  isOpen: boolean;
  beforeSend?: (feedback: Feedback) => void;
}

type OpenFn = (beforeSend?: (feedback: Feedback) => void) => void;
type CloseFn = () => void;

export const FeedbackContext = React.createContext<State & {open: OpenFn, close: CloseFn}>({
  isOpen: false,
  open: () => {},
  close: () => {},
});

export const FeedbackProvider: React.FC<PropsWithChildren> = ({ children }) => {
  const [state, setState] = useState<State>({
    isOpen: false,
  });

  const value = {
    ...state,
    open: (beforeSend?: (feedback: Feedback) => void) => {
      setState({ isOpen: true, beforeSend });
    },
    close: () => {
      setState({ isOpen: false, beforeSend: undefined });
    }
  };


  return (
    <FeedbackContext.Provider value={value}>
      {children}
    </FeedbackContext.Provider>
  );
}
