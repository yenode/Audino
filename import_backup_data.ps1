param(
    [string]$DatabasePath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Escape-SqlValue([AllowNull()][string]$value) {
    if ($null -eq $value) {
        return 'NULL'
    }
    return "'" + ($value -replace "'", "''") + "'"
}

function Get-PropValue($obj, [string]$name) {
    $prop = $obj.PSObject.Properties[$name]
    if ($null -eq $prop) {
        return $null
    }
    return $prop.Value
}

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $repoRoot

$zipPath = Join-Path $repoRoot 'Audino-main.zip'
if (!(Test-Path $zipPath)) {
    throw "Backup zip not found: $zipPath"
}

$extractRoot = Join-Path $env:TEMP 'audino-main-backup-extract'
$dataRoot = Join-Path $extractRoot 'Audino-main\src\main\resources\data'

if (!(Test-Path $dataRoot)) {
    if (Test-Path $extractRoot) {
        Remove-Item -Recurse -Force $extractRoot
    }
    New-Item -ItemType Directory -Path $extractRoot | Out-Null
    Expand-Archive -Path $zipPath -DestinationPath $extractRoot -Force
}

$patients = Get-Content (Join-Path $dataRoot 'patients.json') -Raw | ConvertFrom-Json
$medications = Get-Content (Join-Path $dataRoot 'medications.json') -Raw | ConvertFrom-Json
$prescriptions = Get-Content (Join-Path $dataRoot 'prescriptions.json') -Raw | ConvertFrom-Json
$interactionRulesRaw = Get-Content (Join-Path $dataRoot 'interaction-rules.json') -Raw

if ([string]::IsNullOrWhiteSpace($DatabasePath)) {
    $dataDir = Join-Path $repoRoot 'data'
    if (!(Test-Path $dataDir)) {
        New-Item -ItemType Directory -Path $dataDir | Out-Null
    }
    $dbPath = Join-Path $dataDir 'audino.db'
} else {
    $dbPath = $DatabasePath
    $dbDir = Split-Path -Parent $dbPath
    if (![string]::IsNullOrWhiteSpace($dbDir) -and !(Test-Path $dbDir)) {
        New-Item -ItemType Directory -Path $dbDir | Out-Null
    }
}

$sqlLines = New-Object System.Collections.Generic.List[string]

$sqlLines.Add('PRAGMA foreign_keys = ON;')
$sqlLines.Add('BEGIN TRANSACTION;')

$sqlLines.Add(@"
CREATE TABLE IF NOT EXISTS patients (
    patient_id TEXT PRIMARY KEY,
    first_name TEXT,
    last_name TEXT,
    date_of_birth TEXT,
    gender TEXT,
    contact_number TEXT,
    allergies_json TEXT,
    chronic_conditions_json TEXT
);
"@)

$sqlLines.Add(@"
CREATE TABLE IF NOT EXISTS medications (
    medication_id TEXT PRIMARY KEY,
    generic_name TEXT,
    brand_name TEXT,
    medication_type TEXT,
    strength TEXT,
    concentration TEXT,
    route TEXT,
    active_ingredients_json TEXT,
    interaction_identifiers_json TEXT
);
"@)

$sqlLines.Add(@"
CREATE TABLE IF NOT EXISTS interaction_rules (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    rules_json TEXT NOT NULL
);
"@)

$sqlLines.Add(@"
CREATE TABLE IF NOT EXISTS prescriptions (
    prescription_id TEXT PRIMARY KEY,
    patient_id TEXT NOT NULL UNIQUE,
    created_at TEXT,
    prescribed_by TEXT,
    status TEXT,
    alerts_json TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE
);
"@)

$sqlLines.Add(@"
CREATE TABLE IF NOT EXISTS prescribed_drugs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    prescription_id TEXT NOT NULL,
    medication_id TEXT NOT NULL,
    dosage TEXT,
    frequency TEXT,
    duration TEXT,
    special_instructions TEXT,
    prescribed_by TEXT,
    FOREIGN KEY (prescription_id) REFERENCES prescriptions(prescription_id) ON DELETE CASCADE,
    FOREIGN KEY (medication_id) REFERENCES medications(medication_id)
);
"@)

$sqlLines.Add('DELETE FROM prescribed_drugs;')
$sqlLines.Add('DELETE FROM prescriptions;')
$sqlLines.Add('DELETE FROM interaction_rules;')
$sqlLines.Add('DELETE FROM medications;')
$sqlLines.Add('DELETE FROM patients;')

foreach ($p in $patients) {
    $dob = $null
    if ($null -ne $p.dateOfBirth -and $p.dateOfBirth.Count -ge 3) {
        $dob = ('{0:D4}-{1:D2}-{2:D2}' -f [int]$p.dateOfBirth[0], [int]$p.dateOfBirth[1], [int]$p.dateOfBirth[2])
    }

    $allergiesJson = '[]'
    if ($null -ne $p.allergies) {
        $allergiesJson = ($p.allergies | ConvertTo-Json -Compress)
    }

    $conditionsJson = '[]'
    if ($null -ne $p.chronicConditions) {
        $conditionsJson = ($p.chronicConditions | ConvertTo-Json -Compress)
    }

    $sqlLines.Add(
        "INSERT INTO patients (patient_id, first_name, last_name, date_of_birth, gender, contact_number, allergies_json, chronic_conditions_json) VALUES (" +
        "$(Escape-SqlValue $p.patientId), $(Escape-SqlValue $p.firstName), $(Escape-SqlValue $p.lastName), $(Escape-SqlValue $dob), " +
        "$(Escape-SqlValue $p.gender), $(Escape-SqlValue $p.contactNumber), $(Escape-SqlValue $allergiesJson), $(Escape-SqlValue $conditionsJson));"
    )
}

foreach ($m in $medications) {
    $activeJson = '[]'
    if ($null -ne $m.activeIngredients) {
        $activeJson = ($m.activeIngredients | ConvertTo-Json -Compress)
    }

    $identifiersJson = '[]'
    if ($null -ne $m.interactionIdentifiers) {
        $identifiersJson = ($m.interactionIdentifiers | ConvertTo-Json -Compress)
    }

    $strength = Get-PropValue $m 'strength'
    $concentration = Get-PropValue $m 'concentration'
    $route = Get-PropValue $m 'route'

    $sqlLines.Add(
        "INSERT INTO medications (medication_id, generic_name, brand_name, medication_type, strength, concentration, route, active_ingredients_json, interaction_identifiers_json) VALUES (" +
        "$(Escape-SqlValue $m.medicationId), $(Escape-SqlValue $m.genericName), $(Escape-SqlValue $m.brandName), $(Escape-SqlValue $m.medicationType), " +
        "$(Escape-SqlValue $strength), $(Escape-SqlValue $concentration), $(Escape-SqlValue $route), $(Escape-SqlValue $activeJson), $(Escape-SqlValue $identifiersJson));"
    )
}

foreach ($rx in $prescriptions) {
    $createdAt = $null
    if ($null -ne $rx.createdAt) {
        if ($rx.createdAt -is [System.Array] -and $rx.createdAt.Count -ge 6) {
            $createdAt = ('{0:D4}-{1:D2}-{2:D2} {3:D2}:{4:D2}:{5:D2}' -f [int]$rx.createdAt[0], [int]$rx.createdAt[1], [int]$rx.createdAt[2], [int]$rx.createdAt[3], [int]$rx.createdAt[4], [int]$rx.createdAt[5])
        } else {
            $createdAt = [string]$rx.createdAt
        }
    }

    $alertsJson = '[]'
    if ($null -ne $rx.alerts) {
        $alertsJson = ($rx.alerts | ConvertTo-Json -Compress -Depth 16)
    }

    $sqlLines.Add(
        "INSERT INTO prescriptions (prescription_id, patient_id, created_at, prescribed_by, status, alerts_json) VALUES (" +
        "$(Escape-SqlValue $rx.prescriptionId), $(Escape-SqlValue $rx.patientId), $(Escape-SqlValue $createdAt), " +
        "$(Escape-SqlValue $rx.prescribedBy), $(Escape-SqlValue $rx.status), $(Escape-SqlValue $alertsJson));"
    )

    if ($null -ne $rx.prescribedDrugs) {
        foreach ($drug in $rx.prescribedDrugs) {
            $sqlLines.Add(
                "INSERT INTO prescribed_drugs (prescription_id, medication_id, dosage, frequency, duration, special_instructions, prescribed_by) VALUES (" +
                "$(Escape-SqlValue $rx.prescriptionId), $(Escape-SqlValue $drug.medicationId), $(Escape-SqlValue $drug.dosage), " +
                "$(Escape-SqlValue $drug.frequency), $(Escape-SqlValue $drug.duration), $(Escape-SqlValue $drug.specialInstructions), $(Escape-SqlValue $drug.prescribedBy));"
            )
        }
    }
}

$sqlLines.Add("INSERT INTO interaction_rules (id, rules_json) VALUES (1, $(Escape-SqlValue $interactionRulesRaw));")
$sqlLines.Add('COMMIT;')

$sqlFile = Join-Path $env:TEMP 'audino-import-backup.sql'
$sqlLines | Set-Content -Path $sqlFile -Encoding UTF8

Get-Content $sqlFile | sqlite3 $dbPath
if ($LASTEXITCODE -ne 0) {
    throw "Import failed for database: $dbPath"
}

Write-Output "Imported into: $dbPath"
Write-Output 'Counts:'
sqlite3 $dbPath "SELECT 'patients' AS table_name, COUNT(*) AS count FROM patients UNION ALL SELECT 'medications', COUNT(*) FROM medications UNION ALL SELECT 'interaction_rules', COUNT(*) FROM interaction_rules UNION ALL SELECT 'prescriptions', COUNT(*) FROM prescriptions UNION ALL SELECT 'prescribed_drugs', COUNT(*) FROM prescribed_drugs;"
if ($LASTEXITCODE -ne 0) {
    throw "Post-import verification failed for database: $dbPath"
}

Write-Output 'Sample patients:'
sqlite3 $dbPath "SELECT patient_id, first_name, last_name FROM patients ORDER BY patient_id LIMIT 5;"

Write-Output 'Sample medications:'
sqlite3 $dbPath "SELECT medication_id, generic_name, brand_name FROM medications ORDER BY medication_id LIMIT 8;"
