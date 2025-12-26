import { Component, inject, signal } from '@angular/core';
import { ProfileStore } from '../../core/store/profile.store';
import { Profile } from '@shared/types/profile';
import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { CommonModule } from '@angular/common';
import { ConfirmationService } from 'primeng/api';
import { Router } from '@angular/router';
import { ProfileCreateModal } from './components/create-modal/create-modal';
import { ProfileUpdateModal } from './components/update-modal/update-modal';
import { DateDisplayComponent } from '../../shared/dummys';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { TooltipModule } from 'primeng/tooltip';

@Component({
  selector: 'app-profiles',
  templateUrl: './profiles.html',
  styleUrls: ['./profiles.css'],
  standalone: true,
  imports: [
    TableModule,
    ButtonModule,
    InputTextModule,
    ToastModule,
    ConfirmDialogModule,
    CommonModule,
    ProfileCreateModal,
    ProfileUpdateModal,
    DateDisplayComponent,
    InputGroupModule,
    InputGroupAddonModule,
    TooltipModule
  ],
  providers: [ConfirmationService]
})
export class Profiles {
  protected store = inject(ProfileStore);
  private confirmationService = inject(ConfirmationService);
  private router = inject(Router);

  readonly profiles = this.store.profiles;
  readonly loading = this.store.loading;
  readonly totalRecords = this.store.totalElements;
  readonly isEmpty = this.store.isEmpty;

  isCreateModalVisible = signal(false);
  isUpdateModalVisible = signal(false);
  selectedProfile = signal<Profile | null>(null);

  constructor() {
    this.store.resetFilters();
    this.store.init();
  }

  onSearch(event: Event): void {
    const inputElement = event.target as HTMLInputElement;
    this.store.search(inputElement.value);
  }

  loadProfiles(event: TableLazyLoadEvent): void {
    this.store.onLazyLoad(event);
  }

  showCreateModal() {
    this.isCreateModalVisible.set(true);
  }

  hideCreateModal() {
    this.isCreateModalVisible.set(false);
  }

  showUpdateModal(profile: Profile) {
    this.selectedProfile.set(profile);
    this.isUpdateModalVisible.set(true);
  }

  hideUpdateModal() {
    this.isUpdateModalVisible.set(false);
    this.selectedProfile.set(null);
  }

  confirmDelete(profile: Profile) {
    this.confirmationService.confirm({
      message: `¿Estás seguro de que quieres eliminar el perfil ${profile.name}?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.store.delete(profile.publicId);
      }
    });
  }

  assignElements(profile: Profile) {
    this.router.navigate(['/system/settings/profiles', profile.publicId, 'assign-elements']);
  }
}

