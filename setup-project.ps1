# =============================================================================
#  REST Assured API Automation Suite — Project Scaffold
#  Run from the directory where you want the project created:
#    .\setup-project.ps1
# =============================================================================

$projectName = "restassured-api-test-suite"
$base        = Join-Path (Get-Location) $projectName

function New-Dir  { param($p) New-Item -ItemType Directory -Force -Path $p | Out-Null }
function New-File { param($p) New-Item -ItemType File      -Force -Path $p | Out-Null }

Write-Host "`n  Building project structure..." -ForegroundColor Cyan

# ── Root
New-Dir $base

# ── Maven standard layout
$srcTest   = "$base\src\test\java\com\apitest"
$srcMain   = "$base\src\main\java\com\apitest"
$resources = "$base\src\test\resources"

foreach ($pkg in @("tests","base","utils","payloads","models")) {
    New-Dir "$srcTest\$pkg"
}
foreach ($pkg in @("config")) {
    New-Dir "$srcMain\$pkg"
}

# ── Resource folders
foreach ($folder in @("schemas\json","schemas\xml","testdata","config")) {
    New-Dir "$resources\$folder"
}

# ── GitHub Actions
New-Dir "$base\.github\workflows"

# ── Reports output (gitignored)
New-Dir "$base\target\extent-reports"

# ── Root files
foreach ($f in @("pom.xml","testng.xml","README.md",".gitignore")) {
    New-File "$base\$f"
}

# ── GitHub Actions workflow
New-File "$base\.github\workflows\api-tests.yml"

# ── Config
New-File "$resources\config\config.properties"

# ── Test classes
$testFiles = @(
    "tests\CrudWorkflowTest.java",
    "tests\SchemaValidationTest.java",
    "tests\ContractValidationTest.java",
    "tests\ChainedEndpointTest.java",
    "tests\AuthTests.java",
    "tests\NegativeEdgeCaseTest.java",
    "base\BaseTest.java",
    "utils\TokenStore.java",
    "utils\ExtentReportManager.java",
    "payloads\UserPayload.java",
    "payloads\OrderPayload.java",
    "models\User.java"
)
foreach ($f in $testFiles) { New-File "$srcTest\$f" }

# ── Schema files
New-File "$resources\schemas\json\user-schema.json"
New-File "$resources\schemas\json\order-schema.json"
New-File "$resources\schemas\xml\product-schema.xml"

# ── Test data
New-File "$resources\testdata\users.json"

Write-Host ""
Write-Host "  Project scaffold complete." -ForegroundColor Green
Write-Host ""
Write-Host "  Location : $base" -ForegroundColor White
Write-Host ""
Write-Host "  Structure:" -ForegroundColor Yellow

Get-ChildItem -Recurse -Path $base |
    Where-Object { $_.FullName -notmatch '\\target\\' } |
    ForEach-Object {
        $rel    = $_.FullName.Substring($base.Length + 1)
        $depth  = ($rel.ToCharArray() | Where-Object { $_ -eq '\' }).Count
        $indent = "  " * $depth
        $icon   = if ($_.PSIsContainer) { "  " } else { "  " }
        Write-Host "$indent$icon$($_.Name)" -ForegroundColor Gray
    }

Write-Host ""
Write-Host "  Next steps:" -ForegroundColor Cyan
Write-Host "    1. cd $projectName"                                -ForegroundColor White
Write-Host "    2. Paste each generated file into its location"    -ForegroundColor White
Write-Host "    3. mvn test  (requires Java 17 + Maven on PATH)"   -ForegroundColor White
Write-Host "    4. git init && git remote add origin <your-repo>"  -ForegroundColor White
Write-Host "    5. git push -u origin main"                        -ForegroundColor White
Write-Host ""
