-- =============================================
-- Insert: Initial Calendar Events Data
-- =============================================

-- 1. Insert the event types
-- =============================================
IF NOT EXISTS (SELECT 1 FROM app.tbl_calendar_event_types WHERE code = 'HOLIDAY')
BEGIN
    INSERT INTO app.tbl_calendar_event_types (code, name, description, created_by)
    VALUES ('HOLIDAY', 'Feriado', 'Feriado nacional no laborable.', 'SYSTEM');
END
GO

IF NOT EXISTS (SELECT 1 FROM app.tbl_calendar_event_types WHERE code = 'INTERNAL_EVENT')
BEGIN
    INSERT INTO app.tbl_calendar_event_types (code, name, description, created_by)
    VALUES ('INTERNAL_EVENT', 'Evento Interno', 'Evento específico de la empresa (ej. aniversario, capacitación). ', 'SYSTEM');
END
GO

IF NOT EXISTS (SELECT 1 FROM app.tbl_calendar_event_types WHERE code = 'NON_WORKING_DAY')
BEGIN
    INSERT INTO app.tbl_calendar_event_types (code, name, description, created_by)
    VALUES ('NON_WORKING_DAY', 'Día no Laborable', 'Día puente o día no laborable decretado por el gobierno.', 'SYSTEM');
END
GO

-- 2. Populate the work calendar for 2025 and 2026
-- =============================================
DECLARE @StartDate DATE = '2025-01-01';
DECLARE @EndDate DATE = '2026-12-31'; -- Extended to 2026
DECLARE @Date DATE = @StartDate;

-- Loop to populate all days
WHILE @Date <= @EndDate
BEGIN
    -- Insert the day into the calendar if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM app.tbl_work_calendar WHERE date = @Date)
    BEGIN
        -- By default, only Sunday (1) is a non-working day
        DECLARE @IsWorkingDay BIT = CASE WHEN DATEPART(weekday, @Date) = 1 THEN 0 ELSE 1 END;
        INSERT INTO app.tbl_work_calendar (public_id, date, is_working_day, created_by) VALUES (NEWID(), @Date, @IsWorkingDay, 'SYSTEM');
    END

    SET @Date = DATEADD(day, 1, @Date);
END
GO

-- 3. Define and insert Peruvian holidays for 2025
-- =============================================
DECLARE @HolidayEventTypeId SMALLINT;
SELECT @HolidayEventTypeId = id FROM app.tbl_calendar_event_types WHERE code = 'HOLIDAY';

DECLARE @Holidays TABLE (HolidayDate DATE, Description NVARCHAR(255));
INSERT INTO @Holidays (HolidayDate, Description)
VALUES
    ('2025-01-01', 'Año Nuevo'),
    ('2025-04-17', 'Jueves Santo'),
    ('2025-04-18', 'Viernes Santo'),
    ('2025-05-01', 'Día del Trabajo'),
    ('2025-06-29', 'San Pedro y San Pablo'),
    ('2025-07-28', 'Fiestas Patrias'),
    ('2025-07-29', 'Fiestas Patrias'),
    ('2025-08-30', 'Santa Rosa de Lima'),
    ('2025-10-08', 'Combate de Angamos'),
    ('2025-11-01', 'Día de Todos los Santos'),
    ('2025-12-08', 'Inmaculada Concepción'),
    ('2025-12-25', 'Navidad');

DECLARE @HolidayDate DATE;
DECLARE @HolidayDescription NVARCHAR(255);
DECLARE @WorkCalendarId INT;

DECLARE holiday_cursor CURSOR FOR
SELECT HolidayDate, Description FROM @Holidays;

OPEN holiday_cursor;
FETCH NEXT FROM holiday_cursor INTO @HolidayDate, @HolidayDescription;

WHILE @@FETCH_STATUS = 0
BEGIN
    -- Get the ID from the work calendar
    SELECT @WorkCalendarId = id FROM app.tbl_work_calendar WHERE date = @HolidayDate AND deleted_at IS NULL;

    IF @WorkCalendarId IS NOT NULL
    BEGIN
        -- Mark the day as a non-working day
        UPDATE app.tbl_work_calendar
        SET is_working_day = 0, updated_by = 'SYSTEM', updated_at = GETUTCDATE()
        WHERE id = @WorkCalendarId;

        -- Insert the event, if it doesn't already exist for that day and type
        IF NOT EXISTS (SELECT 1 FROM app.tbl_calendar_events WHERE work_calendar_id = @WorkCalendarId AND event_type_id = @HolidayEventTypeId)
        BEGIN
            INSERT INTO app.tbl_calendar_events (public_id, work_calendar_id, event_type_id, description, created_by)
            VALUES (NEWID(), @WorkCalendarId, @HolidayEventTypeId, @HolidayDescription, 'SYSTEM');
        END
    END

    FETCH NEXT FROM holiday_cursor INTO @HolidayDate, @HolidayDescription;
END

CLOSE holiday_cursor;
DEALLOCATE holiday_cursor;
GO
