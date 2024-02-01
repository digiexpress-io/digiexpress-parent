import React from 'react';
import {Icon, Loader} from 'semantic-ui-react';
import * as Status from '../helpers/constants';

class StatusIndicator extends React.PureComponent {

  render() {
    const {status} = this.props;

    if (status === Status.STATUS_OK) {
      return <Icon name='check' color='green' fitted size='small' />;
    } else if (status === Status.STATUS_BUSY) {
      return <Loader size='mini' inline active />;
    } else if (status === Status.STATUS_WARNINGS) {
      return <Icon name='warning sign' color='yellow' fitted size='small'/>;
    } else if (status === Status.STATUS_ERRORS ||
               status === Status.STATUS_FATAL) {
                return <Icon name='warning sign' color='red' fitted size='small'/>;
               }
    return null;
  }
}

export {
  StatusIndicator as default
}
