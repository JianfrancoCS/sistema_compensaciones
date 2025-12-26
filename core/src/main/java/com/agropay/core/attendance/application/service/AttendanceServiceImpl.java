package com.agropay.core.attendance.application.service;

import com.agropay.core.attendance.application.usecase.IAttendanceUseCase;
import com.agropay.core.attendance.domain.MarkingDetailEntity;
import com.agropay.core.shared.exceptions.BusinessValidationException;
import com.agropay.core.attendance.mapper.IAttendanceMapper;
import com.agropay.core.attendance.model.attendance.AttendanceListDTO;
import com.agropay.core.attendance.model.attendance.AttendanceSummaryDTO;
import com.agropay.core.attendance.model.attendance.AttendanceCountSummaryDTO;
import com.agropay.core.attendance.model.attendance.EmployeeAttendanceCheckDTO;
import com.agropay.core.attendance.persistence.IMarkingDetailRepository;
import com.agropay.core.attendance.persistence.AttendanceSpecification;
import com.agropay.core.organization.api.IEmployeeAPI;
import com.agropay.core.organization.api.IPersonAPI;
import com.agropay.core.organization.api.ISubsidiaryAPI;
import com.agropay.core.organization.domain.EmployeeEntity;
import com.agropay.core.organization.domain.PersonEntity;
import com.agropay.core.organization.domain.SubsidiaryEntity;
import com.agropay.core.shared.utils.PagedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceServiceImpl implements IAttendanceUseCase {

    private final IMarkingDetailRepository markingDetailRepository;
    private final IAttendanceMapper attendanceMapper;
    private final IPersonAPI personAPI;
    private final IEmployeeAPI employeeAPI;
    private final ISubsidiaryAPI subsidiaryAPI;

    @Override
    public PagedResult<AttendanceListDTO> getAttendanceList(
            LocalDate markingDate,
            UUID subsidiaryPublicId,
            String personDocumentNumber,
            Boolean isEmployee,
            Pageable pageable
    ) {
        log.debug("Getting attendance list for date: {}, subsidiary: {}, document: {}, isEmployee: {}",
                markingDate, subsidiaryPublicId, personDocumentNumber, isEmployee);

        Specification<MarkingDetailEntity> spec = AttendanceSpecification.buildSpecification(
                markingDate, subsidiaryPublicId, personDocumentNumber, isEmployee
        );

        Page<MarkingDetailEntity> markingDetailsPage = markingDetailRepository.findAll(spec, pageable);

        List<AttendanceListDTO> attendanceList = processAttendanceMarkings(markingDetailsPage.getContent());

        return new PagedResult<>(
                attendanceList,
                markingDetailsPage.getTotalElements(),
                markingDetailsPage.getNumber(),
                markingDetailsPage.getTotalPages(),
                markingDetailsPage.isFirst(),
                markingDetailsPage.isLast(),
                markingDetailsPage.hasNext(),
                markingDetailsPage.hasPrevious()
        );
    }

    @Override
    public PagedResult<AttendanceListDTO> getEmployeeAttendanceList(
            LocalDate markingDate,
            UUID subsidiaryPublicId,
            String personDocumentNumber,
            Pageable pageable
    ) {
        log.debug("Getting employee attendance list for date: {}", markingDate);
        return getAttendanceList(markingDate, subsidiaryPublicId, personDocumentNumber, true, pageable);
    }

    @Override
    public PagedResult<AttendanceListDTO> getPersonAttendanceList(
            LocalDate markingDate,
            UUID subsidiaryPublicId,
            String personDocumentNumber,
            Pageable pageable
    ) {
        log.debug("Getting person attendance list for date: {}", markingDate);
        return getAttendanceList(markingDate, subsidiaryPublicId, personDocumentNumber, false, pageable);
    }

    @Override
    public EmployeeAttendanceCheckDTO checkEmployeeAttendanceByDate(
            String employeeDocumentNumber,
            UUID subsidiaryPublicId,
            LocalDate checkDate
    ) {
        log.debug("Checking employee attendance for document: {} in subsidiary: {} on date: {}",
                employeeDocumentNumber, subsidiaryPublicId, checkDate);

        Optional<EmployeeEntity> employeeOpt = employeeAPI.findByPersonDocumentNumber(employeeDocumentNumber);
        if (employeeOpt.isEmpty()) {
            throw new BusinessValidationException(employeeDocumentNumber, "No encontrado");
        }

        EmployeeEntity employee = employeeOpt.get();

        SubsidiaryEntity subsidiary = subsidiaryAPI.findByPublicId(subsidiaryPublicId);

        if (!employee.getSubsidiary().getPublicId().equals(subsidiaryPublicId)) {
            throw new BusinessValidationException(
                employeeDocumentNumber,
                subsidiary.getName()
            );
        }

        PersonEntity person = employee.getPerson();

        Specification<MarkingDetailEntity> spec = AttendanceSpecification.buildSpecification(
                checkDate, subsidiaryPublicId, employeeDocumentNumber, true
        );

        List<MarkingDetailEntity> markings = markingDetailRepository.findAll(spec);

        LocalDateTime entryTime = null;
        LocalDateTime exitTime = null;

        for (MarkingDetailEntity marking : markings) {
            if (marking.getIsEntry()) {
                entryTime = marking.getMarkedAt();
            } else {
                exitTime = marking.getMarkedAt();
            }
        }

        boolean hasAttendance = !markings.isEmpty();
        boolean hasEntry = entryTime != null;
        boolean hasExit = exitTime != null;

        return EmployeeAttendanceCheckDTO.builder()
                .hasAttendance(hasAttendance)
                .hasEntry(hasEntry)
                .hasExit(hasExit)
                .checkDate(checkDate)
                .entryTime(entryTime)
                .exitTime(exitTime)
                .personDocumentNumber(employeeDocumentNumber)
                .personFullName(person.getNames() + " " + person.getPaternalLastname()+" "+person.getMaternalLastname())
                .build();
    }

    @Override
    public AttendanceCountSummaryDTO getFlexibleAttendanceSummary(
            UUID subsidiaryPublicId,
            LocalDate summaryDate,
            Boolean isExternal
    ) {
        log.debug("Getting flexible attendance summary for subsidiary: {} on date: {} with isExternal filter: {}",
                subsidiaryPublicId, summaryDate, isExternal);

        SubsidiaryEntity subsidiary = subsidiaryAPI.findByPublicId(subsidiaryPublicId);

        Boolean isEmployee = isExternal != null ? !isExternal : null;

        Long totalEntries = markingDetailRepository.countFlexibleBySubsidiaryAndDateAndIsEntry(
                subsidiaryPublicId, summaryDate, true, isEmployee
        );

        Long totalExits = markingDetailRepository.countFlexibleBySubsidiaryAndDateAndIsEntry(
                subsidiaryPublicId, summaryDate, false, isEmployee
        );

        return AttendanceCountSummaryDTO.create(
                subsidiaryPublicId,
                subsidiary.getName(),
                summaryDate,
                totalEntries,
                totalExits
        );
    }

    private List<AttendanceListDTO> processAttendanceMarkings(List<MarkingDetailEntity> markingDetails) {
        var groupedMarkings = markingDetails.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        marking -> marking.getPersonDocumentNumber() + "_" + marking.getMarking().getMarkingDate()
                ));

        List<AttendanceListDTO> result = new java.util.ArrayList<>();

        for (var entry : groupedMarkings.entrySet()) {
            List<MarkingDetailEntity> personDayMarkings = entry.getValue();

            Optional<MarkingDetailEntity> entryMarking = personDayMarkings.stream()
                    .filter(MarkingDetailEntity::getIsEntry)
                    .findFirst();

            Optional<MarkingDetailEntity> exitMarking = personDayMarkings.stream()
                    .filter(marking -> !marking.getIsEntry())
                    .findFirst();

            MarkingDetailEntity baseMarking = personDayMarkings.get(0);

            PersonEntity person;
            try {
                person = personAPI.findOrCreatePersonByDni(baseMarking.getPersonDocumentNumber());
            } catch (Exception e) {
                log.warn("Could not find person with document: {}", baseMarking.getPersonDocumentNumber());
                continue;
            }

            AttendanceListDTO dto = attendanceMapper.toAttendanceListDTO(baseMarking);

            result.add(dto);
        }

        return result;
    }
}