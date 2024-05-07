import { useIntl } from 'react-intl';

import { useAvatar } from 'descriptor-avatar';
import { _nobody_, Palette, TaskStatus, TaskPriority } from 'descriptor-task';

import { GroupByTypes } from '../TableContext';



export function useTitle(props: { groupByType: GroupByTypes, classifierValue: string }) {
  const { groupByType, classifierValue } = props;
  const intl = useIntl();
  const avatar = useAvatar(classifierValue, groupByType === 'owners' || groupByType === 'roles');

  if (!groupByType) {
    return {
      title: 'Contained',
      color: 'primary'
    };
  }

  if (groupByType === 'status') {
    const backgroundColor = Palette.status[classifierValue as TaskStatus];
    return {
      title: intl.formatMessage({ id: `tasktable.header.spotlight.status.${classifierValue}` }),
      color: backgroundColor
    };
  } else if (groupByType === 'priority') {
    const backgroundColor = Palette.priority[classifierValue as TaskPriority];
    return {
      title: intl.formatMessage({ id: `tasktable.header.spotlight.priority.${classifierValue}` }),
      color: backgroundColor
    };
  } else if (groupByType === 'roles' || groupByType === 'owners') {
    const title = classifierValue === _nobody_ ? intl.formatMessage({ id: classifierValue }) : avatar?.displayName
    return {
      title, 
      color: avatar?.colorCode
    };
  }

  return {
    title: intl.formatMessage({ id: 'tasktable.header.spotlight.no_group' }),
    color: 'primary'
  };
}


