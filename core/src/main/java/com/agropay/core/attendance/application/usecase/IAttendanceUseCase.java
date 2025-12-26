package com.agropay.core.attendance.application.usecase;

import com.agropay.core.attendance.model.attendance.AttendanceListDTO;
import com.agropay.core.attendance.model.attendance.AttendanceSummaryDTO;
import com.agropay.core.attendance.model.attendance.AttendanceCountSummaryDTO;
import com.agropay.core.attendance.model.attendance.EmployeeAttendanceCheckDTO;
import com.agropay.core.shared.utils.PagedResult;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Use case interface for attendance consultation and reporting.
 * Provides methods to query attendance data for different purposes including
 * employee attendance checks for future tareo functionality.
 */
public interface IAttendanceUseCase {

    /**
     * Get paginated attendance list for a specific date
     * @param markingDate Date to filter by (required)
     * @param subsidiaryPublicId Optional subsidiary filter
     * @param personDocumentNumber Optional person document filter
     * @param isEmployee Optional filter for employee vs external person
     * @param pageable Pagination parameters
     * @return Paginated list of attendance records
     */
    PagedResult<AttendanceListDTO> getAttendanceList(
        LocalDate markingDate,
        UUID subsidiaryPublicId,
        String personDocumentNumber,
        Boolean isEmployee,
        Pageable pageable
    );

    /**
     * Get attendance list filtered to employees only
     * @param markingDate Date to filter by (required)
     * @param subsidiaryPublicId Optional subsidiary filter
     * @param personDocumentNumber Optional person document filter
     * @param pageable Pagination parameters
     * @return Paginated list of employee attendance records
     */
    PagedResult<AttendanceListDTO> getEmployeeAttendanceList(
        LocalDate markingDate,
        UUID subsidiaryPublicId,
        String personDocumentNumber,
        Pageable pageable
    );

    /**
     * Get attendance list filtered to external persons only
     * @param markingDate Date to filter by (required)
     * @param subsidiaryPublicId Optional subsidiary filter
     * @param personDocumentNumber Optional person document filter
     * @param pageable Pagination parameters
     * @return Paginated list of external person attendance records
     */
    PagedResult<AttendanceListDTO> getPersonAttendanceList(
        LocalDate markingDate,
        UUID subsidiaryPublicId,
        String personDocumentNumber,
        Pageable pageable
    );

    /**
     * Check if an employee has marked attendance for a specific date and subsidiary.
     * This method is specifically designed for future tareo functionality.
     * Validates that the employee belongs to the specified subsidiary.
     * @param employeeDocumentNumber Employee's document number
     * @param subsidiaryPublicId Subsidiary public ID to validate employee belongs to
     * @param checkDate Date to check attendance for
     * @return EmployeeAttendanceCheckDTO with attendance status and details
     */
    EmployeeAttendanceCheckDTO checkEmployeeAttendanceByDate(
        String employeeDocumentNumber,
        UUID subsidiaryPublicId,
        LocalDate checkDate
    );


    /**
     * Get flexible attendance summary for a specific subsidiary and date.
     * Provides simplified counts (inside, outside, total) with optional filtering
     * by person type (employee vs external). Uses dynamic filtering with Specification.
     * @param subsidiaryPublicId Subsidiary public ID
     * @param summaryDate Date to get summary for
     * @param isExternal Optional filter - true for externals only, false for employees only, null for both
     * @return AttendanceCountSummaryDTO with simplified counts
     */
    AttendanceCountSummaryDTO getFlexibleAttendanceSummary(
        UUID subsidiaryPublicId,
        LocalDate summaryDate,
        Boolean isExternal
    );
}