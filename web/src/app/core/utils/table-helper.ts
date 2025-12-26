export class TableHelper {
  static processPaginationEvent(event: any, defaultSortField: string = 'createdAt') {
    const { first, rows, sortField, sortOrder } = event;

    return {
      page: first / rows,
      pageSize: rows,
      sortField: sortField || defaultSortField,
      sortDirection: sortOrder === 1 ? 'ASC' : 'DESC'
    };
  }

  static processSortField(sortField: string | null | undefined, defaultSortField: string = 'createdAt'): string {
    return sortField || defaultSortField;
  }
}