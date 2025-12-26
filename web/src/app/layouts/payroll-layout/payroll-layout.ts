import {Component, inject, OnInit} from '@angular/core';
import {Navbar} from './components/navbar/navbar';
import {Sidebar} from './components/sidebar/sidebar';
import {RouterOutlet} from '@angular/router';
import {SidebarStore} from '../../core/store/sidebar.store';
import {AuthStore} from '../../core/store/auth.store';

@Component({
  selector: 'app-payroll-layout',
  imports: [
    Navbar,
    Sidebar,
    RouterOutlet
  ],
  templateUrl: './payroll-layout.html',
  standalone: true,
  styleUrl: './payroll-layout.css'
})
export class PayrollLayout implements OnInit {
  sidebarStore = inject(SidebarStore);
  authStore = inject(AuthStore);

  ngOnInit(): void {
    this.authStore.init();
  }
}
