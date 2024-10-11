import { BurgerApi } from '../../BurgerApi';

class DrawerSessionData implements BurgerApi.DrawerSession {
  private _drawer: boolean;

  constructor(props: { drawer?: boolean }) {
    this._drawer = props.drawer ? true : false;
  }
  get drawer() {
    return this._drawer;
  }
  withDrawer(open: boolean): BurgerApi.DrawerSession {
    return new DrawerSessionData({drawer: open});
  }
}


export default DrawerSessionData;
