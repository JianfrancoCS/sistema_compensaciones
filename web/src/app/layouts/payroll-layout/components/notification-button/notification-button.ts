import {Component, signal} from '@angular/core';
import {OverlayBadgeModule} from 'primeng/overlaybadge';

@Component({
  selector: 'app-notification-button',
  imports: [OverlayBadgeModule],
  templateUrl: './notification-button.html',
  standalone: true,
  styleUrl: './notification-button.css'
})
export class NotificationButton {

  countNotification = signal<number>(0);

  public pendingNotifications(): boolean {
    return this.countNotification() > 0;
  }

}
