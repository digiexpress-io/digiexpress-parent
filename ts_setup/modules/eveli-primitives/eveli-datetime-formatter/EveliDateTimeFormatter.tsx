import * as React from 'react';
import { DateTimeFormatter as XuiDateTimeFormatter } from '@dxs-ts/xui-datetime';

export const EveliDateTimeFormatter: React.FC<{ value: any; variant?: 'text' }> = (props) => {
  return <XuiDateTimeFormatter {...props} />;
};

export default EveliDateTimeFormatter;
