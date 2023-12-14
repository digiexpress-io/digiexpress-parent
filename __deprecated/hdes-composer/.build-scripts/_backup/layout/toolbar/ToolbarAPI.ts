import { Session } from '../context';

declare namespace ToolbarAPI {  
  interface Active {
    id?: string;
    view?: React.ReactNode;
  }

  interface Items {
    values: Session.ToolbarItem[];
    handle: (value: Session.ToolbarItem, active: Active) => void;
    color: (value: Session.ToolbarItem, active: Active) => "primary" | "inherit";
    find: (id: string) => Session.ToolbarItem | undefined;
  }
}

namespace ToolbarAPI {
  
  export const create = (props: {
      session: Session.Instance,
      actions: Session.Actions,
      values: Session.ToolbarItem[]
    }) => {
      
    return new ImmutableToolbar(props);
  };

  class ImmutableToolbar implements Items {
    private _instance: Session.Instance
    private _actions: Session.Actions;
    private _values: Session.ToolbarItem[];

    constructor(props: {
      session: Session.Instance,
      actions: Session.Actions,
      values: Session.ToolbarItem[]
    }) {

      this._instance = props.session;
      this._actions = props.actions;
      this._values = props.values.map(v => Object.assign({}, v))
        .map(v => {
          v.enabled = props.session.linkId === v.id;
          return v;
        });
    }

    handle(link: Session.ToolbarItem, active: Active) {
      const type = link.type(link.id);
      
      const button = type as Session.ToolbarItemButton;
      const onClick = button.button as any;
      if (onClick) {
        onClick(link.id);
        return;
      }
      
      const dialog = type as Session.ToolbarItemDialog;
      const getDialog = dialog.dialog as any;
      if(getDialog) {
        this._actions.handleDialog(link.id);  
        return;
      }

      const alreadyOpen = this._instance.linkId === active.id;
      if (alreadyOpen && !active.view) {
        const viewInTab = this._instance.findTab(link.id);
        if (viewInTab === undefined) {
          this._actions.handleLink();
        }
      }
      this._actions.handleLink(link.id);
    }
    color(link: Session.ToolbarItem, active: Active) {
      return active.id === link.id && active.view ? "primary" : "inherit";
    }
    find(id: string) {
      const result = this._values.filter(v => v.id === id);
      if (result.length === 1) {
        return result[0];
      }
      return undefined;
    }
    get values(): Session.ToolbarItem[] {
      return this._values;
    }
  }
}

export default ToolbarAPI;