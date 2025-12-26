package com.agropay.core.payroll.service;

import com.agropay.core.auth.domain.UserEntity;
import com.agropay.core.auth.persistence.IUserRepository;
import com.agropay.core.organization.domain.EmployeeEntity;
import com.agropay.core.organization.domain.PersonEntity;
import com.agropay.core.payroll.domain.PayrollDetailEntity;
import com.agropay.core.payroll.domain.PayrollEntity;
import com.agropay.core.payroll.model.payslip.PayslipListDTO;
import com.agropay.core.payroll.model.payslip.PayslipPageableRequest;
import com.agropay.core.payroll.persistence.IPayrollDetailRepository;
import com.agropay.core.payroll.persistence.specification.PayslipSpecifications;
import com.agropay.core.shared.utils.PagedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayslipService {

    private final IPayrollDetailRepository payrollDetailRepository;
    private final IUserRepository userRepository;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-PE"));

    /**
     * Obtiene el documento del empleado asociado al usuario autenticado.
     * Si el usuario es admin (sin employeeId), retorna null.
     */
    private String getCurrentUserEmployeeDocumentNumber() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authentication found in security context");
            return null;
        }

        String username = authentication.getName();
        log.debug("Getting employee document number for username: {}", username);
        
        UserEntity user = userRepository.findByUsername(username).orElse(null);
        
        if (user == null) {
            log.warn("User not found for username: {}", username);
            return null;
        }

        String employeeId = user.getEmployeeId();
        log.debug("User {} has employeeId: {}", username, employeeId);
        
        // Si el usuario tiene employeeId, retornarlo. Si es null, es admin.
        return employeeId;
    }

    @Transactional(readOnly = true)
    public PagedResult<PayslipListDTO> listPayslips(PayslipPageableRequest request) {
        log.info("Listing payslips with filters: periodFrom={}, periodTo={}, employeeDocumentNumber={}",
            request.getPeriodFrom(), request.getPeriodTo(), request.getEmployeeDocumentNumber());

        // Obtener el documento del empleado del usuario autenticado
        String currentUserEmployeeDoc = getCurrentUserEmployeeDocumentNumber();
        log.info("Current user employee document number: {}", currentUserEmployeeDoc);
        
        // Si el usuario es admin (sin employeeId), puede ver todas las boletas
        // Si el usuario tiene employeeId, solo puede ver sus propias boletas
        String employeeDocumentNumber = currentUserEmployeeDoc;
        
        // Si el request tiene un employeeDocumentNumber específico y el usuario es admin, usarlo
        if (currentUserEmployeeDoc == null && request.getEmployeeDocumentNumber() != null) {
            employeeDocumentNumber = request.getEmployeeDocumentNumber();
            log.info("Using employeeDocumentNumber from request (admin user): {}", employeeDocumentNumber);
        } else {
            log.info("Using employeeDocumentNumber from authenticated user: {}", employeeDocumentNumber);
        }

        // Crear especificación con filtros
        Specification<PayrollDetailEntity> spec = PayslipSpecifications.from(request, employeeDocumentNumber);

        // Crear Pageable con ordenamiento (usar el método toPageable de BasePageableRequest)
        Pageable pageable = request.toPageable();

        // Ejecutar consulta paginada y mapear a DTOs
        Page<PayrollDetailEntity> page = payrollDetailRepository.findAll(spec, pageable);
        Page<PayslipListDTO> payslipPage = page.map(this::toPayslipListDTO);
        return new PagedResult<>(payslipPage);
    }

    /**
     * Obtiene los detalles de una boleta por su publicId
     */
    @Transactional(readOnly = true)
    public PayslipDetailInfo getPayslipDetail(UUID payslipPublicId) {
        PayrollDetailEntity detail = payrollDetailRepository.findByPublicId(payslipPublicId)
            .orElseThrow(() -> new com.agropay.core.shared.exceptions.IdentifierNotFoundException(
                "exception.payroll.payslip.not-found", payslipPublicId.toString()));

        // Validar permisos: si el usuario tiene empleado asociado, solo puede ver sus propias boletas
        String currentUserEmployeeDoc = getCurrentUserEmployeeDocumentNumber();
        if (currentUserEmployeeDoc != null && !currentUserEmployeeDoc.equals(detail.getEmployee().getPersonDocumentNumber())) {
            throw new com.agropay.core.shared.exceptions.BusinessValidationException(
                "exception.payroll.payslip.access-denied");
        }

        PayrollEntity payroll = detail.getPayroll();
        EmployeeEntity employee = detail.getEmployee();

        return new PayslipDetailInfo(
            payroll != null ? payroll.getPublicId() : null,
            employee != null ? employee.getPersonDocumentNumber() : null
        );
    }

    public static record PayslipDetailInfo(UUID payrollPublicId, String employeeDocumentNumber) {}

    /**
     * Obtiene la entidad completa de PayrollDetail con validación de permisos
     */
    @Transactional(readOnly = true)
    public PayrollDetailEntity getPayrollDetailEntity(UUID payslipPublicId) {
        PayrollDetailEntity detail = payrollDetailRepository.findByPublicId(payslipPublicId)
            .orElseThrow(() -> new com.agropay.core.shared.exceptions.IdentifierNotFoundException(
                "exception.payroll.payslip.not-found", payslipPublicId.toString()));

        // Validar permisos: si el usuario tiene empleado asociado, solo puede ver sus propias boletas
        String currentUserEmployeeDoc = getCurrentUserEmployeeDocumentNumber();
        if (currentUserEmployeeDoc != null && !currentUserEmployeeDoc.equals(detail.getEmployee().getPersonDocumentNumber())) {
            throw new com.agropay.core.shared.exceptions.BusinessValidationException(
                "exception.payroll.payslip.access-denied");
        }

        return detail;
    }

    private PayslipListDTO toPayslipListDTO(PayrollDetailEntity detail) {
        PayrollEntity payroll = detail.getPayroll();
        EmployeeEntity employee = detail.getEmployee();
        PersonEntity person = employee != null ? employee.getPerson() : null;

        String employeeNames = person != null ? person.getNames() : null;
        String employeePaternalLastname = person != null ? person.getPaternalLastname() : null;
        String employeeMaternalLastname = person != null ? person.getMaternalLastname() : null;
        
        String employeeFullName = String.format("%s %s %s",
            employeeNames != null ? employeeNames : "",
            employeePaternalLastname != null ? employeePaternalLastname : "",
            employeeMaternalLastname != null ? employeeMaternalLastname : ""
        ).trim();

        String periodName = payroll != null && payroll.getPeriodStart() != null
            ? payroll.getPeriodStart().format(MONTH_FORMATTER)
            : null;

        String subsidiaryName = employee != null && employee.getSubsidiary() != null
            ? employee.getSubsidiary().getName()
            : null;

        return new PayslipListDTO(
            detail.getPublicId(),
            payroll != null ? payroll.getPublicId() : null,
            payroll != null ? payroll.getCode() : null,
            employee != null ? employee.getPersonDocumentNumber() : null,
            employeeNames,
            employeeFullName,
            subsidiaryName,
            periodName,
            payroll != null ? payroll.getPeriodStart() : null,
            payroll != null ? payroll.getPeriodEnd() : null,
            detail.getTotalIncome(),
            detail.getTotalDeductions(),
            detail.getNetToPay(),
            detail.getCreatedAt(),
            detail.getPayslipPdfUrl() // URL del PDF almacenado
        );
    }
}

