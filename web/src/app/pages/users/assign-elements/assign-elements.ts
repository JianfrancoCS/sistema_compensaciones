import { Component, OnInit, inject, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UserStore } from '@core/store/user.store';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { UserElementsByContainer, UserContainerWithElements } from '@shared/types/security';

@Component({
  selector: 'app-assign-user-elements',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule, CheckboxModule],
  templateUrl: './assign-elements.html',
  styleUrl: './assign-elements.css'
})
export class AssignUserElementsComponent implements OnInit {
  readonly userStore = inject(UserStore);
  private router = inject(Router);
  private activatedRoute = inject(ActivatedRoute);

  selectedElementPublicIds = signal<string[]>([]);
  userPublicId = signal<string | null>(null);
  username = signal<string>('');

  constructor() {
    effect(() => {
      const elementsByContainer = this.userStore.elementsByContainer();
      if (elementsByContainer) {
        const assignedIds: string[] = [];
        elementsByContainer.containers.forEach(container => {
          assignedIds.push(...container.selectedElementPublicIds);
        });
        this.selectedElementPublicIds.set(assignedIds);
        this.username.set(elementsByContainer.username);
      }
    }, { allowSignalWrites: true });
  }

  ngOnInit(): void {
    const publicId = this.activatedRoute.snapshot.paramMap.get('publicId');
    if (publicId) {
      this.userPublicId.set(publicId);
      this.userStore.loadElementsByContainer(publicId);
    } else {
      this.router.navigate(['/system/settings/users']);
    }
  }

  get containers(): UserContainerWithElements[] {
    return this.userStore.elementsByContainer()?.containers || [];
  }

  saveAssignments(): void {
    const publicId = this.userPublicId();
    if (publicId) {
      this.userStore.assignElements({
        userId: publicId,
        request: { elementPublicIds: this.selectedElementPublicIds() }
      });
      
      setTimeout(() => {
        if (!this.userStore.shouldRedirectToLogin()) {
          this.router.navigate(['/system/settings/users']);
        }
      }, 1500);
    }
  }

  cancel(): void {
    this.router.navigate(['/system/settings/users']);
  }
}

