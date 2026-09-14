package com.abdel.employee_management.service.implementation;

import com.abdel.employee_management.model.Employee;
import com.abdel.employee_management.repository.EmployeeRepository;
import com.abdel.employee_management.security.PermissionChecker;
import com.abdel.employee_management.service.ContractService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class ContractServiceImpl implements ContractService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PermissionChecker permissionChecker;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color BRAND_COLOR = new Color(42, 63, 115); // matches frontend primary
    private static final Color LIGHT_GRAY = new Color(245, 246, 248);
    private static final Color TEXT_MUTED = new Color(91, 100, 114);

    private static final String COMPANY_NAME = "Techno-Solutions";
    private static final String COMPANY_ADDRESS = "123 Innovation Avenue, Tangier, Morocco";

    @Override
    public byte[] generateContract(Long employeeId) {
        permissionChecker.checkRead();

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 50, 50, 40, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            Font companyFont = new Font(Font.HELVETICA, 16, Font.BOLD, BRAND_COLOR);
            Font addressFont = new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_MUTED);
            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, Color.BLACK);
            Font metaFont = new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_MUTED);
            Font sectionFont = new Font(Font.HELVETICA, 12, Font.BOLD, BRAND_COLOR);
            Font labelFont = new Font(Font.HELVETICA, 9, Font.BOLD, TEXT_MUTED);
            Font valueFont = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.BLACK);
            Font bodyFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
            Font signLineFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
            Font signLabelFont = new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_MUTED);

            // --- Letterhead ---
            Paragraph company = new Paragraph(COMPANY_NAME, companyFont);
            document.add(company);
            Paragraph address = new Paragraph(COMPANY_ADDRESS, addressFont);
            address.setSpacingAfter(20);
            document.add(address);

            // Thin brand-colored rule under the letterhead
            document.add(horizontalRule());
            document.add(spacer(20));

            // --- Title + reference ---
            Paragraph title = new Paragraph("EMPLOYMENT CONTRACT", titleFont);
            document.add(title);

            String reference = "REF-" + employeeId + "-" + LocalDate.now().getYear();
            Paragraph meta = new Paragraph(
                    "Reference: " + reference + "   |   Issued: " + LocalDate.now().format(DATE_FORMAT),
                    metaFont
            );
            meta.setSpacingAfter(20);
            document.add(meta);

            // --- Intro paragraph ---
            String fullName = employee.getFirstName() + " " + employee.getLastName();
            Paragraph intro = new Paragraph(
                    "This Employment Contract (\"Contract\") is made and entered into between " + COMPANY_NAME +
                            " (\"the Company\") and " + fullName + " (\"the Employee\"), and sets forth the terms " +
                            "and conditions of the Employee's employment as detailed below.",
                    bodyFont
            );
            intro.setSpacingAfter(20);
            document.add(intro);

            // --- Employee details section ---
            Paragraph sectionOne = new Paragraph("1. Employee Details", sectionFont);
            sectionOne.setSpacingAfter(10);
            document.add(sectionOne);

            document.add(buildDetailsTable(employee));
            document.add(spacer(20));

            // --- Terms & Conditions ---
            Paragraph sectionTwo = new Paragraph("2. Terms and Conditions", sectionFont);
            sectionTwo.setSpacingAfter(10);
            document.add(sectionTwo);

            document.add(numberedClause("2.1", "Position",
                    "The Employee is engaged in the position of " + nullSafe(employee.getJobTitle()) +
                            " within the " + nullSafe(employee.getDepartment()) + " department, reporting to their designated supervisor.",
                    bodyFont, labelFont));

            document.add(numberedClause("2.2", "Commencement",
                    "This Contract takes effect on " + formatDate(employee.getHireDate()) +
                            " and shall continue until terminated in accordance with the terms herein.",
                    bodyFont, labelFont));

            document.add(numberedClause("2.3", "Compensation",
                    "The Employee shall receive a gross salary as specified in Section 1, payable on a monthly basis, " +
                            "subject to applicable statutory deductions.",
                    bodyFont, labelFont));

            document.add(numberedClause("2.4", "Working Hours",
                    "The Employee agrees to work the standard hours established by Company policy, currently set " +
                            "at 40 hours per week, subject to reasonable variation as business needs require.",
                    bodyFont, labelFont));

            document.add(numberedClause("2.5", "Confidentiality",
                    "The Employee agrees to maintain strict confidentiality regarding all proprietary Company " +
                            "information, both during and after the term of employment.",
                    bodyFont, labelFont));

            document.add(numberedClause("2.6", "Termination",
                    "Either party may terminate this Contract by providing written notice in accordance with " +
                            "applicable labor law and Company policy.",
                    bodyFont, labelFont));

            document.add(spacer(30));

            // --- Closing statement ---
            Paragraph closing = new Paragraph(
                    "By signing below, both parties acknowledge that they have read, understood, and agree to be " +
                            "bound by the terms of this Contract.",
                    bodyFont
            );
            closing.setSpacingAfter(40);
            document.add(closing);

            // --- Signature block ---
            document.add(buildSignatureBlock(signLineFont, signLabelFont, fullName));

            document.close();
            return out.toByteArray();

        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate contract: " + e.getMessage(), e);
        }
    }

    private PdfPTable buildDetailsTable(Employee employee) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        try {
            table.setWidths(new float[]{1f, 1f});
        } catch (DocumentException ignored) {
        }

        Font labelFont = new Font(Font.HELVETICA, 9, Font.BOLD, TEXT_MUTED);
        Font valueFont = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.BLACK);

        addDetailCell(table, "Full Name", employee.getFirstName() + " " + employee.getLastName(), labelFont, valueFont);
        addDetailCell(table, "Job Title", nullSafe(employee.getJobTitle()), labelFont, valueFont);
        addDetailCell(table, "Department", nullSafe(employee.getDepartment()), labelFont, valueFont);
        addDetailCell(table, "Hire Date", formatDate(employee.getHireDate()), labelFont, valueFont);
        addDetailCell(table, "Email", nullSafe(employee.getEmail()), labelFont, valueFont);
        addDetailCell(table, "Monthly Salary", formatSalary(employee.getSalary()), labelFont, valueFont);

        return table;
    }

    private void addDetailCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(LIGHT_GRAY);
        cell.setPadding(10);
        cell.setBorderColor(Color.WHITE);
        cell.setBorderWidth(4);

        Paragraph content = new Paragraph();
        content.add(new Chunk(label.toUpperCase() + "\n", labelFont));
        content.add(new Chunk(value, valueFont));
        cell.addElement(content);

        table.addCell(cell);
    }

    private Paragraph numberedClause(String number, String label, String text, Font bodyFont, Font labelFont) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(number + "  ", labelFont));
        p.add(new Chunk(label + " — ", new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK)));
        p.add(new Chunk(text, bodyFont));
        p.setSpacingAfter(10);
        p.setAlignment(Element.ALIGN_JUSTIFIED);
        return p;
    }

    private PdfPTable buildSignatureBlock(Font lineFont, Font labelFont, String employeeName) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        try {
            table.setWidths(new float[]{1f, 1f});
        } catch (DocumentException ignored) {
        }

        table.addCell(signatureCell("_______________________________", "For " + COMPANY_NAME, "Authorized Signatory", lineFont, labelFont));
        table.addCell(signatureCell("_______________________________", employeeName, "Employee Signature", lineFont, labelFont));

        return table;
    }

    private PdfPCell signatureCell(String line, String name, String role, Font lineFont, Font labelFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(10);

        Paragraph p = new Paragraph();
        p.add(new Chunk(line + "\n", lineFont));
        p.add(new Chunk(name + "\n", new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK)));
        p.add(new Chunk(role, labelFont));
        cell.addElement(p);

        return cell;
    }

    private PdfPTable horizontalRule() {
        PdfPTable rule = new PdfPTable(1);
        rule.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setFixedHeight(2f);
        cell.setBackgroundColor(BRAND_COLOR);
        cell.setBorder(Rectangle.NO_BORDER);
        rule.addCell(cell);
        return rule;
    }

    private Paragraph spacer(float height) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(height);
        return p;
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "N/A";
    }

    private String formatSalary(java.math.BigDecimal salary) {
        return salary != null ? String.format("%,.2f MAD", salary) : "N/A";
    }

    private String nullSafe(String value) {
        return value != null ? value : "N/A";
    }
}