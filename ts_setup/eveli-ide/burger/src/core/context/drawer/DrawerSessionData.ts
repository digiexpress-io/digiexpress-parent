import * as API from './DrawerAPI';

class DrawerSessionData implements API.DrawerSession {
  private _drawer: boolean;

  constructor(props: { drawer?: boolean }) {
    this._drawer = props.drawer ? true : false;
  }
  get drawer() {
    return this._drawer;
  }
  withDrawer(open: boolean): API.DrawerSession {
    return new DrawerSessionData({drawer: open});
  }
}


export default DrawerSessionData;
