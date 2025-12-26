import { Component, effect, EventEmitter, inject, Input, Output, signal, computed, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CreateUserRequest } from '@shared/types/security';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';
import { UserStore } from '@core/store/user.store';
import { PositionStore } from '@core/store/position.store';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { SelectModule } from 'primeng/select';

@Component({
  selector: 'app-create-user-modal',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    ModalTemplateComponent,
    InputTextModule,
    PasswordModule,
    SelectModule
  ],
  templateUrl: './create-user-modal.html',
  styleUrl: './create-user-modal.css'
})
export class CreateUserModal implements OnInit {
  @Input() visible: boolean = false;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected userStore = inject(UserStore);
  protected positionStore = inject(PositionStore);

  private isCreating = signal(false);

  positionOptions = computed(() => {
    return this.positionStore.positionSelectOptionsAll().map(position => ({
      label: position.name,
      value: position.publicId
    }));
  });

  userForm = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    positionId: [null as string | null],
    employeeId: [null as string | null]
  });

  constructor() {
    effect(() => {
      if (!this.isCreating()) {
        return;
      }

      if (!this.userStore.loading()) {
        if (!this.userStore.error()) {
          this.hideModal();
        }
        this.isCreating.set(false);
      }
    });

    effect(() => {
      if (this.visible) {
        this.positionStore.init();
      }
    });
  }

  ngOnInit(): void {
    if (this.visible) {
      this.positionStore.init();
    }
  }

  hideModal() {
    this.onHide.emit();
    this.userForm.reset();
  }

  createUser() {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    const request: CreateUserRequest = {
      username: this.userForm.value.username!,
      password: this.userForm.value.password!,
      positionId: this.userForm.value.positionId || undefined,
      employeeId: this.userForm.value.employeeId || undefined
    };

    this.isCreating.set(true);
    this.userStore.createUser(request);
  }
}