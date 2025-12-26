import { Component, inject } from '@angular/core';
import { SidebarStore } from '../../../../core/store/sidebar.store';
import { AvatarModule } from 'primeng/avatar';
import { SplitButtonModule } from 'primeng/splitbutton';
import {UserDropdown} from '../user-drowpdown/user-dropdown';
import {NotificationButton} from '../notification-button/notification-button';

@Component({
  selector: 'app-navbar',
  imports: [AvatarModule, SplitButtonModule, UserDropdown, NotificationButton],
  templateUrl: './navbar.html',
  standalone: true,
  styleUrl: './navbar.css'
})
export class Navbar {
  sidebarStore = inject(SidebarStore);


}
