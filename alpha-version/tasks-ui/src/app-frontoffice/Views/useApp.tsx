import { useSecondaryMenuItem } from 'app-frontoffice/FrontofficePrefs';
import Burger from 'components-burger';

export const useApp = () => {
  const apps = Burger.useApps();
  const session = useSecondaryMenuItem();

  function changeApp(input: 'tasks' | 'frontoffice' | 'stencil' | 'hdes') {
    if (input === 'frontoffice') {
      session.setNextValue('app-frontoffice');
      apps.actions.handleActive('app-frontoffice');
      return;
    }
    if (input === 'stencil') {
      session.setNextValue('stencil');
      apps.actions.handleActive('app-stencil');
      return;
    }

    if (input === 'hdes') {
      session.setNextValue('hdes');
      apps.actions.handleActive('app-hdes');
      return;
    }
  }
  return { changeApp };
}