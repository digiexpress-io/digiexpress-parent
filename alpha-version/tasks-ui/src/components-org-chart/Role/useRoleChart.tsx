import { ChartTree, ChartProps } from '../ChartTree';
import { Role, useAm } from 'descriptor-access-mgmt';
import { grey_light } from 'components-colors';

import { FormattedMessage } from 'react-intl';
import { RoleContainer } from './RoleContainer';
import { Paper } from '@mui/material';


export function useRoleChart(): { tip: ChartProps } {
  const { roles } = useAm();

  const tip: ChartProps = {
    id: "root",
    label: (<Paper elevation={2} sx={{
        p: '1.25rem', mx: 1, 
        display: "inline-block", 
        borderTop: '5px solid',
        borderTopColor: grey_light
      }}>
      <FormattedMessage id="am.chart.tree.tip.name" />
    </Paper>)
    ,
    expanded: true,
    children: new ChartPropsVisitor(roles).build()
  };

  return { tip };
}


class ChartPropsVisitor {
  private _tips: Role[] = [];
  private _all_roles: Record<string, Role> = {};
  private _chart_tips: ChartProps[] = [];
  private _chart_props: Record<string, ChartProps> = {};
  private _all_roles_by_parent: Record<string, Role[]> = {};

  constructor(allRoles: Role[]) {
    for(const role of allRoles) {
      if(!role.parentId) {
        this._tips.push(role)
      } else {
        if(!this._all_roles_by_parent[role.parentId]) {
          this._all_roles_by_parent[role.parentId] = [];
        }
        this._all_roles_by_parent[role.parentId].push(role);
      }
      this._all_roles[role.id] = role;
    }
  }

  private visitTip(tip: Role) {
    const props: ChartProps = {
      id: tip.id,
      label: <RoleContainer role={tip} />,
      expanded: true,
      children: []
    }
    this._chart_tips.push(props);
    this._chart_props[tip.id] = props;
    this.visitChildRoles(tip)
  }

  private visitChildRoles(parent: Role) {
    const children = this._all_roles_by_parent[parent.id];
    if(!children) {
      return;
    }

    for(const child of children) {
      const { parentId } = child;
      if(parentId === undefined) {
        continue;
      }
      const props: ChartProps = {
        id: child.id,
        label: <RoleContainer role={child} />,
        expanded: true,
        children: []
      }
      this._chart_props[parentId].children?.push(props);
      this.visitChildRoles(child);
    }
  }

  build(): ChartProps[] {
    this._tips.forEach((tip) => this.visitTip(tip));
    return this._chart_tips;
  }
}
