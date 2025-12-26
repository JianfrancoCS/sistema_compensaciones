import { Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';

export type DateType = 'created' | 'updated' | 'deleted';
export type DateVariant = 'full' | 'table' | 'simple';

@Component({
  selector: 'app-date-display',
  template: `
    @if (variant() === 'full') {
      <div [class]="containerClasses()">
        <div class="flex items-center gap-2">
          <i [class]="iconClasses()"></i>
          <div class="flex flex-col">
            <span class="text-xs font-medium opacity-80">{{ typeLabel() }}</span>
            <span class="font-semibold">{{ formattedDate() }}</span>
          </div>
        </div>
      </div>
    }

    @if (variant() === 'table') {
      <div [class]="containerClasses()">
        <span class="font-medium">{{ formattedDate() }}</span>
      </div>
    }

    @if (variant() === 'simple') {
      <span class="font-medium text-gray-700">{{ formattedDate() }}</span>
    }
  `,
  standalone: true,
  imports: [CommonModule]
})
export class DateDisplayComponent {
  date = input.required<string>();
  type = input<DateType>('created');
  variant = input<DateVariant>('full');
  size = input<'sm' | 'md' | 'lg'>('md');

  formattedDate = computed(() => {
    if (!this.date()) return '';

    const dateObj = new Date(this.date());

    const day = dateObj.getDate().toString().padStart(2, '0');
    const month = (dateObj.getMonth() + 1).toString().padStart(2, '0');
    const year = dateObj.getFullYear();

    const hours24 = dateObj.getHours();
    const minutes = dateObj.getMinutes().toString().padStart(2, '0');
    const hours12 = hours24 % 12 || 12;
    const ampm = hours24 >= 12 ? 'PM' : 'AM';

    return `${day}/${month}/${year} ${hours12}:${minutes} ${ampm}`;
  });

  typeLabel = computed(() => {
    switch (this.type()) {
      case 'created': return 'Creado';
      case 'updated': return 'Actualizado';
      case 'deleted': return 'Eliminado';
      default: return 'Fecha';
    }
  });

  containerClasses = computed(() => {
    const baseClasses = 'inline-flex rounded-lg border';

    const fullSizeClasses = {
      sm: 'px-2 py-1 text-xs',
      md: 'px-3 py-2 text-sm',
      lg: 'px-4 py-3 text-base'
    };

    const tableSizeClasses = {
      sm: 'px-2 py-1 text-xs',
      md: 'px-2 py-1 text-xs',
      lg: 'px-3 py-2 text-sm'
    };

    const sizeClasses = this.variant() === 'full' ? fullSizeClasses[this.size()] : tableSizeClasses[this.size()];

    const typeClasses = {
      created: 'bg-sky-50 border-sky-200 text-sky-800',
      updated: 'bg-orange-50 border-orange-200 text-orange-800',
      deleted: 'bg-red-50 border-red-200 text-red-800'
    };

    return `${baseClasses} ${sizeClasses} ${typeClasses[this.type()]}`;
  });

  iconClasses = computed(() => {
    const baseClasses = 'text-sm';

    const typeClasses = {
      created: 'pi pi-plus-circle text-sky-600',
      updated: 'pi pi-refresh text-orange-600',
      deleted: 'pi pi-trash text-red-600'
    };

    return `${baseClasses} ${typeClasses[this.type()]}`;
  });
}
