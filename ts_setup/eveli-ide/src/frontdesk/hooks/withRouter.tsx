import React from 'react';
import {
  useNavigate,
} from 'react-router-dom';

export interface WithRouterProps {
  navigate: ReturnType<typeof useNavigate>;
}

export const withRouter = <Props extends WithRouterProps>(
  WrappedComponent: React.ComponentType<Props>
) => {
  return (props: Omit<Props, keyof WithRouterProps>) => {
    const navigate = useNavigate();

    return (
      <WrappedComponent
        {...(props as Props)}
        navigate={navigate}
      />
    );
  };
};