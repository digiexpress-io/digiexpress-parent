//@ts-nocheck
/**
[INFO] TS2786: 'SnackbarProvider' cannot be used as a JSX component.
[INFO]   Its instance type 'SnackbarProvider' is not a valid JSX element.
[INFO]     The types returned by 'render()' are incompatible between these types.
[INFO]       Type 'React.ReactNode' is not assignable to type 'import("/home/runner/work/hdes-parent/hdes-parent/hdes-core/hdes-composer-ui/node_modules/react-intl/node_modules/@types/react/index").ReactNode'.
 */
import React from 'react';

import { SnackbarProvider } from 'notistack';

const SnakbarWrapper: React.FC<{children: any}> = ({children}) => {
  return (<SnackbarProvider>{children}</SnackbarProvider>)
}

export default SnakbarWrapper;









