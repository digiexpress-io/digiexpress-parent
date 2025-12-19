import React from 'react';
import { DateTimeFormatter } from '@dxs-ts/xui-datetime';

export const FormTableDateTime: React.FC<{ value?: string | Date }> = ({ value }) => {
  if (!value) {
    return <div>--</div>;
  }

  return <DateTimeFormatter value={value} variant="text" />;
};
