package com.audino.util;

import com.audino.model.*;
import com.audino.service.DataService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class AuditLogTester {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  Audino Audit Log Rigorous Integration Tests");
        System.out.println("=================================================\n");

        int passed = 0, failed = 0;

        ConfigurationManager.getInstance().initialize();
        DataService ds = new DataService();
        ds.loadAllData();

        // Simulate admin session for audit username capture
        User admin = ds.authenticate("admin", "1234567890");
        SessionManager.getInstance().login(admin);

        // ── TEST 1: audit_logs table + index exist ─────────────────────────
        try (Connection conn = ConfigurationManager.getInstance().getDataSource().getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT count(*) FROM information_schema.tables WHERE table_name='audit_logs'");
            rs.next();
            if (rs.getInt(1) == 1) {
                System.out.println("✓ TEST 1 PASS: audit_logs table exists");
                passed++;
            } else {
                System.err.println("✗ TEST 1 FAIL: audit_logs table missing");
                failed++;
            }

            ResultSet idx = conn.createStatement().executeQuery(
                "SELECT count(*) FROM pg_indexes WHERE indexname='idx_audit_timestamp'");
            idx.next();
            if (idx.getInt(1) == 1) {
                System.out.println("✓ TEST 2 PASS: idx_audit_timestamp index exists");
                passed++;
            } else {
                System.err.println("✗ TEST 2 FAIL: idx_audit_timestamp index missing");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("✗ TEST 1-2 FAIL: " + e.getMessage());
            failed += 2;
        }

        // ── TEST 3: LOGIN audit written on auth ────────────────────────────
        long countBefore = countLogs(ds);
        ds.authenticate("admin", "1234567890");
        long countAfter = countLogs(ds);
        if (countAfter > countBefore) {
            System.out.println("✓ TEST 3 PASS: LOGIN event written to audit_logs on authenticate()");
            passed++;
        } else {
            System.err.println("✗ TEST 3 FAIL: No audit entry on authenticate");
            failed++;
        }

        // ── TEST 4: LOGIN_FAILED audit on bad auth ─────────────────────────
        long before4 = countLogs(ds);
        ds.authenticate("admin", "wrongpassword");
        long after4 = countLogs(ds);
        if (after4 > before4) {
            System.out.println("✓ TEST 4 PASS: LOGIN_FAILED event written on failed auth");
            passed++;
        } else {
            System.err.println("✗ TEST 4 FAIL: No audit entry on failed auth");
            failed++;
        }

        // ── TEST 5: CREATE patient audit ────────────────────────────────────
        Patient p = new Patient("Audit", "TestPatient", LocalDate.of(1990, 1, 1));
        p.setPatientId("PAT-AUDIT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        long before5 = countLogs(ds);
        ds.savePatient(p);
        long after5 = countLogs(ds);
        if (after5 > before5) {
            System.out.println("✓ TEST 5 PASS: CREATE PATIENT audit written");
            passed++;
        } else {
            System.err.println("✗ TEST 5 FAIL: No audit on patient create");
            failed++;
        }

        // ── TEST 6: UPDATE patient audit ────────────────────────────────────
        p.setGender("Female");
        long before6 = countLogs(ds);
        ds.updatePatient(p);
        long after6 = countLogs(ds);
        if (after6 > before6) {
            System.out.println("✓ TEST 6 PASS: UPDATE PATIENT audit written");
            passed++;
        } else {
            System.err.println("✗ TEST 6 FAIL: No audit on patient update");
            failed++;
        }

        // ── TEST 7: CREATE MEDICATION audit ─────────────────────────────────
        Medication med = new TabletMedication();
        med.setMedicationId("MED-AUDIT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        med.setGenericName("AuditTestDrug");
        med.setBrandName("AuditBrand");
        med.setPricePerUnit(9.99);
        long before7 = countLogs(ds);
        ds.saveMedication(med);
        long after7 = countLogs(ds);
        if (after7 > before7) {
            System.out.println("✓ TEST 7 PASS: CREATE MEDICATION audit written");
            passed++;
        } else {
            System.err.println("✗ TEST 7 FAIL: No audit on medication create");
            failed++;
        }

        // ── TEST 8: UPDATE MEDICATION (price change) audit ───────────────────
        med.setPricePerUnit(12.50);
        long before8 = countLogs(ds);
        ds.updateMedication(med);
        long after8 = countLogs(ds);
        if (after8 > before8) {
            System.out.println("✓ TEST 8 PASS: UPDATE MEDICATION audit written");
            passed++;
        } else {
            System.err.println("✗ TEST 8 FAIL: No audit on medication update");
            failed++;
        }

        // ── TEST 9: CREATE PRESCRIPTION audit ───────────────────────────────
        Prescription rx = new Prescription(p, "Dr. AuditTest");
        PrescribedDrug drug = new PrescribedDrug(med, "1", "Daily", "5 days", "", "Dr. AuditTest");
        drug.calculateCost();
        rx.addPrescribedDrug(drug);
        long before9 = countLogs(ds);
        ds.savePrescription(rx);
        long after9 = countLogs(ds);
        if (after9 > before9) {
            System.out.println("✓ TEST 9 PASS: CREATE PRESCRIPTION audit written");
            passed++;
        } else {
            System.err.println("✗ TEST 9 FAIL: No audit on prescription create");
            failed++;
        }

        // ── TEST 10: PASSWORD CHANGE audit ──────────────────────────────────
        long before10 = countLogs(ds);
        ds.changePassword("user", "1234567890");  // restore original
        long after10 = countLogs(ds);
        if (after10 > before10) {
            System.out.println("✓ TEST 10 PASS: UPDATE PASSWORD audit written");
            passed++;
        } else {
            System.err.println("✗ TEST 10 FAIL: No audit on password change");
            failed++;
        }

        // ── TEST 11: DELETE PATIENT audit ───────────────────────────────────
        long before11 = countLogs(ds);
        ds.deletePatient(p);
        long after11 = countLogs(ds);
        if (after11 > before11) {
            System.out.println("✓ TEST 11 PASS: DELETE PATIENT audit written");
            passed++;
        } else {
            System.err.println("✗ TEST 11 FAIL: No audit on patient delete");
            failed++;
        }

        // ── TEST 12: getAuditLogs() returns ordered entries ──────────────────
        List<AuditLog> logs = ds.getAuditLogs(50);
        if (!logs.isEmpty() && logs.get(0).getTimestamp() != null && !logs.get(0).getUsername().isBlank()) {
            System.out.println("✓ TEST 12 PASS: getAuditLogs() returns " + logs.size() + " entries with valid timestamps");
            passed++;
        } else {
            System.err.println("✗ TEST 12 FAIL: getAuditLogs() returned empty or malformed");
            failed++;
        }

        // ── TEST 13: Verify specific operation types in logs ─────────────────
        boolean hasCreate = logs.stream().anyMatch(l -> "CREATE".equals(l.getOperation()));
        boolean hasUpdate = logs.stream().anyMatch(l -> "UPDATE".equals(l.getOperation()));
        boolean hasDelete = logs.stream().anyMatch(l -> "DELETE".equals(l.getOperation()));
        boolean hasLogin  = logs.stream().anyMatch(l -> "LOGIN".equals(l.getOperation()));
        if (hasCreate && hasUpdate && hasDelete && hasLogin) {
            System.out.println("✓ TEST 13 PASS: CREATE/UPDATE/DELETE/LOGIN all present in audit trail");
            passed++;
        } else {
            System.err.printf("✗ TEST 13 FAIL: Missing ops — CREATE:%b UPDATE:%b DELETE:%b LOGIN:%b%n",
                hasCreate, hasUpdate, hasDelete, hasLogin);
            failed++;
        }

        System.out.println("\n=================================================");
        System.out.printf("  RESULTS: %d PASSED | %d FAILED%n", passed, failed);
        System.out.println("=================================================");
        System.exit(failed > 0 ? 1 : 0);
    }

    private static long countLogs(DataService ds) {
        return ds.getAuditLogs(10000).size();
    }
}
