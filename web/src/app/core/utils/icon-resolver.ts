export function resolvePrimeIcon(iconName: string | null | undefined): string {
  if (!iconName) {
    return 'pi pi-circle';
  }

  const normalized = iconName
    .trim()
    .toLowerCase()
    .replace(/^pi\s+pi-/, '') // Remover "pi pi-"
    .replace(/^pi-/, '')
    .replace(/\s+/g, '-');

  if (!normalized) {
    return 'pi pi-circle';
  }

  return `pi pi-${normalized}`;
}

const iconAliases: Record<string, string> = {
  'user': 'user',
  'users': 'users',
  'person': 'user',
  'people': 'users',
  'home': 'home',
  'house': 'home',
  'file': 'file',
  'document': 'file',
  'folder': 'folder',
  'folders': 'folders',
  'trash': 'trash',
  'delete': 'trash',
  'edit': 'pencil',
  'pencil': 'pencil',
  'save': 'save',
  'print': 'print',
  'download': 'download',
  'upload': 'upload',
  'sync': 'sync',
  'refresh': 'refresh',
  'settings': 'cog',
  'gear': 'cog',
  'cog': 'cog',
  'search': 'search',
  'plus': 'plus',
  'add': 'plus',
  'check': 'check',
  'times': 'times',
  'close': 'times',
  'x': 'times',
  'info': 'info-circle',
  'warning': 'exclamation-triangle',
  'lock': 'lock',
  'key': 'key',
  'unlock': 'unlock',
  'eye': 'eye',
  'eye-slash': 'eye-slash',
  'visibility': 'eye',
  'visibility-off': 'eye-slash',
  
  'chart-line': 'chart-line',
  'sitemap': 'sitemap',
  'file-edit': 'file-edit',
  'clock': 'clock',
  'wallet': 'wallet',
  'shield': 'shield',
  
  'warehouse': 'warehouse',
  'objects-column': 'objects-column',
  'globe': 'globe',
  'briefcase': 'briefcase',
  'copy': 'copy',
  'file-plus': 'file-plus',
  'th-large': 'th-large',
  'qrcode': 'qrcode',
  'sign-in': 'sign-in',
  'sign-out': 'sign-out',
  'file-excel': 'file-excel',
  'calendar-plus': 'calendar-plus',
  'calendar': 'calendar',
  'code': 'code',
  'building': 'building',
  'id-card': 'id-card',
};

export function resolvePrimeIconWithAlias(iconName: string | null | undefined): string {
  if (!iconName) {
    return 'pi pi-circle';
  }

  const normalized = iconName
    .trim()
    .toLowerCase()
    .replace(/^pi\s+pi-/, '')
    .replace(/^pi-/, '')
    .replace(/\s+/g, '-');

  if (!normalized) {
    return 'pi pi-circle';
  }

  const aliased = iconAliases[normalized] || normalized;

  return `pi pi-${aliased}`;
}

