import { SiteApi } from '../api-site';

const STORAGE_KEY = 'gamut_cockpit';

export class CockpitStore {
  
  static get(): SiteApi.Cockpit | null {
    const data = localStorage.getItem(STORAGE_KEY);
    if (!data) return null;
    try {
      return JSON.parse(data);
    } catch {
      return null;
    }
  }

  static save(cockpit: SiteApi.Cockpit | null | undefined): void {
    if(cockpit) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(cockpit));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  }

  static clear(): void {
    localStorage.removeItem(STORAGE_KEY);
  }

  static isEnabled(): boolean {
    return this.get() !== null;
  }
}

// Expose to console
if (typeof window !== 'undefined') {
  (window as any).GamutCockpit = {
    enable: (cockpit: SiteApi.Cockpit) => {
      CockpitStore.save(cockpit);
      console.log('GamutCockpit enabled:', cockpit);
      window.location.reload();
    },
    disable: () => {
      CockpitStore.clear();
      console.log('GamutCockpit disabled');
      window.location.reload();
    },
    status: () => {
      const data = CockpitStore.get();
      console.log('GamutCockpit status:', data);
      return data;
    }
  };
}