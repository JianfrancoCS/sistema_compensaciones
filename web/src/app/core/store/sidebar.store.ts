import { signalStore, withState, withMethods, withHooks, patchState } from '@ngrx/signals';
import { inject, effect } from '@angular/core';

interface SidebarState {
  isOpen: boolean;
  isDesktop: boolean;
}

const initialState: SidebarState = {
  isOpen: false,
  isDesktop: false
};

export const SidebarStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),

  withMethods((store) => ({
    toggleSidebar: () => {
      patchState(store, { isOpen: !store.isOpen() });
    },

    closeSidebar: () => {
      patchState(store, { isOpen: false });
    },

    openSidebar: () => {
      patchState(store, { isOpen: true });
    },

    setDesktopMode: (isDesktop: boolean) => {
      const currentIsOpen = store.isOpen();
      patchState(store, {
        isDesktop,
        isOpen: isDesktop ? false : currentIsOpen
      });
    }
  })),

  withHooks({
    onInit(store) {
      if (typeof window !== 'undefined') {
        const savedState = sessionStorage.getItem('sidebar-state');
        if (savedState) {
          try {
            const parsed = JSON.parse(savedState);
            patchState(store, { isOpen: parsed.isOpen || false });
          } catch (e) {
            console.warn('Error parsing sidebar state:', e);
          }
        }

        store.setDesktopMode(window.innerWidth >= 640);

        const handleResize = () => {
          store.setDesktopMode(window.innerWidth >= 640);
        };

        window.addEventListener('resize', handleResize);

        effect(() => {
          const state = {
            isOpen: store.isOpen(),
            isDesktop: store.isDesktop()
          };
          sessionStorage.setItem('sidebar-state', JSON.stringify(state));
        });
      }
    }
  })
);
