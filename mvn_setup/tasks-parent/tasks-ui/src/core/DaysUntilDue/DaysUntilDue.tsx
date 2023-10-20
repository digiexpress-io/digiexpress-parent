import React from 'react';
import { FormattedMessage } from 'react-intl';


const DaysUntilDue: React.FC<{ daysUntilDue: number | undefined }> = ({ daysUntilDue }) => {
  if (!daysUntilDue) {
    return (<></>);
  }
  if (daysUntilDue < 0) {
    let daysToFormat = daysUntilDue;
    const overdueDays = daysToFormat *= -1;
    return (<FormattedMessage id='core.teamSpace.task.daysUntilDue.overdueBy' values={{ count: overdueDays }} />);
  }
  return (<FormattedMessage id='core.teamSpace.task.daysUntilDue' values={{ count: daysUntilDue }} />);
}

export default DaysUntilDue;