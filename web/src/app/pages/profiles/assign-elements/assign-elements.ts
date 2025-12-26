import { Component, OnInit, inject, signal, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProfileStore } from '@core/store/profile.store';
import { ContainerStore } from '@core/store/container.store';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { ToastModule } from 'primeng/toast';
import { ProfileElementsByContainer, ContainerWithElements, ElementInfo } from '@shared/types/profile';

@Component({
  selector: 'app-assign-elements',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule, CheckboxModule, ToastModule],
  templateUrl: './assign-elements.html',
  styleUrl: './assign-elements.css'
})
export class AssignElementsComponent implements OnInit {
  readonly store = inject(ProfileStore);
  readonly containerStore = inject(ContainerStore);
  private router = inject(Router);
  private activatedRoute = inject(ActivatedRoute);

  selectedElementPublicIds = signal<string[]>([]);
  profilePublicId = signal<string | null>(null);
  profileName = signal<string>('');

  constructor() {
    effect(() => {
      const elementsByContainer = this.store.elementsByContainer();
      if (elementsByContainer) {
        const assignedIds: string[] = [];
        elementsByContainer.containers.forEach(container => {
          assignedIds.push(...container.selectedElementPublicIds);
        });
        this.selectedElementPublicIds.set(assignedIds);
        this.profileName.set(elementsByContainer.profileName);
      }
    }, { allowSignalWrites: true });
  }

  ngOnInit(): void {
    const publicId = this.activatedRoute.snapshot.paramMap.get('publicId');
    if (publicId) {
      this.profilePublicId.set(publicId);
      if (this.containerStore.containers().length === 0) {
        this.containerStore.init();
      }
      this.store.loadElementsByContainer(publicId);
    } else {
      this.router.navigate(['/system/settings/profiles']);
    }
  }

  get containers(): ContainerWithElements[] {
    return this.store.elementsByContainer()?.containers || [];
  }


  saveAssignments(): void {
    const publicId = this.profilePublicId();
    if (publicId) {
      this.store.assignElements({
        publicId,
        request: { elementPublicIds: this.selectedElementPublicIds() }
      });
      setTimeout(() => {
        this.router.navigate(['/system/settings/profiles']);
      }, 1000);
    }
  }

  cancel(): void {
    this.router.navigate(['/system/settings/profiles']);
  }
}

