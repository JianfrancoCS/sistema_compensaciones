import { Component, OnInit, inject, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UserStore } from '@core/store/user.store';
import { ProfileStore } from '@core/store/profile.store';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { ToastModule } from 'primeng/toast';

@Component({
  selector: 'app-assign-profiles',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule, CheckboxModule, ToastModule],
  templateUrl: './assign-profiles.html',
  styleUrl: './assign-profiles.css'
})
export class AssignProfilesComponent implements OnInit {
  readonly userStore = inject(UserStore);
  readonly profileStore = inject(ProfileStore);
  private router = inject(Router);
  private activatedRoute = inject(ActivatedRoute);

  selectedProfilePublicIds = signal<string[]>([]);
  userPublicId = signal<string | null>(null);
  username = signal<string>('');

  constructor() {
    effect(() => {
      const profilesForAssignment = this.userStore.profilesForAssignment();
      if (profilesForAssignment) {
        const assignedIds: string[] = [];
        profilesForAssignment.forEach(profile => {
          if (profile.isSelected) {
            assignedIds.push(profile.publicId);
          }
        });
        this.selectedProfilePublicIds.set(assignedIds);
        this.username.set(profilesForAssignment[0]?.username || '');
      }
    }, { allowSignalWrites: true });
  }

  ngOnInit(): void {
    const publicId = this.activatedRoute.snapshot.paramMap.get('publicId');
    if (publicId) {
      this.userPublicId.set(publicId);
      if (this.profileStore.profiles().length === 0) {
        this.profileStore.loadAllForSelect();
      }
      this.userStore.loadProfilesForAssignment(publicId);
    } else {
      this.router.navigate(['/system/settings/users']);
    }
  }

  get profiles(): any[] {
    return this.userStore.profilesForAssignment() || [];
  }


  saveAssignments(): void {
    const publicId = this.userPublicId();
    if (publicId) {
      this.userStore.syncUserProfiles({
        userId: publicId,
        profileIds: this.selectedProfilePublicIds()
      });
      setTimeout(() => {
        this.router.navigate(['/system/settings/users']);
      }, 1000);
    }
  }

  cancel(): void {
    this.router.navigate(['/system/settings/users']);
  }
}

