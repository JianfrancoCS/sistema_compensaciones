import { Component, inject, OnInit, computed } from '@angular/core';
import { AccordionModule } from 'primeng/accordion';
import { Router, RouterLink } from '@angular/router';
import { SidebarStore } from '@core/store/sidebar.store';
import { AuthStore } from '@core/store/auth.store';
import { NavigationItem } from '@core/models/auth.model';
import { resolvePrimeIcon } from '@core/utils/icon-resolver';

@Component({
  selector: 'app-sidebar',
  imports: [AccordionModule, RouterLink],
  templateUrl: './sidebar.html',
  styleUrls: ['./sidebar.css'],
  standalone: true
})
export class Sidebar implements OnInit {
  private readonly router = inject(Router);
  sidebarStore = inject(SidebarStore);
  authStore = inject(AuthStore);

  public readonly navigationItems = computed(() => {
    const menu = this.authStore.menu();
    return menu || [];
  });

  ngOnInit(): void {
    this.authStore.init();
  }

  public hasChildren(item: NavigationItem): boolean {
    return item.children && item.children.length > 0;
  }

  public isActiveRoute(route?: string | null): boolean {
    if (!route) return false;
    return this.router.url === route;
  }

  public onMenuItemClick(): void {
    if (!this.sidebarStore.isDesktop()) {
      this.sidebarStore.closeSidebar();
    }
  }

  public getPrimeIcon(icon: string | null | undefined): string {
    return resolvePrimeIcon(icon);
  }
}
