import Burger from 'components-burger';

export const useApp = () => {
  const apps = Burger.useApps();

  function changeApp(input: 'tasks' | 'frontoffice' | 'stencil' | 'hdes') {
    if (input === 'frontoffice') {
      apps.actions.handleActive('app-frontoffice');
      return;
    }
    if (input === 'stencil') {
      apps.actions.handleActive('app-stencil');
      return;
    }

    if (input === 'hdes') {
      apps.actions.handleActive('app-hdes');
      return;
    }
  }
  return { changeApp };
}